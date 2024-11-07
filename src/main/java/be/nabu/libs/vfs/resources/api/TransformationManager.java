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
