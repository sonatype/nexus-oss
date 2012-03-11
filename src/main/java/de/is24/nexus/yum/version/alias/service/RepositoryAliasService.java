package de.is24.nexus.yum.version.alias.service;

import java.io.File;

import de.is24.nexus.yum.version.alias.AliasNotFoundException;


public interface RepositoryAliasService {
  File getFile(String repositoryId, String alias) throws AliasNotFoundException;

}
