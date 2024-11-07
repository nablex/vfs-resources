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
