package de.is24.nexus.yum.plugin.impl;

import static de.is24.test.hamcrest.FileMatchers.exists;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertThat;
import java.io.File;
import java.util.ArrayList;
import javax.inject.Inject;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;
import de.is24.nexus.yum.AbstractRepositoryTester;
import de.is24.nexus.yum.plugin.RepositoryRegistry;
import de.is24.nexus.yum.service.RepositoryRpmManager;


public class DefaultRepositoryRegistryTest extends AbstractRepositoryTester {
  private static final String REPO_ID = "rpm-snapshots";
  private static final String REPOSITORY_RPM_FILENAME = "is24-rel-" + REPO_ID + "-1.3-repo-1-1.noarch.rpm";

  @Inject
  private RepositoryRegistry repositoryRegistry;

  @Inject
  private RepositoryRpmManager repositoryRpmManager;

  @Test
  public void shouldScanRepository() throws Exception {
    MavenRepository repository = createMock(MavenRepository.class);
    expect(repository.getId()).andReturn(REPO_ID).anyTimes();
    expect(repository.getLocalUrl()).andReturn(new File(".", "target/test-classes/repo").toURI().toString()).anyTimes();
    replay(repository);

    repositoryRegistry.registerRepository(repository);
    Thread.sleep(5000);
    Assert.assertNotNull(repositoryRegistry.findRepositoryForId(REPO_ID));
    assertThat(repositoryRpmManager.getYumRepository().getFile(REPOSITORY_RPM_FILENAME), exists());
  }

  @Test
  public void shouldUnregisterRepository() throws Exception {
    MavenRepository repository = createRepository(true);
    repositoryRegistry.registerRepository(repository);
    Assert.assertTrue(repositoryRegistry.isRegistered(repository));
    repositoryRegistry.unregisterRepository(repository);
    Assert.assertFalse(repositoryRegistry.isRegistered(repository));
  }

  @SuppressWarnings("serial")
  public static class QueueingEventListener extends ArrayList<Event<?>> implements EventListener {
    @Override
    public void onEvent(Event<?> evt) {
      add(evt);
    }

  }
}
