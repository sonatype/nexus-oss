package org.sonatype.nexus.plugins.yum.version.alias.service;

import java.io.File;

import org.sonatype.nexus.plugins.yum.version.alias.AliasNotFoundException;


public interface RepositoryAliasService {
  File getFile(String repositoryId, String alias) throws AliasNotFoundException;

}
