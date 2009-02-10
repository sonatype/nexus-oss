package org.sonatype.nexus.proxy.repository.metadata;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.repository.metadata.MetadataHandlerException;
import org.sonatype.nexus.repository.metadata.RepositoryMetadataHandler;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;
import org.sonatype.nexus.repository.metadata.restlet.RestletRawTransport;

@Component( role = NexusRepositoryMetadataHandler.class )
public class DefaultNexusRepositoryMetadataHandler
    extends AbstractLogEnabled
    implements NexusRepositoryMetadataHandler
{
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement
    private RepositoryMetadataHandler repositoryMetadataHandler;

    public RepositoryMetadata readRemoteRepositoryMetadata( String url )
        throws MetadataHandlerException,
            IOException
    {
        // TODO: honor global proxy? Current solution will neglect it
        RestletRawTransport restletRawTransport = new RestletRawTransport( url );

        return repositoryMetadataHandler.readRepositoryMetadata( restletRawTransport );
    }

    public RepositoryMetadata readRepositoryMetadata( String repositoryId )
        throws NoSuchRepositoryException,
            MetadataHandlerException,
            IOException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        NexusRawTransport nrt = new NexusRawTransport( repository, true, false );

        return repositoryMetadataHandler.readRepositoryMetadata( nrt );
    }

    public void writeRepositoryMetadata( String repositoryId, RepositoryMetadata repositoryMetadata )
        throws NoSuchRepositoryException,
            MetadataHandlerException,
            IOException
    {
        Repository repository = repositoryRegistry.getRepository( repositoryId );

        NexusRawTransport nrt = new NexusRawTransport( repository, true, false );

        repositoryMetadataHandler.writeRepositoryMetadata( repositoryMetadata, nrt );
    }

}
