package org.sonatype.nexus.plugins.yum.plugin.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import org.junit.Test;


public class VersionizedRepositoryViewIT extends AbstractNexusTestBase {
  static final String SERVICE_BASE_URL = "http://localhost:8080/nexus/service/local";

  @Test
  public void shouldGetEmpytViewForRepository() throws Exception {
    assertThat(executeGet("/yum/repos/snapshots/1.0.0-SNAPSHOT/"),
      containsString("<a href=\"repodata/\">repodata/</a>"));
  }
}
