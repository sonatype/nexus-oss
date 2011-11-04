package de.is24.nexus.yum.plugin.impl;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import de.is24.nexus.yum.AbstractRepositoryTester;
import de.is24.nexus.yum.guice.NexusTestRunner;
import de.is24.nexus.yum.plugin.RepositoryRegistry;
import de.is24.nexus.yum.repository.utils.RepositoryTestUtils;
import de.is24.nexus.yum.service.YumService;
import de.is24.nexus.yum.service.impl.YumRepositoryCreatorService;


/**
 * Created by IntelliJ IDEA.
 * User: BVoss
 * Date: 01.08.11
 * Time: 17:32
 * To change this template use File | Settings | File Templates.
 */
@RunWith(NexusTestRunner.class)
public class ConcurrentRpmDeployedListenerTest extends AbstractRepositoryTester {
  private static final Logger log = LoggerFactory.getLogger(ConcurrentRpmDeployedListenerTest.class);

  @Rule
  public ConcurrentRule concurrently = new ConcurrentRule();

  @Rule
  public RepeatingRule repeatedly = new RepeatingRule();

  @Inject
  private RpmDeployedListener listener;

  @Inject
  @Named(RepositoryRegistry.DEFAULT_BEAN_NAME)
  private RepositoryRegistry repositoryRegistry;

  @Inject
  @Named(YumService.DEFAULT_BEAN_NAME)
  private YumService yumService;

  @Inject
  private YumRepositoryCreatorService yumRepositoryCreatorService;

  @Before
  public void activateRepo() {
    yumService.activate();
  }

  @After
  public void reactivateRepo() {
    yumService.activate();
  }

  @Concurrent(count = 1)
  @Test
  public void shouldCreateRepoForPom() throws Exception {
    for (int j = 0; j < 5; j++) {
      shouldCreateRepoForRpm(j);
    }
    log.info("done");
  }

  private void shouldCreateRepoForRpm(int index) throws URISyntaxException, MalformedURLException,
    NoSuchAlgorithmException, IOException {
    MavenRepository repo = createRepository(true, "repo" + index);
    repositoryRegistry.registerRepository(repo);
    for (int version = 0; version < 5; version++) {
      assertNotMoreThan10ThreadForRpmUpload(repo, version);
    }
  }

  private void assertNotMoreThan10ThreadForRpmUpload(MavenRepository repo, int version) throws URISyntaxException,
    MalformedURLException, NoSuchAlgorithmException, IOException {
    String versionStr = version + ".1";
    File outputDirectory = new File(new URL(repo.getLocalUrl() + "/blalu/" +
        versionStr).toURI());
    File rpmFile = RepositoryTestUtils.createDummyRpm("test-artifact", versionStr, outputDirectory);

    StorageItem storageItem = createItem(versionStr, rpmFile.getName());

    listener.onEvent(new RepositoryItemEventStore(repo, storageItem));

    final int activeWorker = yumRepositoryCreatorService.getActiveWorkerCount();
    log.info("active worker: " + activeWorker + " size: " + yumRepositoryCreatorService.size());
    assertThat(activeWorker, is(lessThanOrEqualTo(10)));
  }
}
