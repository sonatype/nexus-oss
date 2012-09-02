package org.sonatype.nexus.plugins.yum.plugin;

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
    return configuration.addPlugins(artifactResolver().resolvePluginFromDependencyManagement("org.sonatype.nexus.plugins",
        "nexus-yum-plugin"));
  }


}
