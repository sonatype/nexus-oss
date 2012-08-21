package org.sonatype.nexus.plugins.yum.metarepo.rest;

import static org.sonatype.nexus.plugins.yum.metarepo.service.RepositoryRpmManager.URL_PREFIX;

import javax.inject.Inject;
import javax.ws.rs.Path;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Request;
import org.sonatype.nexus.plugins.yum.metarepo.service.RepositoryRpmManager;
import org.sonatype.nexus.plugins.yum.repository.YumRepository;
import org.sonatype.nexus.plugins.yum.rest.AbstractYumRepositoryResource;
import org.sonatype.nexus.plugins.yum.rest.domain.UrlPathInterpretation;
import org.sonatype.plexus.rest.resource.PlexusResource;

import org.sonatype.nexus.plugins.yum.metarepo.service.RepositoryRpmManager;
import org.sonatype.nexus.plugins.yum.repository.YumRepository;
import org.sonatype.nexus.plugins.yum.rest.AbstractYumRepositoryResource;
import org.sonatype.nexus.plugins.yum.rest.domain.UrlPathInterpretation;


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
  public static final String RESOURCE_URI = "/" + RepositoryRpmManager.URL_PREFIX;
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
