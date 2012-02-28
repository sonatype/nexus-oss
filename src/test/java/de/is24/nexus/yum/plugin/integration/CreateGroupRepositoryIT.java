package de.is24.nexus.yum.plugin.integration;

import static java.lang.System.currentTimeMillis;
import static org.apache.http.util.EntityUtils.consume;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


import org.apache.http.HttpResponse;
import org.junit.Test;


public class CreateGroupRepositoryIT extends AbstractNexusTestBase {
  @Test
  public void shouldCreateAGroupRepository() throws Exception {
    HttpResponse response = givenGroupRepository("dummy-group-repo-" + currentTimeMillis());
    consume(response.getEntity());
    assertThat(response.getStatusLine().getStatusCode(), is(201));
  }

}
