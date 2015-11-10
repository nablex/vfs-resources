package be.nabu.libs.vfs.resources.api;

import be.nabu.libs.resources.api.Resource;

/**
 * The task of the transformation manager is to take a resource and transform it according to some internal logic
 * For instance you may want to automount archives or automatically encrypt/decrypt any writes/reads
 */
public interface TransformationManager {
	
	/** 
	 * If no transformation is necessary, the resource is returned as is
	 */
	public Resource transform(Resource resource);
	
}
