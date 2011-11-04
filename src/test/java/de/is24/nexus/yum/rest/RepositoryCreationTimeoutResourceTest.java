package de.is24.nexus.yum.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.restlet.data.Method.GET;
import static org.restlet.data.Method.POST;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.restlet.data.Request;
import org.restlet.resource.Representation;
import de.is24.nexus.yum.guice.NexusTestRunner;
import de.is24.nexus.yum.service.RepositoryCreationTimeoutHolder;


@RunWith(NexusTestRunner.class)
public class RepositoryCreationTimeoutResourceTest {
  private static final String TIMEOUT_CONFIG_URL = "http://localhost:8081/nexus/service/local/yumServer/timeout";

  @Inject
  private RepositoryCreationTimeoutResource resource;

  @Inject
  private RepositoryCreationTimeoutHolder holder;

  private int oldTimeout;

  @Test
  public void shouldPrvoideCurrentTimeout() throws Exception {
    Representation representation = (Representation) resource.get(null, new Request(GET, TIMEOUT_CONFIG_URL), null,
      null);
    assertThat(representation.getText(), is("150"));
  }

  @Test
  public void shouldSetTimeout() throws Exception {
    String newTimeoutStr = Integer.toString(oldTimeout + 200);
    Representation representation = (Representation) resource.post(null, new Request(POST, TIMEOUT_CONFIG_URL), null,
      newTimeoutStr);
    assertThat(representation.getText(), is(newTimeoutStr));

    representation = (Representation) resource.get(null, new Request(GET, TIMEOUT_CONFIG_URL), null, null);
    assertThat(representation.getText(), is(newTimeoutStr));
  }

  @Before
  public void loadTimeout() {
    oldTimeout = holder.getRepositoryCreationTimeout();
  }

  @After
  public void saveTimeout() {
    holder.setRepositoryCreationTimeout(oldTimeout);
  }
}
