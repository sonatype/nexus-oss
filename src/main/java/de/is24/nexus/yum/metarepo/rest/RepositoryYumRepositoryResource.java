package de.is24.nexus.yum.metarepo.rest;

import static de.is24.nexus.yum.version.service.RepositoryRpmManager.URL_PREFIX;

import javax.inject.Inject;
import javax.ws.rs.Path;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Request;
import org.sonatype.plexus.rest.resource.PlexusResource;
import de.is24.nexus.yum.repository.YumRepository;
import de.is24.nexus.yum.rest.AbstractYumRepositoryResource;
import de.is24.nexus.yum.rest.domain.UrlPathInterpretation;
import de.is24.nexus.yum.version.service.RepositoryRpmManager;


/**
 * Resource to provide the repository, that contains all repository-RPMs (RPMs
 * contains just one /etc/yum.repos.d/-repo-file) to plug-in the
 * Nexus-repository to Yum.
 *
 * @author sherold
 *
 */
@Component(role = PlexusResource.class, hint = "RepositoryYumRepositoryResource")
@Path(RepositoryYumRepositoryResource.RESOURCE_URI)
public class RepositoryYumRepositoryResource extends AbstractYumRepositoryResource implements PlexusResource {
  public static final String RESOURCE_URI = "/" + URL_PREFIX;
  @Inject
  private RepositoryRpmManager repositoryRpmManager;

  @Override
  protected String getUrlPrefixName() {
    return "yum";
  }

  @Override
  protected YumRepository getFileStructure(Request request, UrlPathInterpretation interpretation) throws Exception {
    return repositoryRpmManager.getYumRepository();
  }

  @Override
  protected int getSegmentCountAfterPrefix() {
    return 1;
  }

  @Override
  public String getResourceUri() {
    return RESOURCE_URI;
  }

}
