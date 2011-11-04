package de.is24.nexus.yum.rest;

import static de.is24.nexus.yum.service.RepositoryRpmManager.URL_PREFIX;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Path;
import org.restlet.data.Request;
import org.sonatype.plexus.rest.resource.PlexusResource;
import de.is24.nexus.yum.repository.YumRepository;
import de.is24.nexus.yum.service.RepositoryRpmManager;


/**
 * Resource to provide the repository, that contains all repository-RPMs (RPMs
 * contains just one /etc/yum.repos.d/-repo-file) to plug-in the
 * Nexus-repository to Yum.
 *
 * @author sherold
 *
 */
@Path(RepositoryYumRepositoryResource.RESOURCE_URI)
@Singleton
public class RepositoryYumRepositoryResource extends AbstractYumRepositoryResource implements PlexusResource {
  public static final String RESOURCE_URI = "/" + URL_PREFIX;
  @Inject
  @Named(RepositoryRpmManager.DEFAULT_BEAN_NAME)
  private RepositoryRpmManager repositoryRpmManager;

  @Override
  protected String getUrlPrefixName() {
    return URL_PREFIX;
  }

  @Override
  protected YumRepository getFileStructure(Request request, UrlPathInterpretation interpretation) throws Exception {
    return repositoryRpmManager.getYumRepository();
  }

  @Override
  protected int getSegmentCountAfterPrefix() {
    return 0;
  }

  @Override
  public String getResourceUri() {
    return RESOURCE_URI;
  }

}
