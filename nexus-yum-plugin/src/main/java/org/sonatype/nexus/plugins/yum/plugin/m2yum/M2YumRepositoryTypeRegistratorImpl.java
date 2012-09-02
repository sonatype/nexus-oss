package org.sonatype.nexus.plugins.yum.plugin.m2yum;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.plexus.config.Strategies;
import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;


@Component(role = M2YumRepositoryTypeRegistrator.class, instantiationStrategy = Strategies.LOAD_ON_START)
@Singleton
public class M2YumRepositoryTypeRegistratorImpl implements M2YumRepositoryTypeRegistrator {
  private static final Logger LOG = LoggerFactory.getLogger(M2YumRepositoryTypeRegistratorImpl.class);

  @Inject
  private RepositoryTypeRegistry repositoryTypeRegistry;

  @Inject
  public void registerRepositoryType() {
    LOG.info("Try register my M2YumRepository and M2YumGroupRepository to the RepositoryTypeRegistry");
    repositoryTypeRegistry.registerRepositoryTypeDescriptors(m2yumDescriptor());
    repositoryTypeRegistry.registerRepositoryTypeDescriptors(m2yumGroupDescriptor());
  }

  private RepositoryTypeDescriptor m2yumDescriptor() {
    return new RepositoryTypeDescriptor(Repository.class, M2YumRepository.ID, "repositories", RepositoryType.UNLIMITED_INSTANCES);
  }

  private RepositoryTypeDescriptor m2yumGroupDescriptor() {
    return new RepositoryTypeDescriptor(GroupRepository.class, M2YumGroupRepository.ID, "groups", RepositoryType.UNLIMITED_INSTANCES);
  }
}
