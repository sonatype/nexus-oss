package org.sonatype.nexus.plugins.yum.repository;

import static org.sonatype.nexus.plugins.yum.repository.RepositoryUtils.getBaseDir;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.sonatype.nexus.proxy.repository.Repository;


public class RepositoryUtilsTest {
  @Test
  public void shouldConvertRepositoryLocalUrlToFile() throws Exception {
    assertConvertion("file:/hallo/world", "/hallo/world");
    assertConvertion("/hallo/world", "/hallo/world");
  }

  private void assertConvertion(String localUrl, String expected) throws Exception {
    Repository repo = createMock(Repository.class);
    expect(repo.getLocalUrl()).andReturn(localUrl).anyTimes();
    replay(repo);
    assertThat(getBaseDir(repo).getAbsolutePath(), is(expected));
  }
}
