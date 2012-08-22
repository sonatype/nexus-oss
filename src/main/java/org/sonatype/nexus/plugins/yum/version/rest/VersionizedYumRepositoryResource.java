package org.sonatype.nexus.plugins.yum.version.rest;

import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST;
import static org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND;

import javax.inject.Inject;
import javax.ws.rs.Path;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.plugins.yum.config.YumConfiguration;
import org.sonatype.nexus.plugins.yum.plugin.RepositoryRegistry;
import org.sonatype.nexus.plugins.yum.plugin.impl.MavenRepositoryInfo;
import org.sonatype.nexus.plugins.yum.repository.YumRepository;
import org.sonatype.nexus.plugins.yum.repository.service.YumService;
import org.sonatype.nexus.plugins.yum.rest.AbstractYumRepositoryResource;
import org.sonatype.nexus.plugins.yum.rest.domain.UrlPathInterpretation;
import org.sonatype.nexus.plugins.yum.version.alias.AliasNotFoundException;
import org.sonatype.plexus.rest.resource.PlexusResource;


@Component(role = PlexusResource.class, hint = "VersionizedYumRepositoryResource")
@Path(VersionizedYumRepositoryResource.RESOURCE_URI)
public class VersionizedYumRepositoryResource extends AbstractYumRepositoryResource implements PlexusResource {
  private static final String YUM_REPO_PREFIX_NAME = "yum/repos";
  private static final String YUM_REPO_PREFIX = "/" + YUM_REPO_PREFIX_NAME;
  private static final String VERSION_URL_PARAM = "version";
  private static final String REPOSITORY_URL_PARAM = "repository";
  private static final int SEGMENTS_AFTER_REPO_PREFIX = 3;

  public static final String RESOURCE_URI = YUM_REPO_PREFIX + "/{" + REPOSITORY_URL_PARAM + "}/{" + VERSION_URL_PARAM +
    "}";

  @Inject
  private RepositoryRegistry repositoryRegistry;

  @Inject
  private YumConfiguration aliasMapper;

  @Inject
  private YumService yumService;

  @Override
  protected String getUrlPrefixName() {
    return "yum";
  }

  @Override
  public String getResourceUri() {
    return RESOURCE_URI;
  }

  @Override
  protected YumRepository getFileStructure(Request request, UrlPathInterpretation interpretation) throws Exception {
    final String repositoryId = request.getAttributes().get(REPOSITORY_URL_PARAM).toString();
    final String version = request.getAttributes().get(VERSION_URL_PARAM).toString();

    final MavenRepositoryInfo mavenRepositoryInfo = repositoryRegistry.findRepositoryInfoForId(repositoryId);
    if (mavenRepositoryInfo == null) {
      throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, "Couldn't find repository with id : " + repositoryId);
    }

    final String aliasVersion;
    if (mavenRepositoryInfo.getVersions().contains(version)) {
      aliasVersion = version;
    } else {
      try {
        aliasVersion = aliasMapper.getVersion(repositoryId, version);
      } catch (AliasNotFoundException ex) {
        throw new ResourceException(CLIENT_ERROR_NOT_FOUND,
          "Couldn't find version or alias '" + version + "' in repository '" +
          repositoryId + "'", ex);
      }
    }

    return yumService.getRepository(mavenRepositoryInfo.getRepository(), aliasVersion,
      interpretation.getRepositoryUrl());
  }

  @Override
  protected int getSegmentCountAfterPrefix() {
    return SEGMENTS_AFTER_REPO_PREFIX;
  }

}
