package org.sonatype.nexus.plugins;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.repository.ComponentDependency;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRepository;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.plugins.events.PluginActivatedEvent;
import org.sonatype.nexus.plugins.events.PluginDeactivatedEvent;
import org.sonatype.nexus.plugins.events.PluginRejectedEvent;
import org.sonatype.nexus.plugins.repository.PluginRepositoryManager;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plugin.metadata.gleaner.GleanerException;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleaner;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleanerRequest;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleanerResponse;
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
    private PlexusContainer plexusContainer;

    @Requirement
    private PluginRepositoryManager pluginRepositoryManager;

    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Requirement
    private InterPluginDependencyResolver interPluginDependencyResolver;

    @Requirement
    private PlexusComponentGleaner plexusComponentGleaner;

    private final Map<String, PluginDescriptor> pluginDescriptors = new HashMap<String, PluginDescriptor>();

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
            // Cannot find ComponentRepository in plexus context! Are we running in a PlexusTestCase?
            // this can be neglected, since UT will work anyway
        }
    }

    public Map<String, PluginDescriptor> getInstalledPlugins()
    {
        return Collections.unmodifiableMap( new HashMap<String, PluginDescriptor>( pluginDescriptors ) );
    }

    public PluginManagerResponse installPlugin( PluginCoordinates coords )
    {
        // TODO
        return new PluginManagerResponse( RequestResult.FAILED );
    }

    public PluginManagerResponse activateInstalledPlugins()
    {
        PluginManagerResponse result = new PluginManagerResponse();

        Collection<PluginCoordinates> availablePlugins = pluginRepositoryManager.findAvailablePlugins();

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
        PluginResponse result = new PluginResponse( pluginCoordinates );

        ClassRealm pluginRealm = null;

        PluginDiscoveryContext discoveryContext = null;

        try
        {
            File pluginFile = pluginRepositoryManager.resolvePlugin( pluginCoordinates );

            if ( pluginFile == null || !pluginFile.isFile() )
            {
                // this is not a nexus plugin!
                result.setThrowable( new NoSuchPluginException( pluginCoordinates ) );

                return result;
            }

            NexusPluginValidator validator = new DefaultNexusPluginValidator();

            // load plugin md from it, will return null if not found
            PluginMetadata pluginMetadata = loadPluginMetadata( pluginFile );

            if ( pluginMetadata == null )
            {
                // this is not a nexus plugin!
                result.setThrowable( new InvalidPluginException( pluginCoordinates, "The file \""
                    + pluginFile.getAbsolutePath() + "\" is not a nexus plugin, it does not have plugin metadata!" ) );

                return result;
            }

            // create exports
            List<String> pluginExports = createExports( pluginFile );

            // create plugin realm as container child
            pluginRealm = plexusContainer.createChildRealm( pluginCoordinates.getPluginKey() );

            // add plugin jar to it
            pluginRealm.addURL( toUrl( pluginFile ) );

            // create context
            validator = new DefaultNexusPluginValidator();

            discoveryContext =
                new PluginDiscoveryContext( pluginCoordinates, pluginExports, pluginRealm, pluginMetadata, validator );

            // extract imports
            List<PluginCoordinates> dependencyPlugins =
                interPluginDependencyResolver.resolveDependencyPlugins( this, pluginMetadata );

            // add imports
            for ( PluginCoordinates coord : dependencyPlugins )
            {
                PluginDescriptor importPlugin = getInstalledPlugins().get( coord.getPluginKey() );

                List<String> exports = importPlugin.getExports();

                for ( String export : exports )
                {
                    // import ALL
                    try
                    {
                        pluginRealm.importFrom( importPlugin.getPluginRealm().getId(), export );
                    }
                    catch ( NoSuchRealmException e )
                    {
                        // will not happen
                    }
                }

                discoveryContext.getPluginDescriptor().getImportedPlugins().add( importPlugin );
            }

            // get plugin dependecies (not inter-plugin but other libs, jars)
            Collection<File> dependencies = pluginRepositoryManager.resolvePluginDependencies( pluginCoordinates );

            // file the realm
            for ( File dependencyFile : dependencies )
            {
                // TODO: check dependency clashes
                pluginRealm.addURL( toUrl( dependencyFile ) );
            }

            // ==
            // "real work" starts here

            // do all kind of discoveries needed
            discoverPluginComponents( discoveryContext );

            // validate it
            validatePlugin( discoveryContext );

            // register it
            registerPlugin( discoveryContext );

            // stuff the result
            result.setPluginDescriptor( discoveryContext.getPluginDescriptor() );
        }
        catch ( NoSuchPluginException e )
        {
            result.setThrowable( e );
        }
        catch ( InvalidPluginException e )
        {
            result.setThrowable( e );
        }
        catch ( IOException e )
        {
            result.setThrowable( e );
        }

        // clean up if needed
        if ( !result.isSuccesful() && pluginRealm != null )
        {
            // drop the realm
            try
            {
                plexusContainer.removeComponentRealm( pluginRealm );
            }
            catch ( PlexusContainerException e )
            {
                // TODO: ?
                e.printStackTrace();
            }
        }

        // notifications
        if ( result.isSuccesful() )
        {
            applicationEventMulticaster.notifyEventListeners( new PluginActivatedEvent( this, discoveryContext
                .getPluginDescriptor() ) );
        }
        else
        {
            applicationEventMulticaster.notifyEventListeners( new PluginRejectedEvent( this, pluginCoordinates, result
                .getThrowable() ) );
        }

        return result;
    }

    /**
     * A helper method to "swallow" the MalformedURL that should not happen.
     * 
     * @param file
     * @return
     */
    protected URL toUrl( File file )
    {
        try
        {
            return file.toURI().toURL();
        }
        catch ( MalformedURLException e )
        {
            // should not happen
            return null;
        }
    }

    public PluginResponse deactivatePlugin( PluginCoordinates pluginCoordinates )
    {
        // TODO: dependants on this plugin?
        PluginResponse result = new PluginResponse( pluginCoordinates );

        try
        {
            String pluginKey = pluginCoordinates.getPluginKey();

            if ( getInstalledPlugins().containsKey( pluginKey ) )
            {
                PluginDescriptor pluginDescriptor = getInstalledPlugins().get( pluginKey );

                plexusContainer.removeComponentRealm( pluginDescriptor.getPluginRealm() );

                // send notification
                applicationEventMulticaster.notifyEventListeners( new PluginDeactivatedEvent( this, pluginDescriptor ) );
            }
            else
            {
                result.setThrowable( new NoSuchPluginException( pluginCoordinates ) );
            }
        }
        catch ( PlexusContainerException e )
        {
            result.setThrowable( e );
        }

        return result;
    }

    // ==
    // Plugin JAR mungling
    // ==

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
                    // TODO: ?
                    e.printStackTrace();
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
                    // TODO: ?
                    e.printStackTrace();
                }
            }
        }
    }

    // ==
    // Component Discovery
    // ==

    public List<ComponentDescriptor<?>> discoverPluginComponents( PluginDiscoveryContext pluginDiscoveryContext )
        throws InvalidPluginException, IOException
    {
        try
        {
            List<ComponentDescriptor<?>> discoveredComponentDescriptors = new ArrayList<ComponentDescriptor<?>>();

            PluginDescriptor pluginDescriptor = findComponents( pluginDiscoveryContext );

            // HACK -- START
            // to enable "backward compatibility, nexus plugins that are written plexus-way", but circumvent the plexus
            // bug about component descriptor duplication with Realms having parent (will be rediscovered)

            // remember the parent
            ClassRealm parent = pluginDiscoveryContext.getPluginDescriptor().getPluginRealm().getParentRealm();

            // make it parentless
            pluginDiscoveryContext.getPluginDescriptor().getPluginRealm().setParentRealm( null );

            // discover plexus components in dependencies too. These goes directly into discoveredComponentDescriptors
            // list, since we don't need to register them with plexus, they will be registered by plexus itself
            discoveredComponentDescriptors.addAll( plexusContainer.discoverComponents( pluginDiscoveryContext
                .getPluginDescriptor().getPluginRealm() ) );

            // restore original parent
            pluginDiscoveryContext.getPluginDescriptor().getPluginRealm().setParentRealm( parent );
            // HACK -- END

            // collecting
            for ( ComponentDescriptor<?> componentDescriptor : pluginDescriptor.getComponents() )
            {
                discoveredComponentDescriptors.add( componentDescriptor );
            }

            return discoveredComponentDescriptors;
        }
        catch ( GleanerException e )
        {
            throw new InvalidPluginException( pluginDiscoveryContext.getPluginDescriptor().getPluginCoordinates(), e );
        }
        catch ( CycleDetectedInComponentGraphException e )
        {
            throw new InvalidPluginException( pluginDiscoveryContext.getPluginDescriptor().getPluginCoordinates(), e );
        }
        catch ( PlexusConfigurationException e )
        {
            throw new InvalidPluginException( pluginDiscoveryContext.getPluginDescriptor().getPluginCoordinates(), e );
        }
    }

    public void validatePlugin( PluginDiscoveryContext pluginDiscoveryContext )
        throws InvalidPluginException
    {
        PluginDescriptor pluginDescriptor = pluginDiscoveryContext.getPluginDescriptor();

        NexusPluginValidator validator = pluginDiscoveryContext.getNexusPluginValidator();

        validator.validate( pluginDescriptor );
    }

    // ==
    // Component discovery
    // ==

    protected PluginDescriptor findComponents( PluginDiscoveryContext pluginDiscoveryContext )
        throws GleanerException, IOException
    {
        PluginDescriptor pluginDescriptor = pluginDiscoveryContext.getPluginDescriptor();

        // add inter-plugin dependencies
        for ( PluginDependency dep : pluginDescriptor.getPluginMetadata().getPluginDependencies() )
        {
            ComponentDependency cd = new ComponentDependency();

            cd.setGroupId( dep.getGroupId() );

            cd.setArtifactId( dep.getArtifactId() );

            cd.setVersion( dep.getVersion() );

            pluginDescriptor.addDependency( cd );
        }

        // set basics (inherited from ComponentSetDescriptor)
        pluginDescriptor.setId( pluginDescriptor.getPluginMetadata().getArtifactId() );

        pluginDescriptor.setSource( pluginDescriptor.getPluginMetadata().sourceUrl.toString() );

        // and do conversion
        convertPluginMetadata( pluginDiscoveryContext );

        if ( pluginDescriptor.getComponents() != null )
        {
            for ( ComponentDescriptor<?> cd : pluginDescriptor.getComponents() )
            {
                cd.setComponentSetDescriptor( pluginDescriptor );

                cd.setRealm( pluginDescriptor.getPluginRealm() );
            }
        }

        return pluginDescriptor;
    }

    protected void convertPluginMetadata( PluginDiscoveryContext pluginDiscoveryContext )
        throws GleanerException, IOException
    {
        PluginDescriptor pd = pluginDiscoveryContext.getPluginDescriptor();

        for ( String className : pd.getExports() )
        {
            String resourceName = className.replaceAll( "\\.", "/" ) + ".class";

            if ( pd.getPluginRealm().getRealmResource( resourceName ) != null )
            {
                PlexusComponentGleanerRequest request =
                    new PlexusComponentGleanerRequest( className, pd.getPluginRealm() );

                // repository type is one more shot
                request.getPluralComponentAnnotations().add( RepositoryType.class );

                // listen for repository types
                request.getMarkerAnnotations().add( RepositoryType.class );

                PlexusComponentGleanerResponse response = plexusComponentGleaner.glean( request );

                if ( response != null )
                {
                    if ( response.getMarkerAnnotations().containsKey( RepositoryType.class ) )
                    {
                        RepositoryType repositoryTypeAnno =
                            (RepositoryType) response.getMarkerAnnotations().get( RepositoryType.class );

                        PluginRepositoryType pluginRepositoryType =
                            new PluginRepositoryType( response.getComponentDescriptor().getRole(), repositoryTypeAnno
                                .pathPrefix() );

                        pd.getPluginRepositoryTypes().put( pluginRepositoryType.getComponentContract(),
                                                           pluginRepositoryType );
                    }

                    pd.addComponentDescriptor( response.getComponentDescriptor() );
                }
                else
                {
                    // is it some static resource?
                    if ( className.startsWith( "static/" ) )
                    {
                        pd.getPluginStaticResourceModels().add(
                                                                new PluginStaticResourceModel( className, className,
                                                                                               "text/plain" ) );
                    }
                }
            }
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
    // Registering plugin to manager, repoTypeRegistry (if needed) and plexus
    // ==

    protected void registerPlugin( PluginDiscoveryContext pluginDiscoveryContext )
        throws InvalidPluginException
    {
        PluginDescriptor pluginDescriptor = pluginDiscoveryContext.getPluginDescriptor();

        // add it to "known" plugins
        if ( !pluginDescriptors.containsKey( pluginDescriptor.getPluginCoordinates().getPluginKey() ) )
        {
            // register them to plexus
            for ( ComponentDescriptor<?> componentDescriptor : pluginDescriptor.getComponents() )
            {
                try
                {
                    plexusContainer.addComponentDescriptor( componentDescriptor );
                }
                catch ( CycleDetectedInComponentGraphException e )
                {
                    throw new InvalidPluginException( pluginDiscoveryContext.getPluginDescriptor()
                        .getPluginCoordinates(), e );
                }
            }

            // register newly discovered repo types
            for ( PluginRepositoryType repoType : pluginDescriptor.getPluginRepositoryTypes().values() )
            {
                RepositoryTypeDescriptor repoTypeDescriptor = new RepositoryTypeDescriptor();

                repoTypeDescriptor.setRole( repoType.getComponentContract() );

                repoTypeDescriptor.setPrefix( repoType.getPathPrefix() );

                repositoryTypeRegistry.getRepositoryTypeDescriptors().add( repoTypeDescriptor );
            }

            // add it to map
            pluginDescriptors.put( pluginDescriptor.getPluginCoordinates().getPluginKey(), pluginDescriptor );

            // set is as registered
            pluginDiscoveryContext.setPluginRegistered( true );
        }
    }
}
