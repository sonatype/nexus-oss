package de.is24.nexus.yum.service.impl;

import java.io.File;
import javax.inject.Inject;
import org.codehaus.plexus.component.annotations.Component;
import de.is24.nexus.yum.service.AliasNotFoundException;
import de.is24.nexus.yum.service.RepositoryAliasService;
import de.is24.nexus.yum.service.RepositoryRpmManager;
import de.is24.nexus.yum.service.YumConfiguration;


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
