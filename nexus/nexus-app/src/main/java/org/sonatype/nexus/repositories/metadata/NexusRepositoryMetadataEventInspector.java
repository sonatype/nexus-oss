package org.sonatype.nexus.repositories.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventUpdate;
import org.sonatype.nexus.proxy.events.RepositoryRegistryRepositoryEvent;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.mirror.PublishedMirrors;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Mirror;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.repository.metadata.MetadataHandlerException;
import org.sonatype.nexus.repository.metadata.RepositoryMetadataHandler;
import org.sonatype.nexus.repository.metadata.model.RepositoryMemberMetadata;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;
import org.sonatype.nexus.repository.metadata.model.RepositoryMirrorMetadata;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "NexusRepositoryMetadataEventInspector" )
public class NexusRepositoryMetadataEventInspector
    extends AbstractLogEnabled
    implements EventInspector
{
    @Requirement( hint = "maven1" )
    private ContentClass maven1ContentClass;

    @Requirement( hint = "maven2" )
    private ContentClass maven2ContentClass;

    @Requirement
    private RepositoryMetadataHandler repositoryMetadataHandler;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    public boolean accepts( Event evt )
    {
        return ( evt instanceof RepositoryRegistryEventAdd ) || ( evt instanceof RepositoryRegistryEventUpdate );
    }

    public void inspect( Event evt )
    {
        RepositoryRegistryRepositoryEvent revt = (RepositoryRegistryRepositoryEvent) evt;

        if ( revt.getRepository().getRepositoryContentClass().isCompatible( maven2ContentClass )
            || revt.getRepository().getRepositoryContentClass().isCompatible( maven1ContentClass ) )
        {
            Repository repository = revt.getRepository();

            if ( LocalStatus.OUT_OF_SERVICE.equals( repository.getLocalStatus() ) )
            {
                return;
            }

            String repositoryUrl = null;

            String repositoryLocalUrl = null;

            List<RepositoryMirrorMetadata> mirrors = null;

            if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
            {
                repositoryUrl = getRepositoryLocalUrl( repository );

                repositoryLocalUrl = null;
            }
            else if ( repository.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
            {
                // this is a maven repository
                MavenRepository mrepository = revt.getRepository().adaptToFacet( MavenRepository.class );

                if ( mrepository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
                {
                    repositoryUrl = mrepository.adaptToFacet( ProxyRepository.class ).getRemoteUrl();

                    repositoryLocalUrl = getRepositoryLocalUrl( mrepository );
                }
                else
                {
                    repositoryUrl = getRepositoryLocalUrl( mrepository );

                    repositoryLocalUrl = null;
                }
            }
            else
            {
                // huh? unknown stuff, better to not tamper with it
                return;
            }

            if ( repository.getRepositoryKind().isFacetAvailable( HostedRepository.class ) )
            {
                mirrors = getMirrors( repository.getId() );
            }

            RepositoryMetadata rm = new RepositoryMetadata();
            rm.setUrl( repositoryUrl );
            rm.setId( repository.getId() );
            rm.setName( repository.getName() );
            rm.setLayout( repository.getRepositoryContentClass().getId() );
            rm.setPolicy( getRepositoryPolicy( repository ) );
            rm.setMirrors( mirrors );

            if ( repositoryLocalUrl != null )
            {
                rm.setLocalUrl( repositoryLocalUrl );
            }

            if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
            {
                List<Repository> members = repository.adaptToFacet( GroupRepository.class ).getMemberRepositories();

                List<RepositoryMemberMetadata> memberMetadatas =
                    new ArrayList<RepositoryMemberMetadata>( members.size() );

                for ( Repository member : members )
                {
                    RepositoryMemberMetadata memberMetadata = new RepositoryMemberMetadata();

                    memberMetadata.setId( member.getId() );

                    memberMetadata.setName( member.getName() );

                    memberMetadata.setUrl( getRepositoryLocalUrl( member ) );

                    memberMetadata.setPolicy( getRepositoryPolicy( member ) );

                    memberMetadatas.add( memberMetadata );
                }

                rm.getMemberRepositories().addAll( memberMetadatas );
            }

            try
            {
                NexusRawTransport nrt = new NexusRawTransport( repository, true, false );

                repositoryMetadataHandler.writeRepositoryMetadata( rm, nrt );

                // "decorate" the file attrs
                StorageFileItem file = nrt.getLastWriteFile();

                file.getAttributes().put( ContentGenerator.CONTENT_GENERATOR_ID,
                                          "NexusRepositoryMetadataContentGenerator" );

                repository.getLocalStorage().updateItemAttributes( repository, new ResourceStoreRequest( file ), file );
            }
            catch ( MetadataHandlerException e )
            {
                getLogger().info( "Could not write repository metadata!", e );
            }
            catch ( IOException e )
            {
                getLogger().warn( "IOException during write of repository metadata!", e );
            }
            catch ( Exception e )
            {
                getLogger().info( "Could not save repository metadata: ", e );
            }
        }
    }

    protected String getRepositoryLocalUrl( Repository repository )
    {
        if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            return "@rootUrl@/content/groups/" + repository.getId();
        }
        else
        {
            return "@rootUrl@/content/repositories/" + repository.getId();
        }
    }

    protected String getRepositoryPolicy( Repository repository )
    {
        if ( repository.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
        {
            return repository.adaptToFacet( MavenRepository.class ).getRepositoryPolicy().toString().toLowerCase();
        }
        else if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            List<Repository> members = repository.adaptToFacet( GroupRepository.class ).getMemberRepositories();

            HashSet<String> memberPolicies = new HashSet<String>();

            for ( Repository member : members )
            {
                memberPolicies.add( getRepositoryPolicy( member ) );
            }

            if ( memberPolicies.size() == 1 )
            {
                return memberPolicies.iterator().next();
            }
            else
            {
                return RepositoryMetadata.POLICY_MIXED;
            }
        }
        else
        {
            return RepositoryMetadata.POLICY_MIXED;
        }
    }

    protected List<RepositoryMirrorMetadata> getMirrors( String repositoryId )
    {
        try
        {
            List<RepositoryMirrorMetadata> mirrors = new ArrayList<RepositoryMirrorMetadata>();

            Repository repository = repositoryRegistry.getRepository( repositoryId );

            PublishedMirrors publishedMirrors = repository.getPublishedMirrors();

            for ( Mirror mirror : (List<Mirror>) publishedMirrors.getMirrors() )
            {
                RepositoryMirrorMetadata md = new RepositoryMirrorMetadata();

                md.setId( mirror.getId() );

                md.setUrl( mirror.getUrl() );

                mirrors.add( md );
            }

            return mirrors;
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().debug( "Repository not found, returning no mirrors" );
        }

        return null;
    }
}
