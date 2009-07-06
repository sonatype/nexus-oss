package org.sonatype.nexus.plugins;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
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
import org.codehaus.plexus.component.discovery.DefaultComponentDiscoverer;
import org.codehaus.plexus.component.repository.ComponentDependency;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRepository;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.plugins.events.PluginActivatedEvent;
import org.sonatype.nexus.plugins.events.PluginRejectedEvent;
import org.sonatype.nexus.plugins.repository.NexusPluginRepository;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleaner;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleanerRequest;
import org.sonatype.plugins.model.PluginDependency;
import org.sonatype.plugins.model.PluginMetadata;
import org.sonatype.plugins.model.io.xpp3.PluginModelXpp3Reader;

/**
 * We have multiple showstoppers here (mercury, shane's model, transitive hull, etc), so we are going for simple stuff:
 * <p>
 * A plugin directory looks like this: THIS IS OUT OF DATE
 * 
 * <pre>
 *  ${nexus-work}/plugin-repository
 *    aPluginDir.g/aPluginDir.a/aPluginDir.v/
 *      aPluginJar.jar
 *      ...
 *    bPluginDir.g/bPluginDir.a/bPluginDir.v/
 *      bPluginJar.jar
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
    implements NexusPluginManager, Initializable
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

    @Requirement
    private PlexusComponentGleaner plexusComponentGleaner;

    private final Map<String, PluginDescriptor> pluginDescriptors = new HashMap<String, PluginDescriptor>();

    protected Logger getLogger()
    {
        return logger;
    }

    public void initialize()
        throws InitializationException
    {
        try
        {
            ComponentRepository componentRepository =
                (ComponentRepository) plexusContainer.getContext().get( ComponentRepository.class.getName() );

            if ( componentRepository instanceof NexusPluginsComponentRepository )
            {
                ( (NexusPluginsComponentRepository) componentRepository ).setNexusPluginManager( this );
            }
        }
        catch ( ContextException e )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger()
                    .debug( "Cannot find ComponentRepository in plexus context! Are we running in a PlexusTestCase?", e );
            }
            else
            {
                getLogger()
                    .info( "Cannot find ComponentRepository in plexus context! Are we running in a PlexusTestCase?" );
            }
        }
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

            PluginMetadata pluginMetadata = null;

            List<String> pluginExports = null;

            List<PluginCoordinates> dependencyPlugins = null;

            NexusPluginValidator validator = new DefaultNexusPluginValidator();

            PluginDiscoveryContext discoveryContext = null;

            try
            {
                // load plugin md from it, will return null if not found
                pluginMetadata = loadPluginMetadata( pluginFile );

                if ( pluginMetadata == null )
                {
                    // this is not a nexus plugin!
                    result.setThrowable( new IllegalArgumentException( "The file \"" + pluginFile.getAbsolutePath()
                        + "\" is not a nexus plugin, it does not have plugin metadata!" ) );

                    return result;
                }

                // create exports
                pluginExports = createExports( pluginFile );

                // create plugin realm as container child
                pluginRealm = plexusContainer.createChildRealm( pluginCoordinates.getPluginKey() );

                // add plugin jar to it
                pluginRealm.addURL( pluginFile.toURI().toURL() );

                // create context
                validator = new DefaultNexusPluginValidator();

                discoveryContext =
                    new PluginDiscoveryContext( pluginCoordinates, pluginExports, pluginRealm, pluginMetadata,
                                                validator );

                // extract imports
                dependencyPlugins = interPluginDependencyResolver.resolveDependencyPlugins( this, pluginMetadata );

                // add imports
                for ( PluginCoordinates coord : dependencyPlugins )
                {
                    PluginDescriptor importPlugin = getInstalledPlugins().get( coord.getPluginKey() );

                    List<String> exports = importPlugin.getExports();

                    for ( String export : exports )
                    {
                        // import ALL
                        pluginRealm.importFrom( importPlugin.getPluginRealm().getId(), export );
                    }

                    discoveryContext.getImportedPlugins().add( importPlugin );
                }

                // get plugin dependecies (not inter-plugin but other libs, jars)
                Collection<File> dependencies = nexusPluginRepository.resolvePluginDependencies( pluginCoordinates );

                // file the realm
                for ( File dependency : dependencies )
                {
                    pluginRealm.addURL( dependency.toURI().toURL() );
                }
            }
            catch ( MalformedURLException e )
            {
                // will not happen
            }

            discoverComponents( discoveryContext );

            // discover plexus components in dependencies too
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

                // TODO: fix this! Why is it not registered?
                result.setThrowable( new IllegalArgumentException( "Plugin is not registered!" ) );
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
                ClassRealm pluginRealm = getInstalledPlugins().get( pluginKey ).getPluginRealm();

                // TODO: dependencies?
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

    protected PluginMetadata loadPluginMetadata( File pluginJar )
        throws IOException
    {
        ZipFile jar = null;

        try
        {
            jar = new ZipFile( pluginJar );

            ZipEntry entry = jar.getEntry( DESCRIPTOR_PATH );

            Reader reader = null;

            try
            {
                if ( entry == null )
                {
                    return null;
                }

                reader = ReaderFactory.newXmlReader( jar.getInputStream( entry ) );

                InterpolationFilterReader interpolationFilterReader =
                    new InterpolationFilterReader( reader, new ContextMapAdapter( plexusContainer.getContext() ) );

                PluginModelXpp3Reader pdreader = new PluginModelXpp3Reader();

                PluginMetadata md = pdreader.read( interpolationFilterReader );

                md.sourceUrl = pluginJar.toURI().toURL();

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

                if ( !e.isDirectory() )
                {
                    String name = e.getName();

                    // for now, only classes are in, and META-INF is filted out
                    // class name without ".class"
                    if ( name.startsWith( "META-INF" ) )
                    {
                        // skip it
                    }
                    else if ( name.endsWith( ".class" ) )
                    {
                        sb.append( name.substring( 0, name.length() - 6 ).replace( "/", "." ) );

                        result.add( sb.toString() );
                    }
                    else
                    {
                        sb.append( name );

                        result.add( sb.toString() );
                    }
                }
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
            .getPluginDependencies() )
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

        result.getExports().addAll( pluginDiscoveryContext.getExports() );

        result.getImportedPlugins().addAll( pluginDiscoveryContext.getImportedPlugins() );

        // and do conversion
        convertPluginMetadata( result, pluginDiscoveryContext );

        return result;
    }

    protected void convertPluginMetadata( PluginDescriptor csd, PluginDiscoveryContext pluginDiscoveryContext )
        throws PlexusConfigurationException
    {
        try
        {
            ComponentDescriptor<?> componentDescriptor = null;

            for ( String className : pluginDiscoveryContext.getExports() )
            {
                String resourceName = className.replaceAll( "\\.", "/" ) + ".class";

                if ( pluginDiscoveryContext.getPluginRealm().getResource( resourceName ) != null )
                {
                    PlexusComponentGleanerRequest request =
                        new PlexusComponentGleanerRequest( className, pluginDiscoveryContext.getPluginRealm() );

                    componentDescriptor = plexusComponentGleaner.glean( request );

                    if ( componentDescriptor != null )
                    {
                        getLogger().debug(
                                           "... ... adding component role=\"" + componentDescriptor.getRole()
                                               + "\", hint=\"" + componentDescriptor.getRoleHint() + "\"" );

                        csd.addComponentDescriptor( componentDescriptor );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new PlexusConfigurationException( "Unable to discover components!", e );
        }

        // FIXME: resolve resources
        /*
         * // resources, if any if ( !pd.getResources().isEmpty() ) { ComponentDescriptor<Object> resd = new
         * ComponentDescriptor<Object>(); resd.setRole( NexusResourceBundle.class.getName() ); resd.setRoleHint(
         * csd.getPluginCoordinates().getPluginKey() ); resd.setImplementation( PluginResourceBundle.class.getName() );
         * XmlPlexusConfiguration config = new XmlPlexusConfiguration(); config.addChild( "pluginKey" ).setValue(
         * csd.getPluginCoordinates().getPluginKey() ); resd.setConfiguration( config ); csd.addComponentDescriptor(
         * resd ); }
         */
    }

    // ==
    // ComponentDiscoveryListener
    // ==

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
        // FIXME: implement this
        /*
         * for ( RepositoryType repoType : (List<RepositoryType>) pluginDescriptor.getPluginMetadata()
         * .getRepositoryTypes() ) { RepositoryTypeDescriptor repoTypeDescriptor = new RepositoryTypeDescriptor();
         * repoTypeDescriptor.setRole( repoType.getComponentContract() ); repoTypeDescriptor.setPrefix(
         * repoType.getPathPrefix() ); repositoryTypeRegistry.getRepositoryTypeDescriptors().add( repoTypeDescriptor );
         * }
         */

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

        List<ComponentSetDescriptor> disvoveredSets = findComponents( pluginDiscoveryContext );

        // HACK -- START
        // to enable "backward compatibility, nexus plugins that are written plexus-way", but circumvent the plexus bug
        // about component descriptor duplication with Realms having parent

        // remember the parent
        ClassRealm parent = pluginDiscoveryContext.getPluginRealm().getParentRealm();

        // make it parentless
        pluginDiscoveryContext.getPluginRealm().setParentRealm( null );

        // discover components from plexus' components.xml
        DefaultComponentDiscoverer defaultDiscoverer = new DefaultComponentDiscoverer();

        disvoveredSets.addAll( defaultDiscoverer.findComponents( plexusContainer.getContext(), pluginDiscoveryContext
            .getPluginRealm() ) );

        // restore original parent
        pluginDiscoveryContext.getPluginRealm().setParentRealm( parent );
        // HACK -- END

        // registering them with plexus
        for ( ComponentSetDescriptor componentSetDescriptor : disvoveredSets )
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
