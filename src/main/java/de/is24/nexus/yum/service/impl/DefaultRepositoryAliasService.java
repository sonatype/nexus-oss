package de.is24.nexus.yum.service.impl;

import java.io.File;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.sonatype.plugin.Managed;
import de.is24.nexus.yum.service.AliasMapper;
import de.is24.nexus.yum.service.AliasNotFoundException;
import de.is24.nexus.yum.service.RepositoryAliasService;
import de.is24.nexus.yum.service.RepositoryRpmManager;


@Managed
@Named(RepositoryAliasService.DEFAULT_BEAN_NAME)
@Singleton
public class DefaultRepositoryAliasService implements RepositoryAliasService {
  @Inject
  private AliasMapper aliasMapper;

  @Inject
  @Named(RepositoryRpmManager.DEFAULT_BEAN_NAME)
  private RepositoryRpmManager repositoryRpmManager;

  public File getFile(String repositoryId, String alias) throws AliasNotFoundException {
    String version = aliasMapper.getVersion(repositoryId, alias);
    return repositoryRpmManager.updateRepository(repositoryId, version);
  }

}
