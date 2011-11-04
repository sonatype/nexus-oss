package de.is24.nexus.yum.plugin.integration;

import static java.lang.System.currentTimeMillis;
import static org.apache.http.util.EntityUtils.consume;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.junit.Test;


public class CreateGroupRepositoryIT extends AbstractNexusTestBase {
  @Test
  public void shouldCreateAGroupRepository() throws Exception {
    HttpResponse response = executePost("/repo_groups",
      new StringEntity(
        String.format(
          "{data:{id: 'dummy-group-repo-%d', name: 'dummy-group-repo-%d', provider: 'maven2', repositories: []}}",
          currentTimeMillis(),
          currentTimeMillis())),
      new BasicHeader(
        "Content-Type", "application/json"));
    consume(response.getEntity());
    assertThat(response.getStatusLine().getStatusCode(), is(201));
  }

}
