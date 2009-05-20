package org.sonatype.nexus.plugins;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.events.PluginDiscoveredEvent;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.plugin.manager.PlexusPluginManager;
import org.sonatype.plexus.plugin.manager.PluginMetadata;
import org.sonatype.plexus.plugin.manager.PluginResolutionRequest;
import org.sonatype.plexus.plugin.manager.PluginResolutionResult;

@Component( role = NexusPluginManager.class )
public class DefaultNexusPluginManager
    implements NexusPluginManager
{
    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Requirement
    private PlexusPluginManager plexusPluginManager;

    @Requirement
    private NexusPluginCollector nexusPluginCollector;

    @Configuration( value = "${nexus-work}/localRepository" )
    private File nexusLocalRepository;

    protected File getNexusLocalRepository()
    {
        if ( !nexusLocalRepository.exists() )
        {
            nexusLocalRepository.mkdirs();
        }
        
        return nexusLocalRepository;
    }

    public void discoverPlugins( File localRepository )
    {
        PluginResolutionRequest req = null;

        PluginResolutionResult reqRes = null ;//= plexusPluginManager.resolve( req );

        ClassRealm realm = plexusPluginManager.createClassRealm( reqRes.getArtifacts() );

        plexusPluginManager.discoverComponents( realm );
    }

    public Map<String, PluginDescriptor> getInstalledPlugins()
    {
        return nexusPluginCollector.getPluginDescriptors();
    }

    public void installPlugin( URL source )
    {
        // TODO Auto-generated method stub

    }

    public void installPlugin( PluginCoordinates coords )
    {
        PluginMetadata pm = new PluginMetadata( coords.getGroupId(), coords.getArtifactId(), coords.getVersion() );

        PluginResolutionRequest rreq = new PluginResolutionRequest();

        rreq.setPluginMetadata( pm );

        rreq.addLocalRepository( getNexusLocalRepository() );
        
        // rreq.addRemoteRepository( remoteRepository );
    }

    public void activateInstalledPlugins()
    {
        // TODO Auto-generated method stub
        
    }

    public void activatePlugin( String pluginKey )
    {
        // TODO Auto-generated method stub
        
    }

    public void uninstallPlugin( PluginCoordinates coords )
    {
        // TODO Auto-generated method stub
        
    }

}
