package org.sonatype.nexus.plugins.yum.plugin.integration;

import org.junit.Test;

public class CreateGroupRepositoryIT extends AbstractNexusTestBase {

  @Test
  public void shouldCreateAGroupRepository() throws Exception {
    givenGroupRepository("dummy-group-repo", "maven2yum");
  }
}
