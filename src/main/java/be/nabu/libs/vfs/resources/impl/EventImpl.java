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

import be.nabu.libs.vfs.api.File;
import be.nabu.libs.vfs.api.events.Event;
import be.nabu.libs.vfs.api.events.EventType;

public class EventImpl implements Event {

	private EventType type;
	private File file;
	private boolean isDone;

	public EventImpl(EventType type, File file, boolean isDone) {
		this.type = type;
		this.file = file;
		this.isDone = isDone;
	}
	
	@Override
	public EventType getEventType() {
		return type;
	}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

}
