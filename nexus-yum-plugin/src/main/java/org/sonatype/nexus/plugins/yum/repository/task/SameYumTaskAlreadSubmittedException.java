package org.sonatype.nexus.plugins.yum.repository.task;

import org.sonatype.scheduling.ScheduledTask;

import org.sonatype.nexus.plugins.yum.repository.YumRepository;

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
