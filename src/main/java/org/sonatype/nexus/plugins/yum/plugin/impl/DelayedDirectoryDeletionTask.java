package org.sonatype.nexus.plugins.yum.plugin.impl;

import org.sonatype.nexus.plugins.yum.plugin.DeletionService;
import org.sonatype.nexus.proxy.repository.Repository;

import org.sonatype.nexus.plugins.yum.plugin.DeletionService;

public class DelayedDirectoryDeletionTask implements Runnable {

  private final DeletionService service;
  private final Repository repository;
  private final String path;
  private boolean active = false;
  private int executionCount = 0;

  public DelayedDirectoryDeletionTask(DeletionService service, Repository repository, String path) {
    this.service = service;
    this.repository = repository;
    this.path = path + "/";
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public void run() {
    executionCount++;
      service.execute(this);
  }

  public boolean isParent(Repository repo, String subPath) {
    return repository.getId().equals(repository.getId()) && subPath.startsWith(path);
  }

  public Repository getRepository() {
    return repository;
  }

  public boolean isActive() {
    return active;
  }

  public String getPath() {
    return path;
  }

  public int getExecutionCount() {
    return executionCount;
  }
}
