package de.is24.nexus.yum.rest;

import javax.inject.Inject;
import javax.inject.Named;
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
import de.is24.nexus.yum.service.YumService;


/**
 * Resource to deactivate Yum Repository Processing
 * http://localhost:8081/nexus/service/local/yumServer/deactivate
 */
public class DeactivationResource extends AbstractPlexusResource implements PlexusResource {
  private static final String DEACTIVATION_CODE = "2HO_K_yoEtN8Rn9J";

  @Inject
  @Named(YumService.DEFAULT_BEAN_NAME)
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
    return "/yumServer/deactivate";
  }

  @Override
  public Object get(Context context, Request request, Response response,
    Variant variant) throws ResourceException {
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
