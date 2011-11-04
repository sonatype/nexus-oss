package de.is24.nexus.yum.service;

import java.io.File;
import org.sonatype.plugin.Managed;


@Managed
public interface RepositoryAliasService {
  String DEFAULT_BEAN_NAME = "repositoryAliasService";

  File getFile(String repositoryId, String alias) throws AliasNotFoundException;

}
