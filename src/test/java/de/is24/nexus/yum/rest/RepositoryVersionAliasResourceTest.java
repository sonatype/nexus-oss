/**
 *
 */
package de.is24.nexus.yum.rest;

import static de.is24.nexus.yum.rest.RepositoryVersionAliasResource.RESOURCE_URI;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.restlet.data.Method.GET;
import static org.restlet.data.Method.POST;
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST;
import javax.inject.Inject;
import de.is24.nexus.yum.guice.NexusTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;


/**
 * @author BVoss
 *
 */
@RunWith(NexusTestRunner.class)
public class RepositoryVersionAliasResourceTest {
  private static final String EXISTING_VERSION = "trunk";
  private static final String RELEASES = "releases";
  private static final String TRUNK_VERSION = "5.1.15-2";
  private static final String NOT_EXISTING_REPOSITORY = "blablup-repo";
  private static final String VERSION_TO_CREATE = "new-version";
  private static final String ALIAS_TO_CREATE = "alias-to-create";
  @Inject
  private RepositoryVersionAliasResource resource;

  @Test
  public void requestedAliasNotfound() throws Exception {
    assert404(new Request());
  }

  @Test
  public void requestedAliasNotfoundNoParameters() throws Exception {
    assert404(createRequest(RELEASES, "bla"));
  }

  @Test
  public void requestedAliasReturnedRpmFile() throws Exception {
    final Request request = createRequest(RELEASES, "trunk.rpm");
    final FileRepresentation rpmFile = (FileRepresentation) resource.get(null, request, null, null);
    assertEquals("application/x-rpm", rpmFile.getMediaType().getName());
    assertTrue(rpmFile.getFile().getName().contains(TRUNK_VERSION));
  }

  @Test
  public void requestedAliasReturnedVersionString() throws Exception {
    final Request request = createRequest(RELEASES, EXISTING_VERSION);
    final StringRepresentation version = (StringRepresentation) resource.get(null, request, null, null);
    assertEquals(TRUNK_VERSION, version.getText());
  }

  @Test
  public void shouldReturn404ForInvalidRepository() throws Exception {
    assert404(createRequest(NOT_EXISTING_REPOSITORY, EXISTING_VERSION));
  }

  @Test
  public void shouldReturn404ForInvalidRepositoryRpm() throws Exception {
    assert404(createRequest(NOT_EXISTING_REPOSITORY, EXISTING_VERSION + ".rpm"));
  }

  @Test
  public void shouldRetrieveRestRequirements() throws Exception {
    assertThat(resource.getResourceUri(), is(RESOURCE_URI));
    assertThat(resource.getPayloadInstance(), nullValue());
    assertThat(resource.getPayloadInstance(GET), nullValue());
    assertThat(resource.getPayloadInstance(POST), instanceOf(String.class));
    assertThat(resource.getResourceProtection().getPathPattern(), is("/yum-alias/*"));
    assertThat(resource.getResourceProtection().getFilterExpression(), is("anon"));
  }

  @Test
  public void shouldRejectEmptyPayload() throws Exception {
    check400ForPayload(null);
  }

  @Test
  public void shouldRejectObjectPayload() throws Exception {
    check400ForPayload(new Object());
  }

  @Test
  public void shouldSetVersion() throws Exception {
    final Request request = createRequest(NOT_EXISTING_REPOSITORY, ALIAS_TO_CREATE);
    StringRepresentation result = (StringRepresentation) resource.post(null, request, null, VERSION_TO_CREATE);
    assertThat(result.getText(), is(VERSION_TO_CREATE));
    result = (StringRepresentation) resource.get(null, request, null, null);
    assertThat(result.getText(), is(VERSION_TO_CREATE));
  }

  private void check400ForPayload(Object payload) {
    try {
      resource.post(null, createRequest(NOT_EXISTING_REPOSITORY, RELEASES), null, payload);
      fail();
    } catch (ResourceException e) {
      assertThat(e.getStatus(), is(CLIENT_ERROR_BAD_REQUEST));
    }
  }

  private void assert404(final Request request) {
    try {
      resource.get(null, request, null, null);
      fail(ResourceException.class + " expected");
    } catch (ResourceException e) {
      assertEquals(Status.CLIENT_ERROR_NOT_FOUND, e.getStatus());
    }
  }

  private Request createRequest(final String repoValue, final String aliasValue) {
    final Request request = new Request();
    request.getAttributes().put(RepositoryVersionAliasResource.REPOSITORY_ID_PARAM, repoValue);
    request.getAttributes().put(RepositoryVersionAliasResource.ALIAS_PARAM, aliasValue);
    return request;
  }

}
