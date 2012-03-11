package de.is24.nexus.yum.config.rest;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import de.is24.nexus.yum.repository.service.YumService;


/**
 * Resource to deactivate Yum Repository Processing
 * http://localhost:8081/nexus/service/local/yumServer/deactivate
 */
@Component(role = PlexusResource.class, hint = "DeactivationResource")
@Path(DeactivationResource.RESOURCE_URI)
@Produces({ "application/xml", "application/json" })
public class DeactivationResource extends AbstractPlexusResource implements PlexusResource {
  public static final String RESOURCE_URI = "/yum/config/deactivate";
  private static final String DEACTIVATION_CODE = "2HO_K_yoEtN8Rn9J";

  @Inject
  private YumService yumService;

  @Override
  public Object getPayloadInstance() {
    // if you allow PUT or POST you would need to return your object.
    return null;
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    // to be controled by a new prermission
    // return new PathProtectionDescriptor( this.getResourceUri(),
    // "authcBasic,perms[nexus:somepermission]" );

    // for an anonymous resoruce
    return new PathProtectionDescriptor(this.getResourceUri(), "anon");
  }

  @Override
  public String getResourceUri() {
    // note this must start with a '/'
    return RESOURCE_URI;
  }

  @Override
  public Object get(Context context, Request request, Response response,
    Variant variant) throws ResourceException {
    return !yumService.isActive();
  }

  @Override
  public Object post(Context context, Request request, Response response, Object payload) throws ResourceException {
    Form form = request.getResourceRef().getQueryAsForm();
    Parameter param = form.getFirst("code");

    if ((param != null) && DEACTIVATION_CODE.equals(param.getValue())) {
      yumService.deactivate();
      return true;
    }

    throw new ResourceException(
      Status.CLIENT_ERROR_FORBIDDEN,
      "Please use '/yumServer/deactivate?code=&lt;DEACTIVATION_CODE&gt;' to deactivate the service");
  }
}
