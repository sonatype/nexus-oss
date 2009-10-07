package org.sonatype.nexus.feeds;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.access.NexusItemAuthorizer;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = FeedArtifactEventFilter.class )
public class DefaultFeedArtifactEventFilter
    extends AbstractLogEnabled
    implements FeedArtifactEventFilter
{
    @Requirement
    private NexusItemAuthorizer nexusItemAuthorizer;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    public List<NexusArtifactEvent> filterArtifactEventList( List<NexusArtifactEvent> artifactEvents )
    {
        // make sure we have something to filter
        if( artifactEvents == null )
        {
            return null;
        }
        
        List<NexusArtifactEvent> filteredList = new ArrayList<NexusArtifactEvent>();

        for ( NexusArtifactEvent nexusArtifactEvent : artifactEvents )
        {
            if ( this.filterEvent( nexusArtifactEvent ) )
            {
                filteredList.add( nexusArtifactEvent );
            }
        }

        return filteredList;
    }

    private boolean filterEvent( NexusArtifactEvent event )
    {
        try
        {
            Repository repo = this.repositoryRegistry.getRepository( event.getNexusItemInfo().getRepositoryId() );

            ResourceStoreRequest req = new ResourceStoreRequest( event.getNexusItemInfo().getPath() );

            if ( !this.nexusItemAuthorizer.authorizePath( repo, req, Action.read ) )
            {
                return false;
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            // Can't get repository for artifact, therefore we can't authorize access, therefore you don't see it
            getLogger().debug(
                "Feed entry contained invalid repository id " + event.getNexusItemInfo().getRepositoryId(),
                e );

            return false;
        }

        return true;
    }

}
