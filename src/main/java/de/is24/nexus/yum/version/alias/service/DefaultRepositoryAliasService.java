package de.is24.nexus.yum.version.alias.service;

import java.io.File;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Component;

import de.is24.nexus.yum.config.YumConfiguration;
import de.is24.nexus.yum.version.alias.AliasNotFoundException;
import de.is24.nexus.yum.version.service.RepositoryRpmManager;


@Component(role = RepositoryAliasService.class)
public class DefaultRepositoryAliasService implements RepositoryAliasService {
  @Inject
  private YumConfiguration aliasMapper;

  @Inject
  private RepositoryRpmManager repositoryRpmManager;

  @Override
  public File getFile(String repositoryId, String alias) throws AliasNotFoundException {
    String version = aliasMapper.getVersion(repositoryId, alias);
    return repositoryRpmManager.updateRepository(repositoryId, version);
  }

}
