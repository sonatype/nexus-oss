package org.sonatype.nexus.plugins.yum.plugin;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.testsuite.support.NexusRunningITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

@NexusStartAndStopStrategy(NexusStartAndStopStrategy.Strategy.EACH_TEST)
public class GroupRepositoryIT extends NexusRunningITSupport {

  @Test
  public void shouldCreateAGroupRepository() throws Exception {

  }

  @Override
  protected NexusBundleConfiguration configureNexus(NexusBundleConfiguration configuration) {
    URI pluginFileURI;
    try {
      pluginFileURI = getClass().getResource("/plugin.zip").toURI();
    } catch (URISyntaxException e) {
      throw new RuntimeException("Could not determine plugin URI.", e);
    }
    return configuration.addPlugins(new File(pluginFileURI));
  }

}
