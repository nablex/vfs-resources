package be.nabu.libs.vfs.resources.impl;

import java.security.Principal;

import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.libs.vfs.resources.api.SecurityManager;

/**
 * A simple security manager that simply evaluates if the backend supports it
 */
public class SimpleSecurityManager implements SecurityManager {
	
	@Override
	public boolean canRead(Principal principal, Resource resource) {
		return resource instanceof ReadableResource;
	}

	@Override
	public boolean canWrite(Principal principal, Resource resource) {
		return resource instanceof WritableResource;
	}

	@Override
	public boolean canList(Principal principal, Resource resource) {
		return resource instanceof ResourceContainer;
	}

	@Override
	public boolean canDelete(Principal principal, Resource resource) {
		return resource.getParent() != null && resource.getParent() instanceof ManageableContainer;
	}

	@Override
	public boolean canCreateIn(Principal principal, Resource resource, String contentType) {
		return resource instanceof ManageableContainer;
	}
}
