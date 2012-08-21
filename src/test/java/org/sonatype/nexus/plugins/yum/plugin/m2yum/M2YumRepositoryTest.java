package org.sonatype.nexus.plugins.yum.plugin.m2yum;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.sonatype.nexus.plugins.yum.plugin.m2yum.M2YumRepository;


@RunWith(Parameterized.class)
public class M2YumRepositoryTest {
  private M2YumRepository repository;

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
      new Object[][] {
        { "/repodata/repomd.xml", true },
        { "/repodata/primary.xml.gz", true },
        { "/maven-metadata.xml", true },
        { "/de/testproject/maven-metadata.xml", true },
        { "/de/testproject/test.rpm", false },
        { "/de/testproject/repodata/repomd.xml", false }
      });
  }

  private final String path;
  private final boolean expectedMetaData;

  public M2YumRepositoryTest(final String path, final Boolean isExpectedMetaData) {
    this.path = path;
    this.expectedMetaData = isExpectedMetaData;
  }

  @Before
  public void init() {
    repository = new M2YumRepository();
  }

  @Test
  public void should_mark_files_as_metadata() {
    Assert.assertEquals("path: " + path + " has not the expected value: " + expectedMetaData, expectedMetaData,
      repository.isMavenMetadataPath(path));

  }

}
