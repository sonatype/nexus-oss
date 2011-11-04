package de.is24.nexus.yum.repository;

import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.BASE_CACHE_DIR;
import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.PACKAGE_CACHE_DIR;
import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.REPODATA_DIR;
import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.RPM_BASE_FILE;
import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.TARGET_DIR;
import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.assertRepository;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.junit.Assert.assertFalse;
import java.io.File;
import org.junit.Before;
import org.junit.Test;


public class YumRepositoryGeneratorJobTest {
  private static final File PATH_NOT_EXISTS = new File("/data/path/not/exists");
  private static final String SNAPSHOTS = "snapshots";
  private static final String VERSION = "2.2-2";
  private static final String BASE_URL = "http://localhost:8080/nexus/content/snapshots";
  private static final String BASE_VERSIONED_URL = "http://localhost:8080/nexus/service/local/yum/snapshots/" + VERSION;

  @Before
  public void removeRepoDataDir() throws Exception {
    deleteDirectory(PACKAGE_CACHE_DIR);
    deleteDirectory(REPODATA_DIR);
    YumRepositoryGeneratorJob.activate();
  }

  @Test
  public void shouldCreateRepo() throws Exception {
    new YumRepositoryGeneratorJob(new YumDefaultGeneratorConfiguration(RPM_BASE_FILE, BASE_URL, TARGET_DIR, SNAPSHOTS,
        BASE_CACHE_DIR)).call();
    assertRepository(REPODATA_DIR, "default");

  }

  @Test
  public void shouldNotExecuteCreateRepoIfDeactivated() throws Exception {
    YumRepositoryGeneratorJob.deactivate();
    new YumRepositoryGeneratorJob(new YumDefaultGeneratorConfiguration(RPM_BASE_FILE, BASE_URL, TARGET_DIR, SNAPSHOTS,
        BASE_CACHE_DIR)).call();
    assertFalse(REPODATA_DIR.exists());
  }

  @Test
  public void shouldFilterForSpecificVersion() throws Exception {
    new YumRepositoryGeneratorJob(new YumDefaultGeneratorConfiguration(RPM_BASE_FILE, BASE_URL, TARGET_DIR,
        BASE_VERSIONED_URL, SNAPSHOTS,
        VERSION, BASE_CACHE_DIR, null, true)).call();
    assertRepository(REPODATA_DIR, "filtering");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotCreateRepoIfPathNotExists() throws Exception {
    new YumRepositoryGeneratorJob(new YumDefaultGeneratorConfiguration(PATH_NOT_EXISTS, BASE_URL, TARGET_DIR, SNAPSHOTS,
        BASE_CACHE_DIR)).call();
  }

}
