package de.is24.nexus.yum.config.rest;

import static org.restlet.data.Method.GET;
import static org.restlet.data.Method.POST;
import org.codehaus.plexus.component.annotations.Requirement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.sonatype.plexus.rest.resource.PlexusResource;
import de.is24.nexus.yum.AbstractYumNexusTestCase;
import de.is24.nexus.yum.service.YumService;


public class DeactivationResourceTest extends AbstractYumNexusTestCase {
  @Requirement
  private YumService yumService;

  @Requirement(hint = "DeactivationResource")
  private PlexusResource resource;

  @After
  public void reactivate() {
    yumService.activate();
  }

  @Test
  public void shouldReturnUriAndNoSecurity() throws Exception {
    Assert.assertNull(resource.getPayloadInstance());
    Assert.assertEquals("/yum/config/deactivate", resource.getResourceUri());
    Assert.assertEquals("/yum/config/deactivate", resource.getResourceProtection().getPathPattern());
    Assert.assertEquals("anon", resource.getResourceProtection().getFilterExpression());
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
    Assert.assertEquals(true,
      resource.get(null, new Request(GET, "http://localhost:8081/nexus/service/local/yum/config/deactivate"), null,
        null));
  }
}
