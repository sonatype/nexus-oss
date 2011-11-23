package de.is24.nexus.yum.repository;

import org.sonatype.scheduling.ScheduledTask;

public class SameYumTaskAlreadSubmittedException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final ScheduledTask<YumRepository> sameScheduledTask;

	public SameYumTaskAlreadSubmittedException(ScheduledTask<YumRepository> sameScheduledTask, String message) {
		super(message);
		this.sameScheduledTask = sameScheduledTask;
	}

	public ScheduledTask<YumRepository> getSameTask() {
		return sameScheduledTask;
	}

}
