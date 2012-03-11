package de.is24.nexus.yum.plugin.impl;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStoreCreate;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;

import de.is24.nexus.yum.AbstractRepositoryTester;
import de.is24.nexus.yum.plugin.ItemEventListener;
import de.is24.nexus.yum.plugin.RepositoryRegistry;
import de.is24.nexus.yum.repository.service.YumService;


public class RpmRepositoryEventListenerTest extends AbstractRepositoryTester {
  @Inject
  private ItemEventListener listener;

  @Inject
  private RepositoryRegistry repositoryRegistry;

  @Inject
  private YumService yumService;

  @Before
  public void activateRepo() {
    yumService.activate();
  }

  @After
  public void reactivateRepo() {
    yumService.activate();
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
    yumService.deactivate();

    MavenRepository repo = createRepository(true);
    repositoryRegistry.registerRepository(repo);
    listener.onEvent(new RepositoryItemEventStoreCreate(repo, createItem("VERSION", "test.pom")));
  }

  @Test
  public void shouldCreateRepoForPom() {
    yumService.deactivate();

    MavenRepository repo = createRepository(true);
    repositoryRegistry.registerRepository(repo);
    listener.onEvent(new RepositoryItemEventStoreCreate(repo, createItem("VERSION", "test.rpm")));
  }

}
