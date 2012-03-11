package de.is24.nexus.yum.plugin.impl;

import static de.is24.test.reflection.ReflectionTestUtils.setField;
import static java.lang.Thread.sleep;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;

import de.is24.nexus.yum.config.YumConfiguration;
import de.is24.nexus.yum.repository.service.YumService;

public class DefaultDeletionServiceTest {

  private static final String BASE_PATH = "/base/path";
  private static final String SUB_PATH1 = BASE_PATH + "/subdir/foo.rpm";
  private static final String SUB_PATH2 = BASE_PATH + "/subdir/bar.rpm";
  private static final String SUB_PATH3 = BASE_PATH + "/otherdir/test.rpm";
  private static final long TIMEOUT_IN_SEC = 1;
  private static final String REPO_ID = "snapshots";

  private DefaultDeletionService service;
  private YumService yumService;
  private Repository repository;

  @Before
  public void prepareService() {
    yumService = mock(YumService.class);
    service = new DefaultDeletionService();
    setField(service, "yumService", yumService);
    YumConfiguration config = mock(YumConfiguration.class);
    when(config.isDeleteProcessing()).thenReturn(true);
    when(config.getDelayAfterDeletion()).thenReturn(TIMEOUT_IN_SEC);
    setField(service, "configuration", config);
    repository = mock(Repository.class);
    when(repository.getId()).thenReturn(REPO_ID);
  }

  @Test
  public void shouldNotRegenerateRepositoryWithoutRpms() throws Exception {
    service.deleteDirectory(repository, BASE_PATH);
    sleep(TIMEOUT_IN_SEC * 2000);
    verify(yumService, times(0)).recreateRepository(repository);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void shouldRegenerateRepositoryWithRpm() throws Exception {
    when(repository.retrieveItem(any(ResourceStoreRequest.class))).thenThrow(new ItemNotFoundException("Cant retrieve file"));
    service.deleteDirectory(repository, BASE_PATH);
    service.deleteRpm(repository, SUB_PATH1);
    sleep(TIMEOUT_IN_SEC * 2000);
    verify(yumService, times(1)).recreateRepository(repository);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void shouldRegenerateRepositoryWithRpms() throws Exception {
    when(repository.retrieveItem(any(ResourceStoreRequest.class))).thenThrow(new ItemNotFoundException("Cant retrieve file"));
    service.deleteDirectory(repository, BASE_PATH);
    service.deleteRpm(repository, SUB_PATH1);
    service.deleteRpm(repository, SUB_PATH2);
    service.deleteRpm(repository, SUB_PATH3);
    sleep(TIMEOUT_IN_SEC * 2000);
    verify(yumService, times(1)).recreateRepository(repository);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void shouldWaitUntilDirIsDeleted() throws Exception {
    when(repository.retrieveItem(any(ResourceStoreRequest.class))).thenReturn(null).thenReturn(null)
        .thenThrow(new ItemNotFoundException("Cant retrieve file"));
    service.deleteDirectory(repository, BASE_PATH);
    service.deleteRpm(repository, SUB_PATH1);
    sleep(TIMEOUT_IN_SEC * 1500);
    verify(yumService, times(0)).recreateRepository(repository);
    sleep(TIMEOUT_IN_SEC * 2500);
    verify(yumService, times(1)).recreateRepository(repository);
  }

  @Test
  public void shouldRegenerateRepositoryAfterDeletionSingleRpm() throws Exception {
    service.deleteRpm(repository, SUB_PATH1);
    verify(yumService, times(1)).recreateRepository(repository);
  }
}
