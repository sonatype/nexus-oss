package org.sonatype.nexus.plugins;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.MutablePlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.discovery.ComponentDiscoverer;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryEvent;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.plugins.events.PluginActivatedEvent;
import org.sonatype.nexus.plugins.events.PluginRejectedEvent;
import org.sonatype.nexus.plugins.model.ExtensionComponent;
import org.sonatype.nexus.plugins.model.PluginMetadata;
import org.sonatype.nexus.plugins.model.RepositoryType;
import org.sonatype.nexus.plugins.model.UserComponent;
import org.sonatype.nexus.plugins.model.io.xpp3.NexusPluginXpp3Reader;
import org.sonatype.nexus.plugins.repository.NexusPluginRepository;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

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
    implements NexusPluginManager, ComponentDiscoverer, ComponentDiscoveryListener, Initializable
{
    private static final String DESCRIPTOR_PATH = "META-INF/nexus/plugin.xml";

    @Requirement
    private Logger logger;

    @Requirement
    private PlexusContainer plexusContainer;

    @Requirement( hint = "file" )
    private NexusPluginRepository nexusPluginRepository;

    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    private final Map<String, PluginDescriptor> pluginDescriptors = new HashMap<String, PluginDescriptor>();

    protected Logger getLogger()
    {
        return logger;
    }

    public void initialize()
        throws InitializationException
    {
        // DIRTY HACK FOLLOWS!
        ( (MutablePlexusContainer) plexusContainer ).getComponentDiscovererManager().addComponentDiscoverer( this );

        ( (MutablePlexusContainer) plexusContainer ).getComponentDiscovererManager()
            .registerComponentDiscoveryListener( this );
    }

    public Map<String, PluginDescriptor> getInstalledPlugins()
    {
        return Collections.unmodifiableMap( pluginDescriptors );
    }

    public PluginManagerResponse installPlugin( PluginCoordinates coords )
    {
        // TODO
        return new PluginManagerResponse( RequestResult.FAILED );
    }

    public PluginManagerResponse activateInstalledPlugins()
    {
        getLogger().info( "Activating locally installed plugins..." );

        PluginManagerResponse result = new PluginManagerResponse();

        Collection<PluginCoordinates> availablePlugins = nexusPluginRepository.findAvailablePlugins();

        for ( PluginCoordinates pluginCoordinate : availablePlugins )
        {
            result.addPluginResponse( activatePlugin( pluginCoordinate ) );
        }

        return result;
    }

    public PluginManagerResponse uninstallPlugin( PluginCoordinates coords )
    {
        // TODO
        return new PluginManagerResponse( RequestResult.FAILED );
    }

    public PluginResponse activatePlugin( PluginCoordinates pluginCoordinate )
    {
        File pluginFile = nexusPluginRepository.resolvePlugin( pluginCoordinate );

        Collection<File> dependencies = nexusPluginRepository.resolvePluginDependencies( pluginCoordinate );

        ArrayList<URL> constituents = new ArrayList<URL>( 1 + dependencies.size() );

        try
        {
            constituents.add( pluginFile.toURI().toURL() );

            for ( File dependency : dependencies )
            {
                constituents.add( dependency.toURI().toURL() );
            }
        }
        catch ( MalformedURLException e )
        {
            // will not happen
        }

        return activatePlugin( pluginCoordinate, constituents );

    }

    protected PluginResponse activatePlugin( PluginCoordinates pluginCoordinates, List<URL> constituents )
    {
        getLogger().info( "... activating plugin " + pluginCoordinates.toString() );

        PluginResponse result = new PluginResponse( pluginCoordinates );

        try
        {
            ClassRealm pluginRealm = plexusContainer.createChildRealm( pluginCoordinates.toString() );

            for ( URL constituent : constituents )
            {
                pluginRealm.addURL( constituent );
            }

            NexusPluginValidator validator = new DefaultNexusPluginValidator();

            PluginDiscoveryContext discoveryContext = new PluginDiscoveryContext( pluginRealm, validator );

            plexusContainer.discoverComponents( pluginRealm, discoveryContext );

            if ( !discoveryContext.isPluginRegistered() )
            {
                // drop it
                try
                {
                    plexusContainer.removeComponentRealm( pluginRealm );
                }
                catch ( PlexusContainerException e )
                {
                    getLogger().debug( "Could not remove plugin realm!", e );
                }
            }
        }
        catch ( Exception e )
        {
            getLogger().warn( "Was not able to activate Nexus plugin " + pluginCoordinates.toString() + "!", e );

            result.setThrowable( e );
        }

        return result;
    }

    public PluginResponse deactivatePlugin( PluginCoordinates pluginCoordinates )
    {
        PluginResponse result = new PluginResponse( pluginCoordinates );

        try
        {
            String pluginKey =
                getPluginKey( pluginCoordinates.getGroupId(), pluginCoordinates.getArtifactId(), pluginCoordinates
                    .getVersion() );

            if ( getInstalledPlugins().containsKey( pluginKey ) )
            {
                ClassRealm pluginRealm = getInstalledPlugins().get( pluginKey ).getClassRealm();

                plexusContainer.removeComponentRealm( pluginRealm );
            }
            else
            {
                // TODO:?
                result.setThrowable( new IllegalArgumentException( "No such plugin!" ) );
            }
        }
        catch ( Exception e )
        {
            getLogger().warn( "Was not able to deactivate Nexus plugin " + pluginCoordinates.toString() + "!", e );

            result.setThrowable( e );
        }

        return result;
    }

    protected String getPluginKey( String g, String a, String v )
    {
        return g + ":" + a + ":" + v;
    }

    // ==
    // Component Discovery
    // ==

    @SuppressWarnings( "unchecked" )
    public List<ComponentSetDescriptor> findComponents( Context context, ClassRealm realm )
        throws PlexusConfigurationException
    {
        List<ComponentSetDescriptor> componentSetDescriptors = new ArrayList<ComponentSetDescriptor>();

        Enumeration<URL> resources;

        try
        {
            // We don't always want to scan parent realms. For plexus
            // testcase, most components are in the root classloader so that needs to be scanned,
            // but for child realms, we don't.
            if ( realm.getParentRealm() != null )
            {
                resources = realm.findRealmResources( DESCRIPTOR_PATH );
            }
            else
            {
                resources = realm.findResources( DESCRIPTOR_PATH );
            }
        }
        catch ( IOException e )
        {
            throw new PlexusConfigurationException( "Unable to retrieve resources for: " + DESCRIPTOR_PATH
                + " in class realm: " + realm.getId() );
        }

        for ( URL url : Collections.list( resources ) )
        {
            Reader reader = null;

            try
            {
                URLConnection conn = url.openConnection();

                conn.setUseCaches( false );

                conn.connect();

                reader = ReaderFactory.newXmlReader( conn.getInputStream() );

                InterpolationFilterReader interpolationFilterReader =
                    new InterpolationFilterReader( reader, new ContextMapAdapter( context ) );

                PluginDescriptor pluginDescriptor =
                    createComponentDescriptors( realm, interpolationFilterReader, url.toString() );

                pluginDescriptor.setClassRealm( realm );

                if ( pluginDescriptor.getComponents() != null )
                {
                    for ( ComponentDescriptor<?> cd : pluginDescriptor.getComponents() )
                    {
                        cd.setComponentSetDescriptor( pluginDescriptor );

                        cd.setRealm( realm );
                    }
                }

                componentSetDescriptors.add( pluginDescriptor );
            }
            catch ( IOException ex )
            {
                throw new PlexusConfigurationException( "Error reading configuration " + url, ex );
            }
            finally
            {
                IOUtil.close( reader );
            }
        }

        return componentSetDescriptors;
    }

    protected PluginDescriptor createComponentDescriptors( ClassRealm realm, Reader reader, String source )
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

            // =

            result.setPluginKey( getPluginKey( pd.getGroupId(), pd.getArtifactId(), pd.getVersion() ) );

            result.setPluginMetadata( pd );

            result.setClassRealm( realm );

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

            addRequirementsIfNeeded( plugin, pd.getPlugin().getRequirements() );

            csd.addComponentDescriptor( plugin );

            getLogger().debug( "... ... adding NexusPlugin: " + plugin.getImplementation() );
        }

        // extension points, if any
        if ( !pd.getExtensions().isEmpty() )
        {
            for ( ExtensionComponent ext : (List<ExtensionComponent>) pd.getExtensions() )
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

                addRequirementsIfNeeded( extd, ext.getRequirements() );

                csd.addComponentDescriptor( extd );

                getLogger().debug(
                                   "... ... adding ExtensionPoint (role='" + extd.getRole() + "', hint='"
                                       + extd.getRoleHint() + "'): " + ext.getImplementation() );
            }
        }

        // managed user components, if any
        if ( !pd.getComponents().isEmpty() )
        {
            for ( UserComponent cmp : (List<UserComponent>) pd.getComponents() )
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

                addRequirementsIfNeeded( cmpd, cmp.getRequirements() );

                csd.addComponentDescriptor( cmpd );

                getLogger().debug(
                                   "... ... adding user component (role='" + cmpd.getRole() + "', hint='"
                                       + cmpd.getRoleHint() + "'): " + cmpd.getImplementation() );
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

    protected void addRequirementsIfNeeded( ComponentDescriptor<?> component,
                                            List<org.sonatype.nexus.plugins.model.ComponentRequirement> reqs )
    {
        if ( reqs != null && !reqs.isEmpty() )
        {
            ArrayList<ComponentRequirement> result = new ArrayList<ComponentRequirement>( reqs.size() );

            for ( org.sonatype.nexus.plugins.model.ComponentRequirement req : reqs )
            {
                ComponentRequirement reqd = new ComponentRequirement();

                reqd.setFieldName( req.getFieldName() );

                reqd.setRole( req.getComponentContract() );

                reqd.setRoleHint( req.getQualifier() );

                result.add( reqd );
            }

            component.addRequirements( result );
        }
    }

    // ==
    // ComponentDiscoveryListener
    // ==

    @SuppressWarnings( "unchecked" )
    public void componentDiscovered( ComponentDiscoveryEvent event )
    {
        ComponentSetDescriptor componentSetDescriptor = event.getComponentSetDescriptor();

        if ( componentSetDescriptor instanceof PluginDescriptor )
        {
            PluginDescriptor pluginDescriptor = (PluginDescriptor) componentSetDescriptor;

            PluginDiscoveryContext context = (PluginDiscoveryContext) event.getData();

            NexusPluginValidator validator = context.getNexusPluginValidator();

            if ( !validator.validate( pluginDescriptor ) )
            {
                context.setPluginRegistered( false );

                // emit an event
                applicationEventMulticaster.notifyEventListeners( new PluginRejectedEvent( this, pluginDescriptor,
                                                                                           validator ) );

                // and return like nothing happened
                return;
            }

            // register new repo types
            for ( RepositoryType repoType : (List<RepositoryType>) pluginDescriptor.getPluginMetadata()
                .getRepositoryTypes() )
            {
                RepositoryTypeDescriptor repoTypeDescriptor = new RepositoryTypeDescriptor();

                repoTypeDescriptor.setRole( repoType.getComponentContract() );

                repoTypeDescriptor.setPrefix( repoType.getPathPrefix() );

                repositoryTypeRegistry.getRepositoryTypeDescriptors().add( repoTypeDescriptor );
            }

            // add it to "known" plugins
            if ( !pluginDescriptors.containsKey( pluginDescriptor.getPluginKey() ) )
            {
                pluginDescriptors.put( pluginDescriptor.getPluginKey(), pluginDescriptor );

                context.setPluginRegistered( true );

                // emit an event
                applicationEventMulticaster.notifyEventListeners( new PluginActivatedEvent( this, pluginDescriptor ) );
            }
        }
    }
}
