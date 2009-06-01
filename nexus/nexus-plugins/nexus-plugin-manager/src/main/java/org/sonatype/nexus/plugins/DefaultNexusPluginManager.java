package org.sonatype.nexus.plugins;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Map;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.plexus.NexusPluginCollector;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.plugin.manager.PlexusPluginManager;

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

    protected File getNexusPluginsDirectory()
    {
        if ( !nexusPluginsDirectory.exists() )
        {
            nexusPluginsDirectory.mkdirs();
        }

        return nexusPluginsDirectory;
    }

    public Map<String, PluginDescriptor> getInstalledPlugins()
    {
        return Collections.unmodifiableMap( nexusPluginCollector.getPluginDescriptors() );
    }

    public PluginManagerResponse installPlugin( PluginCoordinates coords )
    {
        // We have a showstopper here:
        // M3 + Mercury is no go
        // furthermore, we would need the transitive hull of plugin (Shane or Oleg)?

        // TODO
        return new PluginManagerResponse( RequestResult.FAILED );
    }

    public PluginManagerResponse activateInstalledPlugins()
    {
        /*File[] pluginDirs = getNexusPluginsDirectory().listFiles();

        if ( pluginDirs != null )
        {
            for ( File pluginDir : pluginDirs )
            {
                if ( pluginDir.isDirectory() )
                {
                    ClassRealm pluginRealm = plexusPluginManager.createClassRealm( pluginDir.getName() );

                    File[] pluginConstituents = pluginDir.listFiles();

                    if ( pluginConstituents != null )
                    {
                        for ( File pluginConstituent : pluginConstituents )
                        {
                            try
                            {
                                pluginRealm.addURL( pluginConstituent.toURI().toURL() );
                            }
                            catch ( MalformedURLException e )
                            {
                                // will not happen
                            }
                        }
                    }

                    plexusPluginManager.discoverComponents( pluginRealm );
                }
            }
        }
*/
        return new PluginManagerResponse( RequestResult.COMPLETELY_EXECUTED );
    }

    public PluginManagerResponse uninstallPlugin( PluginCoordinates coords )
    {
        // TODO
        return new PluginManagerResponse( RequestResult.FAILED );
    }

}
