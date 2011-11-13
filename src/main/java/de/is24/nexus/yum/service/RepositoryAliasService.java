package de.is24.nexus.yum.service;

import java.io.File;


public interface RepositoryAliasService {
  File getFile(String repositoryId, String alias) throws AliasNotFoundException;

}
