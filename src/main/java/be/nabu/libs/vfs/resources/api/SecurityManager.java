package be.nabu.libs.vfs.resources.api;

import java.security.Principal;

import be.nabu.libs.resources.api.Resource;

/**
 * Determines which crud actions are possible for the given principal
 * 
 * @author alex
 *
 */
public interface SecurityManager {
	public boolean canRead(Principal principal, Resource resource);
	public boolean canWrite(Principal principal, Resource resource);
	public boolean canList(Principal principal, Resource resource);
	public boolean canDelete(Principal principal, Resource resource);
	public boolean canCreateIn(Principal principal, Resource resource, String contentType);
}
