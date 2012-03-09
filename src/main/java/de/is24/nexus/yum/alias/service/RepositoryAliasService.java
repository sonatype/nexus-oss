package de.is24.nexus.yum.alias.service;

import java.io.File;

import de.is24.nexus.yum.alias.AliasNotFoundException;


public interface RepositoryAliasService {
  File getFile(String repositoryId, String alias) throws AliasNotFoundException;

}
