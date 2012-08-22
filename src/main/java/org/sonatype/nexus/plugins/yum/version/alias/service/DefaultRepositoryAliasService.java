package org.sonatype.nexus.plugins.yum.version.alias.service;

import java.io.File;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.yum.config.YumConfiguration;
import org.sonatype.nexus.plugins.yum.metarepo.service.RepositoryRpmManager;
import org.sonatype.nexus.plugins.yum.version.alias.AliasNotFoundException;


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
