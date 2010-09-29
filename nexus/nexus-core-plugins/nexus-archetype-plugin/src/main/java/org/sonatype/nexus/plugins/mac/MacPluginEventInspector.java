package org.sonatype.nexus.plugins.mac;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryRepositoryEvent;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.Event;

/**
 * EventInspector that listens to registry events, repo addition and removal, and simply "hooks" in the generated
 * Archetype catalog file to their root.
 * 
 * @author cstamas
 */
public class MacPluginEventInspector
    extends AbstractLoggingComponent
    implements EventInspector
{
    private static final String ARCHETYPE_PATH = "/archetype-catalog.xml";

    @Inject
    @Named( "maven2" )
    private ContentClass maven2ContentClass;

    public boolean accepts( Event<?> evt )
    {
        if ( evt instanceof RepositoryRegistryEventAdd )
        {
            RepositoryRegistryRepositoryEvent registryEvent = (RepositoryRegistryRepositoryEvent) evt;

            Repository repository = registryEvent.getRepository();

            return maven2ContentClass.isCompatible( repository.getRepositoryContentClass() )
                && ( repository.getRepositoryKind().isFacetAvailable( HostedRepository.class )
                    || repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) || repository.getRepositoryKind().isFacetAvailable(
                    GroupRepository.class ) );
        }
        else
        {
            return false;
        }
    }

    public void inspect( Event<?> evt )
    {
        RepositoryRegistryRepositoryEvent registryEvent = (RepositoryRegistryRepositoryEvent) evt;

        Repository repository = registryEvent.getRepository();

        // check is it a maven2 content, and either a "hosted", "proxy" or "group" repository
        if ( maven2ContentClass.isCompatible( repository.getRepositoryContentClass() )
            && ( repository.getRepositoryKind().isFacetAvailable( HostedRepository.class )
                || repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) || repository.getRepositoryKind().isFacetAvailable(
                GroupRepository.class ) ) )
        {
            if ( evt instanceof RepositoryRegistryEventAdd )
            {
                // new repo added, "install" the archetype catalog
                try
                {
                    DefaultStorageFileItem file =
                        new DefaultStorageFileItem( repository, new ResourceStoreRequest( ARCHETYPE_PATH ), true,
                            false, new StringContentLocator( ArchetypeContentGenerator.ID ) );

                    file.getAttributes().put( ContentGenerator.CONTENT_GENERATOR_ID, ArchetypeContentGenerator.ID );

                    repository.storeItem( false, file );
                }
                catch ( Exception e )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().info( "Unable to install the generated archetype catalog!", e );
                    }
                    else
                    {
                        getLogger().info( "Unable to install the generated archetype catalog!" );
                    }
                }
            }
        }
    }

}
