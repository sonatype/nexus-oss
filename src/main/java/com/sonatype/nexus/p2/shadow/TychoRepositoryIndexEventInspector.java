/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.p2.shadow;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.tycho.p2.facade.internal.DefaultTychoRepositoryIndex;

@Component( role = EventInspector.class, hint = TychoRepositoryIndexContentGenerator.ROLE_HINT )
public class TychoRepositoryIndexEventInspector
    extends AbstractEventInspector
    implements EventInspector
{
    @Requirement( hint = "maven2" )
    private ContentClass maven2ContentClass;
    
    public boolean accepts( Event<?> evt )
    {
        return evt instanceof RepositoryRegistryEventAdd;
    }

    public void inspect( Event<?> evt )
    {
        RepositoryRegistryEventAdd registryEvent = (RepositoryRegistryEventAdd) evt;

        Repository repository = registryEvent.getRepository();

        if ( maven2ContentClass.isCompatible( repository.getRepositoryContentClass() )
            && ( repository.getRepositoryKind().isFacetAvailable( HostedRepository.class )
                || repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) || repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) ) )
        {
            // new repo added, "install" the archetype catalog
            try
            {
                DefaultStorageFileItem file =
                    new DefaultStorageFileItem( repository, new ResourceStoreRequest( DefaultTychoRepositoryIndex.INDEX_RELPATH ), true, false,
                                                new StringContentLocator( ContentGenerator.CONTENT_GENERATOR_ID + "="
                                                    + TychoRepositoryIndexContentGenerator.ROLE_HINT ) );

                file.getAttributes().put( ContentGenerator.CONTENT_GENERATOR_ID,
                                          TychoRepositoryIndexContentGenerator.ROLE_HINT );

                repository.storeItem( false, file );
            }
            catch ( Exception e )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().info( "Unable to install the generated Tycho repository index!", e );
                }
                else
                {
                    getLogger().info( "Unable to install the generated Tycho repository index!" );
                }
            }
        }
    }
}
