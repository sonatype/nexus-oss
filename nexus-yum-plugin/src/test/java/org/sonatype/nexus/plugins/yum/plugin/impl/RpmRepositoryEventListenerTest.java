package org.sonatype.nexus.plugins.yum.plugin.impl;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStoreCreate;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;

import org.sonatype.nexus.plugins.yum.AbstractRepositoryTester;
import org.sonatype.nexus.plugins.yum.config.YumConfiguration;
import org.sonatype.nexus.plugins.yum.plugin.ItemEventListener;
import org.sonatype.nexus.plugins.yum.plugin.RepositoryRegistry;


public class RpmRepositoryEventListenerTest extends AbstractRepositoryTester {
  @Inject
  private ItemEventListener listener;

  @Inject
  private RepositoryRegistry repositoryRegistry;

  @Inject
  private YumConfiguration yumConfig;

  @Before
  public void activateRepo() {
    yumConfig.setActive(true);
  }

  @After
  public void reactivateRepo() {
    yumConfig.setActive(true);
  }

  @Test
  public void shouldRegisterRepository() throws Exception {
    Repository repo = createRepository(true);
    listener.onEvent(new RepositoryRegistryEventAdd(null, repo));
    Assert.assertTrue(repositoryRegistry.isRegistered(repo));
  }

  @Test
  public void shouldNotRegisterRepository() throws Exception {
    Repository repo = createRepository(false);
    repositoryRegistry.unregisterRepository(repo);
    listener.onEvent(new RepositoryRegistryEventAdd(null, repo));
    Assert.assertFalse(repositoryRegistry.isRegistered(repo));
  }

  @Test
  public void shouldNotCreateRepo() {
    Repository repo = createRepository(true);
    repositoryRegistry.unregisterRepository(repo);
    listener.onEvent(new RepositoryItemEventStoreCreate(repo, createItem("VERSION", "test-source.jar")));
  }

  @Test
  public void shouldNotCreateRepoForPom() {
    yumConfig.setActive(false);

    MavenRepository repo = createRepository(true);
    repositoryRegistry.registerRepository(repo);
    listener.onEvent(new RepositoryItemEventStoreCreate(repo, createItem("VERSION", "test.pom")));
  }

  @Test
  public void shouldCreateRepoForPom() {
    yumConfig.setActive(false);

    MavenRepository repo = createRepository(true);
    repositoryRegistry.registerRepository(repo);
    listener.onEvent(new RepositoryItemEventStoreCreate(repo, createItem("VERSION", "test.rpm")));
  }

}
