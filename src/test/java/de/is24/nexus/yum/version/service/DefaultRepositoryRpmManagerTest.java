package de.is24.nexus.yum.version.service;

import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.REPOSITORY_RPM_CACHE_DIR;
import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.assertRepository;
import static de.is24.test.hamcrest.FileMatchers.exists;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.is24.nexus.yum.AbstractRepositoryTester;
import de.is24.nexus.yum.config.YumConfiguration;
import de.is24.nexus.yum.version.service.RepositoryRpmManager;


public class DefaultRepositoryRpmManagerTest extends AbstractRepositoryTester {
  private static final String FILE_PATH = "is24-rel-snapshots-0.1.2-snapshot-repo-1-1.noarch.rpm";

  @Inject
  private RepositoryRpmManager rpmManager;

  @Inject
  private YumConfiguration configHandler;

  @Before
  public void setActive() {
    configHandler.setRepositoryOfRepositoryVersionsActive(true);
  }

  @Test
  public void shouldUpdateRepository() throws Exception {
    File rpmFile = new File(REPOSITORY_RPM_CACHE_DIR, FILE_PATH);
    rpmFile.delete();

    rpmManager.updateRepository("snapshots", "0.1.2-SNAPSHOT");
    assertThat(rpmFile, exists());
    assertThat(rpmManager.getYumRepository(), notNullValue());
    assertRepository(new File(REPOSITORY_RPM_CACHE_DIR, "repodata"), "yum-repos");
  }

  @Test
  public void shouldNotHaveRepoForNoRpms() throws Exception {
    rpmManager.getYumRepository();
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionIfDeactivated() throws Exception {
    configHandler.setRepositoryOfRepositoryVersionsActive(false);
    rpmManager.updateRepository("dummy-repo", "any-version");
    Assert.fail("should throw an exeption before this line.");
  }
}
