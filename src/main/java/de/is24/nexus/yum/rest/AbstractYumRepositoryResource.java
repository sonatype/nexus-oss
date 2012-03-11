package de.is24.nexus.yum.rest;

import javax.inject.Inject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import de.is24.nexus.yum.repository.FileDirectoryStructure;
import de.is24.nexus.yum.rest.domain.IndexRepresentation;
import de.is24.nexus.yum.rest.domain.UrlPathInterpretation;
import de.is24.nexus.yum.rest.domain.YumFileRepresentation;


public abstract class AbstractYumRepositoryResource extends AbstractPlexusResource {
  private UrlPathParser requestSegmentInterpetor;

  private GlobalRestApiSettings restApiSettings;

  @Inject
  public void initialize(GlobalRestApiSettings restApiSettings) {
    this.restApiSettings = restApiSettings;
    this.requestSegmentInterpetor = new UrlPathParser(getVersionedRepositoryBaseUrl(), getUrlPrefixName(),
      getSegmentCountAfterPrefix());
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor("/" + getUrlPrefixName() + "/*", "anon");
  }

  @Override
  public Object getPayloadInstance() {
    // if you allow PUT or POST you would need to return your object.
    return null;
  }

  @Override
  public Object get(Context context, Request request, Response response, Variant variant) throws ResourceException {
    try {
      UrlPathInterpretation interpretation = requestSegmentInterpetor.interprete(request);

      if (interpretation.isRedirect()) {
        response.redirectPermanent(interpretation.getRedirectUri());
        return null;
      }

      return createRepresentation(interpretation, getFileStructure(request, interpretation));
    } catch (ResourceException e) {
      throw e;
    } catch (Exception e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
    }
  }

  private String getVersionedRepositoryBaseUrl() {
    return restApiSettings.getBaseUrl() + "/service/local";
  }

  private Representation createRepresentation(UrlPathInterpretation interpretation,
    FileDirectoryStructure fileDirectoryStructure) {
    return interpretation.isIndex() ? new IndexRepresentation(interpretation, fileDirectoryStructure)
                                    : new YumFileRepresentation(
        interpretation, fileDirectoryStructure);
  }

  protected abstract String getUrlPrefixName();

  protected abstract FileDirectoryStructure getFileStructure(Request request, UrlPathInterpretation interpretation)
    throws Exception;

  protected abstract int getSegmentCountAfterPrefix();
}
