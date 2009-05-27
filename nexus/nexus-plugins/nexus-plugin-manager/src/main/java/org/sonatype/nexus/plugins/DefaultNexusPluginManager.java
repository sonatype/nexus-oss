package org.sonatype.nexus.plugins;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.plexus.NexusPluginCollector;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.plugin.manager.PlexusPluginManager;
import org.sonatype.plexus.plugin.manager.PluginMetadata;
import org.sonatype.plexus.plugin.manager.PluginResolutionRequest;

/**
 * We have multiple showstoppers here (mercury, shane's model, transitive hull, etc), so we are going for simple stuff:
 * <p>
 * A plugin directory looks like this:
 * 
 * <pre>
 *  ${nexus-work}/plugins
 *    aPluginDir/
 *      pluginJar.jar
 *      pluginDepA.jar
 *      pluginDepB.jar
 *      ...
 *    anotherPluginDir/
 *      anotherPlugin.jar
 *      ...
 *    ...
 * </pre>
 * 
 * So, "installing" should be done by a) creating a plugin directory b) copying the plugin and it's deps there (kinda it
 * was before).
 * 
 * @author cstamas
 */
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

    @Configuration( value = "${nexus-work}/plugins" )
    private File nexusPluginsDirectory;

    public Map<String, PluginDescriptor> getInstalledPlugins()
    {
        return Collections.unmodifiableMap( nexusPluginCollector.getPluginDescriptors() );
    }

    public void installPlugin( PluginCoordinates coords )
    {
        // We have a showstopper here:
        // M3 + Mercury is no go
        // furthermore, we would need the transitive hull of plugin (Shane or Oleg)?

        // resolve the plugin against repository

        PluginMetadata pm = new PluginMetadata( coords.getGroupId(), coords.getArtifactId(), coords.getVersion() );

        PluginResolutionRequest rreq = new PluginResolutionRequest();

        rreq.setPluginMetadata( pm );

        // rreq.addLocalRepository( getNexusLocalRepository() );

        // rreq.addRemoteRepository( remoteRepository );
    }

    public void activateInstalledPlugins()
    {
        // TODO Auto-generated method stub

    }

    public void uninstallPlugin( PluginCoordinates coords )
    {
        // TODO Auto-generated method stub

    }

}
