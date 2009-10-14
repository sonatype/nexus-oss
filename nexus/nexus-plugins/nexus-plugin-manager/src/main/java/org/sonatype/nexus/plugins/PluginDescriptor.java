package org.sonatype.nexus.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.sonatype.nexus.util.ClasspathUtils;
import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;

public class PluginDescriptor
    extends ComponentSetDescriptor
{
    private GAVCoordinate pluginCoordinates;

    private PluginMetadata pluginMetadata;

    private ClassRealm pluginRealm;

    private List<String> exportedResources;

    private List<String> gleanedResources;

    private List<PluginDescriptor> importedPlugins;

    private List<PluginStaticResourceModel> pluginStaticResourceModels;

    private Map<String, PluginRepositoryType> pluginRepositoryTypes;

    public GAVCoordinate getPluginCoordinates()
    {
        return pluginCoordinates;
    }

    public void setPluginCoordinates( GAVCoordinate pluginCoordinates )
    {
        this.pluginCoordinates = pluginCoordinates;
    }

    public ClassRealm getPluginRealm()
    {
        return pluginRealm;
    }

    public void setPluginRealm( ClassRealm pluginRealm )
    {
        this.pluginRealm = pluginRealm;
    }

    public PluginMetadata getPluginMetadata()
    {
        return pluginMetadata;
    }

    public void setPluginMetadata( PluginMetadata pluginMetadata )
    {
        this.pluginMetadata = pluginMetadata;
    }

    /**
     * This method returns the modifiable list of exported resources that are within this plugin JAR. In case of
     * classes, these are <b>binary</b> names! See method getExportedClassnames() if you want the list of classes in
     * this plugin JAR!
     * 
     * @return list of resources (class binary names) found in this plugin JAR that are exported.
     */
    public List<String> getExportedResources()
    {
        if ( exportedResources == null )
        {
            exportedResources = new ArrayList<String>();
        }

        return exportedResources;
    }

    /**
     * Returns an <b>unmodifiable</b> list of exported classnames found in this plugin JAR. They are converted/filtered
     * on the fly from the getExportedResources() method result. The "META-INF" directory and it's content is filtered
     * out. Classes will have "canonical names" instead of "binary names", while resources will retain their binary
     * names, to be able to be loaded from Classloader.
     * 
     * @return
     */
    public List<String> getExportedClassnames()
    {
        List<String> binaryNames = getExportedResources();

        ArrayList<String> classNames = new ArrayList<String>( binaryNames.size() );

        for ( String binaryName : binaryNames )
        {
            // for now, only classes are in, and META-INF is filtered out
            if ( binaryName.startsWith( "META-INF" ) )
            {
                // skip it
            }
            else
            {
                String className = ClasspathUtils.convertClassBinaryNameToCanonicalName( binaryName );

                if ( className != null )
                {
                    classNames.add( className );
                }
            }
        }

        return Collections.unmodifiableList( classNames );
    }

    /**
     * List of binary names (of classes) and resources, that may need gleaning for this plugin.
     * 
     * @return
     */
    public List<String> getGleanedResources()
    {
        if ( gleanedResources == null )
        {
            gleanedResources = new ArrayList<String>();
        }

        return gleanedResources;
    }

    public List<PluginDescriptor> getImportedPlugins()
    {
        if ( importedPlugins == null )
        {
            importedPlugins = new ArrayList<PluginDescriptor>();
        }

        return importedPlugins;
    }

    public List<PluginStaticResourceModel> getPluginStaticResourceModels()
    {
        if ( pluginStaticResourceModels == null )
        {
            pluginStaticResourceModels = new ArrayList<PluginStaticResourceModel>();
        }

        return pluginStaticResourceModels;
    }

    public Map<String, PluginRepositoryType> getPluginRepositoryTypes()
    {
        if ( pluginRepositoryTypes == null )
        {
            pluginRepositoryTypes = new HashMap<String, PluginRepositoryType>();
        }

        return pluginRepositoryTypes;
    }
}
