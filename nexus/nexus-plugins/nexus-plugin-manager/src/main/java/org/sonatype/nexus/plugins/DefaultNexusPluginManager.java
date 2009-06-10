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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.repository.ComponentDependency;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.plugins.events.PluginActivatedEvent;
import org.sonatype.nexus.plugins.events.PluginRejectedEvent;
import org.sonatype.nexus.plugins.model.ExtensionComponent;
import org.sonatype.nexus.plugins.model.PluginDependency;
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
    implements NexusPluginManager
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

    @Requirement
    private InterPluginDependencyResolver interPluginDependencyResolver;

    private final Map<String, PluginDescriptor> pluginDescriptors = new HashMap<String, PluginDescriptor>();

    protected Logger getLogger()
    {
        return logger;
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
        if ( getInstalledPlugins().containsKey( pluginCoordinate.getPluginKey() ) )
        {
            PluginResponse result = new PluginResponse( pluginCoordinate );

            return result;
        }

        return doActivatePlugin( pluginCoordinate );
    }

    protected PluginResponse doActivatePlugin( PluginCoordinates pluginCoordinates )
    {
        getLogger().info( "... activating plugin " + pluginCoordinates.toString() );

        PluginResponse result = new PluginResponse( pluginCoordinates );

        try
        {
            File pluginFile = nexusPluginRepository.resolvePlugin( pluginCoordinates );

            ClassRealm pluginRealm = null;

            ClassRealm dependencyRealm = null;

            PluginMetadata pluginMetadata = null;

            try
            {
                // create plugin realm as container child
                pluginRealm = plexusContainer.createChildRealm( pluginCoordinates.getPluginKey() );

                // add plugin jar to it
                pluginRealm.addURL( pluginFile.toURI().toURL() );

                // load plugin md from it
                pluginMetadata = loadPluginMetadata( pluginRealm );

                // extract imports
                List<PluginCoordinates> imports =
                    interPluginDependencyResolver.resolveDependencyRealms( this, pluginMetadata );

                // add imports
                for ( PluginCoordinates coord : imports )
                {
                    // import ALL
                    pluginRealm.importFrom( getInstalledPlugins().get( coord.getPluginKey() ).getPluginRealm().getId(),
                                            "" );
                }

                // create a dependencyRealm as child of plugin realm
                dependencyRealm = pluginRealm.createChildRealm( pluginRealm.getId() + "-dependencies" );
                // dependencyRealm = pluginRealm.getWorld().newRealm( pluginRealm.getId() + "-dependencies" );

                // get plugin dependecies (not inter-plugin but other libs, jars)
                Collection<File> dependencies = nexusPluginRepository.resolvePluginDependencies( pluginCoordinates );

                // file the realm
                for ( File dependency : dependencies )
                {
                    dependencyRealm.addURL( dependency.toURI().toURL() );
                }

                // pluginRealm.importFrom( dependencyRealm.getId(), "" );
            }
            catch ( MalformedURLException e )
            {
                // will not happen
            }

            NexusPluginValidator validator = new DefaultNexusPluginValidator();

            PluginDiscoveryContext discoveryContext =
                new PluginDiscoveryContext( pluginCoordinates, pluginRealm, dependencyRealm, pluginMetadata, validator );

            discoverComponents( discoveryContext );

            // discover plexus components in dependencies too
            plexusContainer.discoverComponents( dependencyRealm, discoveryContext );

            if ( !discoveryContext.isPluginRegistered() )
            {
                // drop it
                try
                {
                    plexusContainer.removeComponentRealm( dependencyRealm );
                }
                catch ( PlexusContainerException e )
                {
                    getLogger().debug( "Could not remove plugin dependency realm!", e );
                }

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
            String pluginKey = pluginCoordinates.getPluginKey();

            if ( getInstalledPlugins().containsKey( pluginKey ) )
            {
                ClassRealm dependencyRealm = getInstalledPlugins().get( pluginKey ).getDependencyRealm();

                ClassRealm pluginRealm = getInstalledPlugins().get( pluginKey ).getPluginRealm();

                plexusContainer.removeComponentRealm( dependencyRealm );

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

    protected PluginMetadata loadPluginMetadata( ClassRealm pluginRealm )
        throws IOException
    {
        Enumeration<URL> resources = pluginRealm.findRealmResources( DESCRIPTOR_PATH );

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
                    new InterpolationFilterReader( reader, new ContextMapAdapter( plexusContainer.getContext() ) );

                NexusPluginXpp3Reader pdreader = new NexusPluginXpp3Reader();

                PluginMetadata md = pdreader.read( interpolationFilterReader );

                md.sourceUrl = url;

                return md;
            }
            catch ( XmlPullParserException e )
            {
                IOException ex = new IOException( e.getMessage() );

                ex.initCause( e );

                throw ex;
            }
            finally
            {
                IOUtil.close( reader );
            }
        }

        throw new IOException( "No Nexus plugin metadata found!" );
    }

    protected List<String> createExports( File pluginJar )
        throws IOException
    {
        ZipFile jar = null;

        try
        {
            jar = new ZipFile( pluginJar );

            ArrayList<String> result = new ArrayList<String>( jar.size() );

            @SuppressWarnings( "unchecked" )
            Enumeration en = jar.entries();
            while ( en.hasMoreElements() )
            {
                StringBuilder sb = new StringBuilder();

                ZipEntry e = (ZipEntry) en.nextElement();

                String name = e.getName();

                if ( name.charAt( 0 ) != '/' )
                {
                    sb.append( '/' );
                }

                // class name without ".class"
                if ( name.endsWith( ".class" ) )
                {
                    sb.append( name.substring( 0, name.length() - 6 ) );
                }
                else
                {
                    sb.append( name );
                }

                result.add( sb.toString() );
            }

            return result;
        }
        finally
        {
            if ( jar != null )
            {
                try
                {
                    jar.close();
                }
                catch ( Exception e )
                {
                    getLogger().error( "Could not close jar file properly.", e );
                }
            }
        }
    }

    // ==
    // Component Discovery
    // ==

    public List<ComponentSetDescriptor> findComponents( PluginDiscoveryContext pluginDiscoveryContext )
        throws PlexusConfigurationException
    {
        List<ComponentSetDescriptor> componentSetDescriptors = new ArrayList<ComponentSetDescriptor>();

        PluginDescriptor pluginDescriptor = createComponentDescriptors( pluginDiscoveryContext );

        pluginDiscoveryContext.setPluginDescriptor( pluginDescriptor );

        pluginDescriptor.setPluginRealm( pluginDiscoveryContext.getPluginRealm() );

        pluginDescriptor.setDependencyRealm( pluginDiscoveryContext.getDependencyRealm() );

        if ( pluginDescriptor.getComponents() != null )
        {
            for ( ComponentDescriptor<?> cd : pluginDescriptor.getComponents() )
            {
                cd.setComponentSetDescriptor( pluginDescriptor );

                cd.setRealm( pluginDiscoveryContext.getPluginRealm() );
            }
        }

        componentSetDescriptors.add( pluginDescriptor );

        return componentSetDescriptors;
    }

    protected PluginDescriptor createComponentDescriptors( PluginDiscoveryContext pluginDiscoveryContext )
        throws PlexusConfigurationException
    {
        PluginDescriptor result = new PluginDescriptor();

        // add inter-plugin dependencies
        for ( PluginDependency dep : (List<PluginDependency>) pluginDiscoveryContext.getPluginMetadata()
            .getDependencies() )
        {
            ComponentDependency cd = new ComponentDependency();

            cd.setGroupId( dep.getGroupId() );

            cd.setArtifactId( dep.getArtifactId() );

            cd.setVersion( dep.getVersion() );

            result.addDependency( cd );
        }

        // set basics
        result.setId( pluginDiscoveryContext.getPluginMetadata().getArtifactId() );

        result.setSource( pluginDiscoveryContext.getPluginMetadata().sourceUrl.toString() );

        // set PluginDescriptor specifics
        result.setPluginCoordinates( pluginDiscoveryContext.getPluginCoordinates() );

        result.setPluginMetadata( pluginDiscoveryContext.getPluginMetadata() );

        result.setPluginRealm( pluginDiscoveryContext.getPluginRealm() );

        result.setDependencyRealm( pluginDiscoveryContext.getDependencyRealm() );

        // and do conversion
        convertPluginMetadata( result, pluginDiscoveryContext.getPluginMetadata() );

        return result;
    }

    @SuppressWarnings( "unchecked" )
    protected void convertPluginMetadata( PluginDescriptor csd, PluginMetadata pd )
    {
        // plugin entry point, if any
        if ( pd.getPlugin() != null )
        {
            ComponentDescriptor<NexusPlugin> plugin = new ComponentDescriptor<NexusPlugin>();

            plugin.setRole( NexusPlugin.class.getName() );

            plugin.setRoleHint( csd.getPluginCoordinates().getPluginKey() );

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

            resd.setRoleHint( csd.getPluginCoordinates().getPluginKey() );

            resd.setImplementation( PluginResourceBundle.class.getName() );

            XmlPlexusConfiguration config = new XmlPlexusConfiguration();

            config.addChild( "pluginKey" ).setValue( csd.getPluginCoordinates().getPluginKey() );

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
    public void componentDiscovered( PluginDiscoveryContext pluginDiscoveryContext )
    {
        PluginDescriptor pluginDescriptor = pluginDiscoveryContext.getPluginDescriptor();

        NexusPluginValidator validator = pluginDiscoveryContext.getNexusPluginValidator();

        if ( !validator.validate( pluginDescriptor ) )
        {
            pluginDiscoveryContext.setPluginRegistered( false );

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
        if ( !pluginDescriptors.containsKey( pluginDescriptor.getPluginCoordinates().getPluginKey() ) )
        {
            pluginDescriptors.put( pluginDescriptor.getPluginCoordinates().getPluginKey(), pluginDescriptor );

            pluginDiscoveryContext.setPluginRegistered( true );

            // emit an event
            applicationEventMulticaster.notifyEventListeners( new PluginActivatedEvent( this, pluginDescriptor ) );
        }
    }

    // ==
    // Copied from DefaultPlexusContainer and modified
    // (to use only "our" discoverer!)
    // ==
    public void discoverComponents( PluginDiscoveryContext pluginDiscoveryContext )
        throws PlexusConfigurationException, CycleDetectedInComponentGraphException
    {
        List<ComponentDescriptor<?>> discoveredComponentDescriptors = new ArrayList<ComponentDescriptor<?>>();

        for ( ComponentSetDescriptor componentSetDescriptor : findComponents( pluginDiscoveryContext ) )
        {
            // Here we should collect all the urls
            // do the interpolation against the context
            // register all the components
            // allow interception and replacement of the components

            for ( ComponentDescriptor<?> componentDescriptor : componentSetDescriptor.getComponents() )
            {
                plexusContainer.addComponentDescriptor( componentDescriptor );

                discoveredComponentDescriptors.add( componentDescriptor );
            }

            componentDiscovered( pluginDiscoveryContext );
        }
    }

}
