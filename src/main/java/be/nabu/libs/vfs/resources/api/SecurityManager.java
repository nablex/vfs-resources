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
