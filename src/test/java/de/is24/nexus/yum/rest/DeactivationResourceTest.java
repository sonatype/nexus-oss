package de.is24.nexus.yum.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.restlet.data.Method.GET;
import static org.restlet.data.Method.POST;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import de.is24.nexus.yum.guice.NexusTestRunner;
import de.is24.nexus.yum.service.YumService;


@RunWith(NexusTestRunner.class)
public class DeactivationResourceTest {
  @Inject
  @Named(YumService.DEFAULT_BEAN_NAME)
  private YumService yumService;

  @Inject
  private DeactivationResource resource;

  @After
  public void reactivate() {
    yumService.activate();
  }

  @Test
  public void shouldReturnUriAndNoSecurity() throws Exception {
    assertNull(resource.getPayloadInstance());
    assertEquals("/yum/config/deactivate", resource.getResourceUri());
    assertEquals("/yum/config/deactivate", resource.getResourceProtection().getPathPattern());
    assertEquals("anon", resource.getResourceProtection().getFilterExpression());
  }

  @Test(expected = ResourceException.class)
  public void shouldThrowExceptionForNoCode() throws Exception {
    resource.post(null, new Request(POST, "http://localhost:8081/nexus/service/local/yum/config/deactivate"), null,
      null);
  }

  @Test(expected = ResourceException.class)
  public void shouldThrowExceptionForWrongCode() throws Exception {
    resource.post(null, new Request(POST, "http://localhost:8081/nexus/service/local/yum/config/deactivate?code=bla"),
      null, null);
  }

  @Test
  public void shouldDeactivateService() throws Exception {
    resource.post(null,
      new Request(POST, "http://localhost:8081/nexus/service/local/yum/config/deactivate?code=2HO_K_yoEtN8Rn9J"),
      null, null);
    assertEquals(true,
      resource.get(null, new Request(GET, "http://localhost:8081/nexus/service/local/yum/config/deactivate"), null,
        null));
  }
}
