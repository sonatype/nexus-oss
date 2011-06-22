package org.sonatype.nexus.plugins.p2.repository.internal;

import static org.sonatype.nexus.plugins.p2.repository.internal.NexusUtils.retrieveFile;

import java.io.File;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.p2.repository.P2MetadataGenerator;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.plexus.appevents.Event;

@Named
@Singleton
public class JarsEventsInspector
    implements EventInspector
{

    private final P2MetadataGenerator p2MetadataGenerator;

    private final RepositoryRegistry repositories;

    @Inject
    public JarsEventsInspector( final P2MetadataGenerator p2MetadataGenerator, final RepositoryRegistry repositories )
    {
        this.p2MetadataGenerator = p2MetadataGenerator;
        this.repositories = repositories;
    }

    @Override
    public boolean accepts( final Event<?> evt )
    {
        if ( evt == null
            || !( evt instanceof RepositoryItemEvent )
            || !( evt instanceof RepositoryItemEventStore || evt instanceof RepositoryItemEventCache || evt instanceof RepositoryItemEventDelete ) )
        {
            return false;
        }

        final RepositoryItemEvent event = (RepositoryItemEvent) evt;

        return isABundle( event.getItem() );
    }

    @Override
    public void inspect( final Event<?> evt )
    {
        if ( !accepts( evt ) )
        {
            return;
        }

        final RepositoryItemEvent event = (RepositoryItemEvent) evt;

        if ( event instanceof RepositoryItemEventStore || event instanceof RepositoryItemEventCache )
        {
            onItemAdded( event );
        }
        else if ( event instanceof RepositoryItemEventDelete )
        {
            onItemRemoved( event );
        }
    }

    private void onItemAdded( final RepositoryItemEvent event )
    {
        p2MetadataGenerator.generateP2Metadata( event.getItem() );
    }

    private void onItemRemoved( final RepositoryItemEvent event )
    {
        p2MetadataGenerator.removeP2Metadata( event.getItem() );
    }

    // TODO optimize by saving the fact that is a bundle as item attribute and check that one first
    private boolean isABundle( final StorageItem item )
    {
        if ( item == null )
        {
            return false;
        }
        try
        {
            final File file = retrieveFile( repositories.getRepository( item.getRepositoryId() ), item.getPath() );
            final JarFile jarFile = new JarFile( file );
            final Manifest manifest = jarFile.getManifest();
            final Attributes mainAttributes = manifest.getMainAttributes();
            return mainAttributes.getValue( "Bundle-SymbolicName" ) != null;
        }
        catch ( final Exception e )
        {
            return false;
        }
    }
}
