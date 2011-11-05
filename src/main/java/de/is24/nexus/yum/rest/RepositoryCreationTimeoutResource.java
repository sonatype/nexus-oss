package de.is24.nexus.yum.rest;

import static java.lang.Integer.parseInt;
import static org.restlet.data.MediaType.TEXT_PLAIN;
import static org.restlet.data.Method.POST;
import javax.inject.Inject;
import javax.inject.Named;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import de.is24.nexus.yum.service.RepositoryCreationTimeoutHolder;


public class RepositoryCreationTimeoutResource extends AbstractPlexusResource implements PlexusResource {
  @Inject
  @Named(RepositoryCreationTimeoutHolder.DEFAULT_BEAN_NAME)
  private RepositoryCreationTimeoutHolder timeoutHolder;

  @Override
  public Object getPayloadInstance() {
    return null;
  }

  @Override
  public Object getPayloadInstance(Method method) {
    if (POST.equals(method)) {
      return new String();
    }
    return null;
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor(this.getResourceUri(), "anon");
  }

  @Override
  public String getResourceUri() {
    // note this must start with a '/'
    return "/yum/config/timeout";
  }

  @Override
  public Object get(Context context, Request request, Response response, Variant variant) throws ResourceException {
    return new StringRepresentation(Integer.toString(timeoutHolder.getRepositoryCreationTimeout()), TEXT_PLAIN);
  }

  @Override
  public Object post(Context context, Request request, Response response, Object payload) throws ResourceException {
    if ((payload == null) || !String.class.isAssignableFrom(payload.getClass())) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please provide a valid timeout in seconds.");
    }

    try {
      timeoutHolder.setRepositoryCreationTimeout(parseInt(payload.toString()));
    } catch (NumberFormatException e) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please provide a valid timeout in seconds.");
    }

    return new StringRepresentation(payload.toString(), TEXT_PLAIN);
  }
}
