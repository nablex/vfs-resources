package be.nabu.libs.vfs.resources.impl;

import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.vfs.resources.api.TransformationManager;

/**
 * A non-transforming implementation
 */
public class SimpleTransformationManager implements TransformationManager {

	@Override
	public Resource transform(Resource resource) {
		return resource;
	}

}
