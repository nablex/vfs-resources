/*
* Copyright (C) 2015 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.libs.vfs.resources.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;
import java.util.Date;
import java.util.Iterator;

import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.URIUtils;
import be.nabu.libs.resources.api.FiniteResource;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.resources.api.TimestampedResource;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.libs.resources.api.features.CacheableResource;
import be.nabu.utils.io.IOUtils;
import be.nabu.libs.vfs.api.File;
import be.nabu.libs.vfs.api.events.EventType;

public class ResourceFile implements File {

	private URI uri;
	private Resource resource;
	private Principal principal;
	private ResourceFileSystem fileSystem;

	ResourceFile(ResourceFileSystem fileSystem, URI uri, Resource resource, Principal principal) {
		this.fileSystem = fileSystem;
		this.uri = uri;
		this.resource = preprocess(resource);
		this.principal = principal;
	}
	
	private Resource preprocess(Resource resource) {
		if (resource instanceof CacheableResource) {
			((CacheableResource) resource).setCaching(false);
		}
		return resource;
	}
	
	@Override
	public Iterator<File> iterator() {
		return new ResourceIterator();
	}

	private class ResourceIterator implements Iterator<File> {
		
		private Iterator<? extends Resource> iterator;

		public ResourceIterator() {
			if (resource instanceof ResourceContainer) {
				iterator = ((ResourceContainer<?>) resource).iterator();
			}
		}
		
		@Override
		public boolean hasNext() {
			return iterator != null && iterator.hasNext();
		}

		@Override
		public File next() {
			Resource resource = iterator == null || !iterator.hasNext() ? null : iterator.next();
			return resource == null ? null : new ResourceFile(fileSystem, URIUtils.getChild(uri, resource.getName()), resource, principal);
		}

		@Override
		public void remove() {
			if (iterator != null) {
				iterator.remove();
			}
		}
	}
	
	@Override
	public boolean exists() {
		return resource != null;
	}

	@Override
	public boolean isDirectory() {
		return resource instanceof ResourceContainer;
	}

	@Override
	public boolean isFile() {
		return resource instanceof ReadableResource;
	}

	@Override
	public boolean isReadable() {
		return isFile() && getFileSystem().getSecurityManager().canRead(principal, resource);
	}

	@Override
	public boolean isWritable() {
		return isFile() && getFileSystem().getSecurityManager().canWrite(principal, resource);
	}
	
	public URI getFullPath() {
		return URIUtils.getChild(fileSystem.getFullPath(), uri.getPath());
	}

	@Override
	public File resolve(String path) throws IOException {
		if (path.isEmpty() || path.equals("/")) {
			return getFileSystem().resolve("/");
		}
		if (path.startsWith("/")) {
			return getFileSystem().resolve(path);
		}
		URI child = URIUtils.getChild(uri, path);
		return getFileSystem().resolve(child.getPath());
	}

	@Override
	public ResourceFileSystem getFileSystem() {
		return fileSystem;
	}

	@Override
	public File getParent() throws IOException {
		if (uri.getPath().equals("/")) {
			return null;
		}
		else {
			URI parent = URIUtils.getParent(uri);
			return parent == null ? null : getFileSystem().resolve(parent.getPath());
		}
	}

	@Override
	public String getPath() {
		return uri.getPath();
	}

	@Override
	public String getName() {
		return URIUtils.getName(uri);
	}

	@Override
	public long getSize() throws IOException {
		return resource instanceof FiniteResource ? ((FiniteResource) resource).getSize() : -1;
	}

	@Override
	public String getContentType() throws IOException {
		return resource == null ? null : resource.getContentType();
	}

	@Override
	public Date getLastModified() throws IOException {
		return resource instanceof TimestampedResource ? ((TimestampedResource) resource).getLastModified() : new Date();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		fileSystem.getEventDispatcher().fire(new EventImpl(EventType.READ, this, false), this);
		return isReadable() ? IOUtils.toInputStream(((ReadableResource) resource).getReadable()) : null;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		if (resource == null) {
			fileSystem.getEventDispatcher().fire(new EventImpl(EventType.CREATE, this, false), this);
			resource = preprocess(ResourceUtils.touch(getFullPath(), principal));
			fileSystem.getEventDispatcher().fire(new EventImpl(EventType.CREATE, this, true), this);
		}
		fileSystem.getEventDispatcher().fire(new EventImpl(EventType.WRITE, this, false), this);
		return isReadable() ? IOUtils.toOutputStream(((WritableResource) resource).getWritable()) : null;
	}

	@Override
	public void mkdir() throws IOException {
		if (resource == null) {
			fileSystem.getEventDispatcher().fire(new EventImpl(EventType.CREATE, this, false), this);
			resource = preprocess(ResourceUtils.mkdir(getFullPath(), principal));
			fileSystem.getEventDispatcher().fire(new EventImpl(EventType.CREATE, this, true), this);
		}
	}

	@Override
	public void delete() throws IOException {
		if (resource != null && resource.getParent() instanceof ManageableContainer) {
			fileSystem.getEventDispatcher().fire(new EventImpl(EventType.DELETE, this, false), this);
			((ManageableContainer<?>) resource.getParent()).delete(resource.getName());
			resource = null;
			fileSystem.getEventDispatcher().fire(new EventImpl(EventType.DELETE, this, true), this);
		}
	}

	@Override
	public void move(File target) throws IOException {
		boolean copied = false;
		InputStream input = getInputStream();
		if (input != null) {
			fileSystem.getEventDispatcher().fire(new EventImpl(EventType.MOVE, this, false), this);
			try {
				OutputStream output = target.getOutputStream();
				if (output != null) {
					try {
						IOUtils.copyBytes(IOUtils.wrap(input), IOUtils.wrap(output));
						copied = true;
					}
					finally {
						output.close();
					}
				}
			}
			finally {
				input.close();
			}
		}
		if (copied) {
			delete();
			fileSystem.getEventDispatcher().fire(new EventImpl(EventType.MOVE, this, true), this);
		}
	}

	@Override
	public File cloneFor(Principal principal) {
		return new ResourceFile(fileSystem, uri, resource, principal);
	}

	Resource getResource() {
		return resource;
	}
	
	URI getURI() {
		return uri;
	}

}
