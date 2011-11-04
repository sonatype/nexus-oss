package de.is24.nexus.yum.service.impl;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;
import java.io.File;
import java.net.URL;
import javax.inject.Inject;
import javax.inject.Named;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import de.is24.nexus.yum.guice.NexusTestRunner;
import de.is24.nexus.yum.plugin.RepositoryRegistry;
import de.is24.nexus.yum.repository.YumRepository;
import de.is24.nexus.yum.service.YumService;


@RunWith(NexusTestRunner.class)
public class DefaultYumServiceTest {
  private static final String REPO_BASE_URL = "http://localhost:8081/nexus/service/local/snapshots/1.0";
  private static final String VERSION_1_0 = "1.0";
  private static final String SNAPSHOTS = "snapshots";

  @Inject
  @Named(YumService.DEFAULT_BEAN_NAME)
  private YumService yumService;

  @Inject
  @Named(RepositoryRegistry.DEFAULT_BEAN_NAME)
  private RepositoryRegistry repositoryRegistry;

  @Before
  public void activateService() {
    yumService.activate();
  }

  @Test
  public void shouldCacheRepository() throws Exception {
    YumRepository repo1 = yumService.getRepository(createRepository(SNAPSHOTS), VERSION_1_0, new URL(REPO_BASE_URL));
    YumRepository repo2 = yumService.getRepository(createRepository(SNAPSHOTS), VERSION_1_0, new URL(REPO_BASE_URL));
    Assert.assertEquals(repo1, repo2);
  }

  @Test
  public void shouldRecreateRepository() throws Exception {
    YumRepository repo1 = yumService.getRepository(createRepository(SNAPSHOTS), VERSION_1_0, new URL(REPO_BASE_URL));

    yumService.markDirty(createRepository(SNAPSHOTS), VERSION_1_0);

    YumRepository repo2 = yumService.getRepository(createRepository(SNAPSHOTS), VERSION_1_0, new URL(REPO_BASE_URL));

    assertNotSame(repo1, repo2);
  }

  @Test
  public void shouldNotCreateYumRepo() throws Exception {
    yumService.deactivate();
    assertNull(yumService.createYumRepository(createRepository(SNAPSHOTS)));
  }

  @Test
  public void shouldNotFindRepository() throws Exception {
    assertNull(repositoryRegistry.findRepositoryForId("blablup"));
  }

  @Test
  public void shouldFindRepository() throws Exception {
    repositoryRegistry.registerRepository(createRepository(SNAPSHOTS));
    assertNotNull(repositoryRegistry.findRepositoryForId(SNAPSHOTS));
  }

  public static MavenRepository createRepository(String id) {
    MavenRepository repo = createMock(MavenRepository.class);
    expect(repo.getId()).andReturn(id).anyTimes();
    expect(repo.getLocalUrl()).andReturn(getTempUrl()).anyTimes();
    replay(repo);
    return repo;
  }

  private static String getTempUrl() {
    return new File(System.getProperty("java.io.tmpdir")).toURI().toString();
  }
}
