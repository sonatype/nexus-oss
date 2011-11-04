package de.is24.nexus.yum.plugin.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import javax.inject.Inject;
import javax.inject.Named;
import de.is24.nexus.yum.AbstractRepositoryTester;
import de.is24.nexus.yum.guice.NexusTestRunner;
import de.is24.nexus.yum.plugin.RepositoryRegistry;
import de.is24.nexus.yum.service.YumService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;


@RunWith(NexusTestRunner.class)
public class RpmDeployedListenerTest extends AbstractRepositoryTester {
  @Inject
  private RpmDeployedListener listener;

  @Inject
  @Named(RepositoryRegistry.DEFAULT_BEAN_NAME)
  private RepositoryRegistry repositoryRegistry;

  @Inject
  @Named(YumService.DEFAULT_BEAN_NAME)
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
    assertTrue(repositoryRegistry.isRegistered(repo));
  }

  @Test
  public void shouldNotRegisterRepository() throws Exception {
    Repository repo = createRepository(false);
    repositoryRegistry.unregisterRepository(repo);
    listener.onEvent(new RepositoryRegistryEventAdd(null, repo));
    assertFalse(repositoryRegistry.isRegistered(repo));
  }

  @Test
  public void shouldNotCreateRepo() {
    Repository repo = createRepository(true);
    repositoryRegistry.unregisterRepository(repo);
    listener.onEvent(new RepositoryItemEventStore(repo, createItem("VERSION", "test-source.jar")));
  }

  @Test
  public void shouldNotCreateRepoForPom() {
    yumService.deactivate();

    MavenRepository repo = createRepository(true);
    repositoryRegistry.registerRepository(repo);
    listener.onEvent(new RepositoryItemEventStore(repo, createItem("VERSION", "test.pom")));
  }

  @Test
  public void shouldCreateRepoForPom() {
    yumService.deactivate();

    MavenRepository repo = createRepository(true);
    repositoryRegistry.registerRepository(repo);
    listener.onEvent(new RepositoryItemEventStore(repo, createItem("VERSION", "test.rpm")));
  }

}
