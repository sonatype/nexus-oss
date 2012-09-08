package org.sonatype.nexus.plugins.yum.plugin;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.testsuite.support.NexusRunningITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

@NexusStartAndStopStrategy(NexusStartAndStopStrategy.Strategy.EACH_TEST)
public class GroupRepositoryIT extends NexusRunningITSupport {

  private static final String TEST_REPO_NAME = "dummy-group-repo";

  @Test
  public void shouldCreateAGroupRepository() throws Exception {
    RepositoryTestService service = new RepositoryTestService(client());
    service.deleteGroupRepo(TEST_REPO_NAME);
    assertThat(service.createGroupRepo(TEST_REPO_NAME, "maven2yum"), is(true));
  }

  @Override
  protected NexusBundleConfiguration configureNexus(NexusBundleConfiguration configuration) {
    URL pluginFileUrl = getClass().getResource("/plugin.zip");
    if (pluginFileUrl == null) {
      throw new IllegalStateException("Couldn't find /plugin.zip in classpath");
    }
    try {
      return configuration.addPlugins(new File(pluginFileUrl.toURI()));
    } catch (URISyntaxException e) {
      throw new RuntimeException("Could not determine plugin bundle URI.", e);
    }
  }

}
