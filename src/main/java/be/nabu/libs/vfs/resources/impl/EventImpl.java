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
