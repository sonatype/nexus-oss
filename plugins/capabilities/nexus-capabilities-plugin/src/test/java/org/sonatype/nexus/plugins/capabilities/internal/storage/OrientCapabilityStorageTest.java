package org.sonatype.nexus.plugins.capabilities.internal.storage;

import java.io.File;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.After;
import org.junit.Before;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link OrientCapabilityStorage}.
 */
public class OrientCapabilityStorageTest
  extends TestSupport
{
  private OrientCapabilityStorage underTest;

  @Before
  public void setUp() throws Exception {
    ApplicationDirectories applicationDirectories = mock(ApplicationDirectories.class);
    File dir = util.createTempDir("testdb");
    when(applicationDirectories.getWorkDirectory(OrientCapabilityStorage.DB_LOCATION)).thenReturn(dir);

    this.underTest = new OrientCapabilityStorage(applicationDirectories);
    underTest.start();
  }

  @After
  public void tearDown() throws Exception {
    if (underTest != null) {
      underTest.stop();
    }
  }
}
