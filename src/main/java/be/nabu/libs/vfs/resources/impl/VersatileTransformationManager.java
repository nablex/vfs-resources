package be.nabu.libs.vfs.resources.impl;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.nabu.libs.resources.ArchiveFactory;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.TransformerFactory;
import be.nabu.libs.resources.api.Archive;
import be.nabu.libs.resources.api.ArchiveResolver;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.Transformer;
import be.nabu.libs.resources.api.TransformerResolver;
import be.nabu.libs.vfs.resources.api.TransformationManager;

public class VersatileTransformationManager implements TransformationManager {

	private ArchiveFactory archiveFactory;
	private TransformerFactory transformerFactory;
	private Map<String, List<Class<? extends Transformer>>> transformers = new HashMap<String, List<Class<? extends Transformer>>>();
	
	public VersatileTransformationManager() {
		this(null, null);
	}
	
	public VersatileTransformationManager(ArchiveFactory archiveFactory, TransformerFactory transformerFactory) {
		this.archiveFactory = archiveFactory;
		this.transformerFactory = transformerFactory;
	}
	
	public void addTransformer(String path, Class<? extends Transformer> transformer) {
		// if the path is null, it applies to the entire system
		if (path == null)
			path = "/";
		if (!transformers.containsKey(path))
			transformers.put(path, new ArrayList<Class<? extends Transformer>>());
		transformers.get(path).add(transformer);
	}
	
	private String getApplicablePath(Resource resource) {
		String path = ResourceUtils.getPath(resource);
		String currentPath = null;
		for (String possiblePath : transformers.keySet()) {
			if (path.startsWith(possiblePath) && (currentPath == null || possiblePath.length() > currentPath.length()))
				currentPath = possiblePath;
		}
		return currentPath;
	}
	
	@Override
	public Resource transform(Resource resource) {
		return mountArchive(
			wrapTransformers(resource)
		);
	}
	
	private Resource wrapTransformers(Resource resource) {
		// if it can't be read, it can't be transformed
		// note that transformers are only layered upon readable, not somewhere in the middle of the stack
		if (!(resource instanceof Readable))
			return resource;
		
		// check if it needs to be transformed
		String transformerPath = getApplicablePath(resource);
		// no path applies
		if (transformerPath == null)
			return resource;
		
		for (Class<? extends Transformer> transformer : transformers.get(transformerPath)) {
			try {
				Transformer instance = transformer.newInstance();
				instance.setSource(resource);
				resource = instance;
			}
			catch (InstantiationException e) {
				throw new RuntimeException(e);
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		return resource;
	}
	
	@SuppressWarnings("rawtypes")
	private Resource mountArchive(Resource resource) {
		// can't read it
		if (!(resource instanceof Readable))
			return resource;
		// unknown content type
		else if (resource.getContentType() == null)
			return resource;
		
		ArchiveResolver archiveResolver = getArchiveFactory().getResolver(resource.getContentType());
		List<Transformer> transformers = new ArrayList<Transformer>();
		// there is no archive factory for this content type, check if after some transformations, you find an archive mounter
		if (archiveResolver == null) {
			archiveResolver = getTransformersToArchive(resource.getName(), transformers);
		}
		
		if (archiveResolver == null) {
			return resource;
		}
		else {
			for (Transformer transformer : transformers) {
				transformer.setSource(resource);
				resource = transformer;
			}
			Archive archive = archiveResolver.newInstance();
			archive.setSource(resource);
			return archive;
		}
	}
	
	/**
	 * After the fact addition to allow automounting after one or more transformations have occured, e.g. files with extension "tar.gz"
	 */
	private ArchiveResolver getTransformersToArchive(String fileName, List<Transformer> transformers) {
		String contentType = URLConnection.guessContentTypeFromName(fileName);
		// unknown content type
		if (contentType == null)
			return null;
		
		TransformerResolver resolver = getTransformerFactory().getResolver(contentType);
		// no transformer for this 
		if (resolver == null)
			return null;
		
		transformers.add(resolver.newInstance());
		// dump the last extension for this fileName, it indicated the transformer
		fileName = fileName.replaceAll("\\.[^.]+$", "");
		if (fileName.contains(".")) {
			// check if there is an archivefactory for the new type
			contentType = URLConnection.guessContentTypeFromName(fileName);
			if (contentType == null)
				return null;
			ArchiveResolver archiveResolver = getArchiveFactory().getResolver(contentType);
			if (archiveResolver == null)
				return getTransformersToArchive(fileName, transformers);
			else
				return archiveResolver;
		}
		return null;		
	}

	public ArchiveFactory getArchiveFactory() {
		if (archiveFactory == null) {
			archiveFactory = ArchiveFactory.getInstance();
		}
		return archiveFactory;
	}

	public TransformerFactory getTransformerFactory() {
		if (transformerFactory == null) {
			transformerFactory = TransformerFactory.getInstance();
		}
		return transformerFactory;
	}
	
}
