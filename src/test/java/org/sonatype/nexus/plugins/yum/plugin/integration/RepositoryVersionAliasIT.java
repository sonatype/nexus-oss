package org.sonatype.nexus.plugins.yum.plugin.integration;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;


public class RepositoryVersionAliasIT extends AbstractNexusTestBase {
  private static final String VERSION_1 = "1.0.0-SNAPSHOT";
  private static final String VERSION_2 = "1.0.1-SNAPSHOT";

  @Test
  public void shouldRetrieveAlias() throws Exception {
    assertEquals("5.1.15-1", executeGet("/yum/alias/releases/production"));
  }

  @Test
  public void shouldRetriveRpmForAlias() throws Exception {
    String content = executeGet("/yum/alias/releases/production.rpm");
    assertThat(content.length(), greaterThan(1000));
  }

  @Test
  public void shouldSetAlias() throws Exception {
    HttpResponse response = executePost("/yum/alias/snapshots/testAlias", new StringEntity(VERSION_1));
    assertEquals(VERSION_1, EntityUtils.toString(response.getEntity()));
    assertEquals(VERSION_1, executeGet("/yum/alias/snapshots/testAlias"));
    response = executePost("/yum/alias/snapshots/testAlias", new StringEntity(VERSION_2));
    assertEquals(VERSION_2, EntityUtils.toString(response.getEntity()));
    assertEquals(VERSION_2, executeGet("/yum/alias/snapshots/testAlias"));
  }

}
