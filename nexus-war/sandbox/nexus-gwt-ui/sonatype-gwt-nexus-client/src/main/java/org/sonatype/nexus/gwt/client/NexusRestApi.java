package org.sonatype.nexus.gwt.client;

import org.sonatype.gwt.client.resource.Resource;
import org.sonatype.nexus.gwt.client.services.ArtifactService;
import org.sonatype.nexus.gwt.client.services.FeedsService;
import org.sonatype.nexus.gwt.client.services.GlobalSettingsService;
import org.sonatype.nexus.gwt.client.services.RepositoriesService;

/**
 * The interface published by Nexus REST API.
 * 
 * @author cstamas
 */
public interface NexusRestApi
    extends Resource
{
    GlobalSettingsService getGlobalSettingsService();

    RepositoriesService getRepositoriesService();

    ArtifactService getArtifactService();

    FeedsService getFeedsService();
}
