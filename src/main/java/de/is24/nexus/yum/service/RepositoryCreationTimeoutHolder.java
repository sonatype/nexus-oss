package de.is24.nexus.yum.service;

public interface RepositoryCreationTimeoutHolder {
  String DEFAULT_BEAN_NAME = "YumConfiguration";

  int getRepositoryCreationTimeout();

  void setRepositoryCreationTimeout(int seconds);
}
