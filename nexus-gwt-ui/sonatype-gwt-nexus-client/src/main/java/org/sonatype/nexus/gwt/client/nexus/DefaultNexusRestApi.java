package org.sonatype.nexus.gwt.client.nexus;

import org.sonatype.gwt.client.resource.PathUtils;
import org.sonatype.nexus.gwt.client.Nexus;
import org.sonatype.nexus.gwt.client.NexusRestApi;
import org.sonatype.nexus.gwt.client.services.ArtifactService;
import org.sonatype.nexus.gwt.client.services.FeedsService;
import org.sonatype.nexus.gwt.client.services.GlobalSettingsService;
import org.sonatype.nexus.gwt.client.services.RepositoriesService;

/**
 * The inteface published by Nexus REST API. All the URL's are calculated here, in single class. Hence, the "layout" of
 * Nexus REST API is concentrated here, almost nothing else here.
 * 
 * @author cstamas
 */
public class DefaultNexusRestApi
    extends AbstractNexusService
    implements NexusRestApi
{
    private ArtifactService artifactService;

    private FeedsService feedsService;

    private GlobalSettingsService globalSettingsService;

    private RepositoriesService repositoriesService;

    public DefaultNexusRestApi( Nexus nexus, String instanceName )
    {
        super( nexus, PathUtils.append( nexus.getPath(), instanceName ) );
    }

    public ArtifactService getArtifactService()
    {
        if ( artifactService == null )
        {
            artifactService = new DefaultArtifactService( getNexus(), PathUtils.append( getPath(), "artifact" ) );
        }
        return artifactService;
    }

    public FeedsService getFeedsService()
    {
        if ( feedsService == null )
        {
            feedsService = new DefaultFeedsService( getNexus(), PathUtils.append( getPath(), "feeds" ) );
        }
        return feedsService;
    }

    public GlobalSettingsService getGlobalSettingsService()
    {
        if ( globalSettingsService == null )
        {
            globalSettingsService = new DefaultGlobalSettingsService( getNexus(), PathUtils.append(
                getPath(),
                "global_settings" ) );
        }
        return globalSettingsService;
    }

    public RepositoriesService getRepositoriesService()
    {
        if ( repositoriesService == null )
        {
            repositoriesService = new DefaultRepositoriesService( getNexus(), PathUtils.append(
                getPath(),
                "repositories" ) );
        }
        return repositoriesService;
    }

}
