package de.is24.nexus.yum.service;

public interface AliasMapper {
  String DEFAULT_BEAN_NAME = "YumConfiguration";

  String getVersion(String repositoryId, String alias) throws AliasNotFoundException;

  void setAlias(String repositoryId, String alias, String version);
}
