package be.nabu.libs.vfs.resources.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;

import be.nabu.libs.events.api.EventDispatcher;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.URIUtils;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.vfs.resources.api.SecurityManager;
import be.nabu.libs.vfs.resources.api.TransformationManager;
import be.nabu.libs.vfs.api.File;
import be.nabu.libs.vfs.api.FileSystem;

public class ResourceFileSystem implements FileSystem {

	private Principal principal;
	private EventDispatcher dispatcher;
	private SecurityManager securityManager;
	private TransformationManager transformationManager;
	private ResourceFile root;
	
	public ResourceFileSystem(EventDispatcher dispatcher, String path, Principal principal) throws IOException, URISyntaxException {
		this(dispatcher, path.matches("^[\\w]+:.*") ? new URI(path) : (path.startsWith("/") ? new URI("file:" + path) : new java.io.File(path).toURI()), principal);
	}
	
	public ResourceFileSystem(EventDispatcher dispatcher, ResourceContainer<?> resource, Principal principal) throws IOException, URISyntaxException {
		this.dispatcher = dispatcher;
		this.principal = principal;
		this.securityManager = new SimpleSecurityManager();
		this.root = new ResourceFile(this, new URI("/"), getTransformationManager().transform(resource), principal);
	}
	
	public ResourceFileSystem(EventDispatcher dispatcher, URI uri, Principal principal) throws IOException, URISyntaxException {
		this(dispatcher, ResourceUtils.mkdir(uri, principal), principal);
	}
	
	@Override
	public void close() throws IOException {
		ResourceUtils.close(root.getResource());
	}

	@Override
	public File resolve(String path, Principal principal) throws IOException {
		if (path == null || path.isEmpty() || path.equals("/")) {
			return root;
		}
		else {
			if (path.startsWith("/")) {
				path = path.substring(1);
			}
			URI child = URIUtils.getChild(root.getURI(), path);
			return new ResourceFile(this, child, getTransformationManager().transform(ResourceUtils.resolve(root.getResource(), path)), principal);
		}
	}

	@Override
	public File resolve(String path) throws IOException {
		return resolve(path, principal);
	}

	public URI getFullPath() {
		return ResourceUtils.getURI(root.getResource());
	}
	
	@Override
	public EventDispatcher getEventDispatcher() {
		return dispatcher;
	}
	
	SecurityManager getSecurityManager() {
		return securityManager;
	}

	public TransformationManager getTransformationManager() {
		if (transformationManager == null) {
			transformationManager = new VersatileTransformationManager();
		}
		return transformationManager;
	}

	public void setTransformationManager(TransformationManager transformationManager) {
		this.transformationManager = transformationManager;
	}
}
