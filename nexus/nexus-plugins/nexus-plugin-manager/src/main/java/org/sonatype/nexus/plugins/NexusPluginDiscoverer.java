package org.sonatype.nexus.plugins;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.discovery.AbstractComponentDiscoverer;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.plugins.model.Component;
import org.sonatype.nexus.plugins.model.Extension;
import org.sonatype.nexus.plugins.model.PluginMetadata;
import org.sonatype.nexus.plugins.model.Requirement;
import org.sonatype.nexus.plugins.model.io.xpp3.NexusPluginXpp3Reader;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;

public class NexusPluginDiscoverer
    extends AbstractComponentDiscoverer
{
    private static final String DESCRIPTOR_PATH = "META-INF/nexus/plugin.xml";

    @Override
    protected String getComponentDescriptorLocation()
    {
        return DESCRIPTOR_PATH;
    }

    @Override
    protected ComponentSetDescriptor createComponentDescriptors( Reader reader, String source )
        throws PlexusConfigurationException
    {
        try
        {
            NexusPluginXpp3Reader pdreader = new NexusPluginXpp3Reader();

            PluginMetadata pd = pdreader.read( reader );

            PluginDescriptor result = new PluginDescriptor();

            // XXX: Jason, is this working in Plexus? For inter-plugin deps or so?
            // result.addDependency( cd );

            result.setId( pd.getArtifactId() );

            result.setSource( source );

            result.setPluginKey( createPluginKey( pd ) );

            result.setPluginMetadata( pd );

            convertPluginMetadata( result, pd );

            return result;
        }
        catch ( IOException e )
        {
            throw new PlexusConfigurationException( "Nexus plugin descriptor found, but cannot read it! (source="
                + source + ")", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new PlexusConfigurationException( "Nexus plugin descriptor found, but is badly formatted! (source="
                + source + ")", e );
        }
    }

    @SuppressWarnings( "unchecked" )
    protected void convertPluginMetadata( PluginDescriptor csd, PluginMetadata pd )
    {
        // plugin entry point, if any
        if ( pd.getPlugin() != null )
        {
            ComponentDescriptor<NexusPlugin> plugin = new ComponentDescriptor<NexusPlugin>();

            plugin.setRole( NexusPlugin.class.getName() );

            plugin.setRoleHint( csd.getPluginKey() );

            plugin.setDescription( pd.getDescription() );

            plugin.setImplementation( pd.getPlugin().getImplementation() );

            plugin.addRequirements( getRequirements( pd.getPlugin().getRequirements() ) );

            csd.addComponentDescriptor( plugin );
        }

        // extension points, if any
        if ( !pd.getExtensions().isEmpty() )
        {
            for ( Extension ext : (List<Extension>) pd.getExtensions() )
            {
                // TEMPLATES! This is not good
                ComponentDescriptor<Object> extd = new ComponentDescriptor<Object>();

                extd.setRole( ext.getExtensionPoint() );

                if ( StringUtils.isNotBlank( ext.getQualifier() ) )
                {
                    extd.setRoleHint( ext.getQualifier() );
                }
                else
                {
                    extd.setRoleHint( ext.getImplementation() );
                }

                extd.setImplementation( ext.getImplementation() );

                if ( !ext.isIsSingleton() )
                {
                    extd.setInstantiationStrategy( "per-lookup" );
                }

                extd.addRequirements( getRequirements( ext.getRequirements() ) );

                csd.addComponentDescriptor( extd );
            }
        }

        // managed user components, if any
        if ( !pd.getComponents().isEmpty() )
        {
            for ( Component cmp : (List<Component>) pd.getComponents() )
            {
                ComponentDescriptor<Object> cmpd = new ComponentDescriptor<Object>();

                cmpd.setRole( cmp.getComponentContract() );

                if ( StringUtils.isNotBlank( cmp.getQualifier() ) )
                {
                    cmpd.setRoleHint( cmp.getQualifier() );
                }

                cmpd.setImplementation( cmp.getImplementation() );

                if ( !cmp.isIsSingleton() )
                {
                    cmpd.setInstantiationStrategy( "per-lookup" );
                }

                cmpd.addRequirements( getRequirements( cmp.getRequirements() ) );

                csd.addComponentDescriptor( cmpd );
            }
        }

        // resources, if any
        if ( !pd.getResources().isEmpty() )
        {
            ComponentDescriptor<Object> resd = new ComponentDescriptor<Object>();

            resd.setRole( NexusResourceBundle.class.getName() );

            resd.setRoleHint( csd.getPluginKey() );

            resd.setImplementation( PluginResourceBundle.class.getName() );

            XmlPlexusConfiguration config = new XmlPlexusConfiguration();

            config.addChild( "pluginKey" ).setValue( csd.getPluginKey() );

            resd.setConfiguration( config );

            csd.addComponentDescriptor( resd );
        }
    }

    protected List<ComponentRequirement> getRequirements( List<Requirement> reqs )
    {
        if ( reqs == null || reqs.isEmpty() )
        {
            return null;
        }
        else
        {
            ArrayList<ComponentRequirement> result = new ArrayList<ComponentRequirement>( reqs.size() );

            for ( Requirement req : reqs )
            {
                ComponentRequirement reqd = new ComponentRequirement();

                reqd.setFieldName( req.getFieldName() );

                reqd.setRole( req.getComponentContract() );

                reqd.setRoleHint( req.getQualifier() );
            }

            return result;
        }
    }

    private String createPluginKey( PluginMetadata pm )
    {
        return pm.getGroupId() + ":" + pm.getArtifactId() + ":" + pm.getVersion();
    }
}
