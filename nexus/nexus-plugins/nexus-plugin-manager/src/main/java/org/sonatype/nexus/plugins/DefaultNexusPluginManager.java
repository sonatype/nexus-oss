package org.sonatype.nexus.plugins;

import java.io.File;
import java.io.IOException;
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
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.repository.ComponentDependency;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRepository;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.plugins.events.PluginActivatedEvent;
import org.sonatype.nexus.plugins.events.PluginDeactivatedEvent;
import org.sonatype.nexus.plugins.events.PluginRejectedEvent;
import org.sonatype.nexus.plugins.plexus.NexusPluginsComponentRepository;
import org.sonatype.nexus.plugins.repository.NexusPluginRepository;
import org.sonatype.nexus.plugins.repository.NexusWritablePluginRepository;
import org.sonatype.nexus.plugins.repository.NoSuchPluginRepositoryArtifactException;
import org.sonatype.nexus.plugins.repository.PluginRepositoryArtifact;
import org.sonatype.nexus.plugins.repository.PluginRepositoryManager;
import org.sonatype.nexus.plugins.repository.UserNexusPluginRepository;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.util.ClasspathUtils;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugin.metadata.gleaner.GleanerException;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleaner;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleanerRequest;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleanerResponse;
import org.sonatype.plugins.model.ClasspathDependency;
import org.sonatype.plugins.model.PluginDependency;
import org.sonatype.plugins.model.PluginMetadata;

/**
 * Plugin Manager implementation.
 * 
 * @author cstamas
 */
@Component( role = NexusPluginManager.class )
public class DefaultNexusPluginManager
    implements NexusPluginManager, Initializable
{
    @Requirement
    private PlexusContainer plexusContainer;

    @Requirement
    private PluginRepositoryManager pluginRepositoryManager;

    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Requirement
    private MimeUtil mimeUtil;

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Requirement
    private PlexusComponentGleaner plexusComponentGleaner;

    private final Map<GAVCoordinate, PluginDescriptor> activatedPlugins =
        new HashMap<GAVCoordinate, PluginDescriptor>();

    private final Map<GAVCoordinate, PluginResponse> pluginActions = new HashMap<GAVCoordinate, PluginResponse>();

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

    public Map<GAVCoordinate, PluginDescriptor> getActivatedPlugins()
    {
        return Collections.unmodifiableMap( new HashMap<GAVCoordinate, PluginDescriptor>( activatedPlugins ) );
    }

    public Map<GAVCoordinate, PluginMetadata> getInstalledPlugins()
    {
        return Collections.unmodifiableMap( pluginRepositoryManager.findAvailablePlugins() );
    }

    public Map<GAVCoordinate, PluginResponse> getPluginResponses()
    {
        return Collections.unmodifiableMap( new HashMap<GAVCoordinate, PluginResponse>( pluginActions ) );
    }

    public boolean installPluginBundle( File bundle )
        throws IOException
    {
        NexusPluginRepository userRepository =
            pluginRepositoryManager.getNexusPluginRepository( UserNexusPluginRepository.ID );

        if ( userRepository instanceof NexusWritablePluginRepository )
        {
            NexusWritablePluginRepository writableUserRepository = (NexusWritablePluginRepository) userRepository;

            return writableUserRepository.installPluginBundle( bundle );
        }

        return false;
    }

    public boolean uninstallPluginBundle( GAVCoordinate coords )
        throws IOException
    {
        if ( isActivatedPlugin( coords ) )
        {
            PluginManagerResponse result = deactivatePlugin( coords );

            if ( !result.isSuccessful() )
            {
                return false;
            }
        }

        NexusPluginRepository userRepository =
            pluginRepositoryManager.getNexusPluginRepository( UserNexusPluginRepository.ID );

        if ( userRepository instanceof NexusWritablePluginRepository )
        {
            NexusWritablePluginRepository writableUserRepository = (NexusWritablePluginRepository) userRepository;

            return writableUserRepository.deletePluginBundle( coords );
        }

        return false;
    }

    public boolean isActivatedPlugin( GAVCoordinate coords )
    {
        return getActivatedPlugins().containsKey( coords );
    }

    public Collection<PluginManagerResponse> activateInstalledPlugins()
    {
        Map<GAVCoordinate, PluginMetadata> availablePlugins = pluginRepositoryManager.findAvailablePlugins();

        ArrayList<PluginManagerResponse> result = new ArrayList<PluginManagerResponse>( availablePlugins.size() );

        for ( GAVCoordinate pluginCoordinate : availablePlugins.keySet() )
        {
            result.add( activatePlugin( pluginCoordinate ) );
        }

        return result;
    }

    public PluginManagerResponse activatePlugin( GAVCoordinate pluginCoordinate )
    {
        PluginManagerResponse response = new PluginManagerResponse( pluginCoordinate, PluginActivationRequest.ACTIVATE );

        if ( getActivatedPlugins().containsKey( pluginCoordinate ) )
        {
            // already is active, let's play dumb

            return response;
        }

        try
        {
            PluginRepositoryArtifact pluginArtifact = pluginRepositoryManager.resolveArtifact( pluginCoordinate );

            doActivatePlugin( response, pluginArtifact );

            return response;
        }
        catch ( NoSuchPluginRepositoryArtifactException e )
        {
            PluginResponse result = new PluginResponse( pluginCoordinate, PluginActivationRequest.ACTIVATE );

            result.setThrowable( new NoSuchPluginException( pluginCoordinate ) );

            result.setAchievedGoal( PluginActivationResult.MISSING );

            this.pluginActions.put( pluginCoordinate, result );

            response.addPluginResponse( result );

            return response;
        }
    }

    public PluginManagerResponse deactivatePlugin( GAVCoordinate pluginCoordinates )
    {
        PluginManagerResponse response =
            new PluginManagerResponse( pluginCoordinates, PluginActivationRequest.DEACTIVATE );

        PluginResponse result = new PluginResponse( pluginCoordinates, PluginActivationRequest.DEACTIVATE );

        try
        {
            if ( getActivatedPlugins().containsKey( pluginCoordinates ) )
            {
                PluginDescriptor pluginDescriptor = getActivatedPlugins().get( pluginCoordinates );

                // scan all activated plugins and look if some points at us
                for ( PluginDescriptor activePlugin : getActivatedPlugins().values() )
                {
                    if ( activePlugin.getImportedPlugins().contains( pluginDescriptor ) )
                    {
                        // this one points to us, so do a recursive call but save the results of it
                        response.addPluginManagerResponse( deactivatePlugin( activePlugin.getPluginCoordinates() ) );
                    }
                }

                // unregister discovered repo types
                for ( PluginRepositoryType repoType : pluginDescriptor.getPluginRepositoryTypes().values() )
                {
                    RepositoryTypeDescriptor repoTypeDescriptor = new RepositoryTypeDescriptor();

                    repoTypeDescriptor.setRole( repoType.getComponentContract() );

                    repoTypeDescriptor.setPrefix( repoType.getPathPrefix() );

                    repositoryTypeRegistry.unregisterRepositoryTypeDescriptors( repoTypeDescriptor );
                }

                // kill it
                plexusContainer.removeComponentRealm( pluginDescriptor.getPluginRealm() );

                // send notification
                applicationEventMulticaster.notifyEventListeners( new PluginDeactivatedEvent( this, pluginDescriptor ) );

                result.setAchievedGoal( PluginActivationResult.DEACTIVATED );
            }
            else
            {
                result.setThrowable( new NoSuchPluginException( pluginCoordinates ) );

                result.setAchievedGoal( PluginActivationResult.MISSING );
            }
        }
        catch ( PlexusContainerException e )
        {
            result.setThrowable( e );
        }

        response.addPluginResponse( result );

        return response;
    }

    // ==

    protected void doActivatePlugin( PluginManagerResponse response, PluginRepositoryArtifact pluginArtifact )
    {
        GAVCoordinate pluginCoordinates = pluginArtifact.getCoordinate();

        PluginResponse result = new PluginResponse( pluginCoordinates, PluginActivationRequest.ACTIVATE );

        PluginDescriptor pluginDescriptor = null;

        PluginDiscoveryContext discoveryContext = null;

        try
        {
            File pluginFile = pluginArtifact.getFile();

            if ( pluginFile == null || !pluginFile.exists() )
            {
                // this is not a nexus plugin!
                result.setThrowable( new NoSuchPluginException( pluginCoordinates ) );

                result.setAchievedGoal( PluginActivationResult.MISSING );

                response.addPluginResponse( result );

                this.pluginActions.put( pluginCoordinates, result );

                return;
            }

            // create validator
            NexusPluginValidator validator = new DefaultNexusPluginValidator();

            // scan the jar
            pluginDescriptor = scanPluginJar( pluginCoordinates, pluginArtifact );

            // "old way", since the above way does not work
            pluginDescriptor.setPluginRealm( plexusContainer.createChildRealm( pluginCoordinates.toCompositeForm() ) );

            // add plugin jar to it
            pluginDescriptor.getPluginRealm().addURL( toUrl( pluginFile ) );

            // we will have pluginDescriptor even later the plugin is broken
            result.setPluginDescriptor( pluginDescriptor );

            // create discovery context
            discoveryContext = new PluginDiscoveryContext( pluginDescriptor, validator );

            // resolve inter-plugin deps and gather them
            List<GAVCoordinate> dependencyPlugins =
                new ArrayList<GAVCoordinate>( pluginDescriptor.getPluginMetadata().getPluginDependencies().size() );

            List<GAVCoordinate> brokenDependencyPlugins = new ArrayList<GAVCoordinate>();

            for ( PluginDependency dependency : pluginDescriptor.getPluginMetadata().getPluginDependencies() )
            {
                // we use GAV here only, neglecting CT
                GAVCoordinate depCoord =
                    new GAVCoordinate( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion() );

                if ( !activatedPlugins.containsKey( depCoord ) )
                {
                    // try to activate it in recursion
                    PluginManagerResponse dependencyResponse = activatePlugin( depCoord );

                    response.addPluginManagerResponse( dependencyResponse );

                    if ( !dependencyResponse.isSuccessful() )
                    {
                        brokenDependencyPlugins.add( depCoord );
                    }
                }

                dependencyPlugins.add( depCoord );
            }

            // before going further, we must ensure that we resolved all the dependencies
            if ( !brokenDependencyPlugins.isEmpty() )
            {
                result.setThrowable( new PluginDependencyUnavailableException( brokenDependencyPlugins ) );

                response.addPluginResponse( result );

                this.pluginActions.put( pluginCoordinates, result );

                return;
            }

            // add imports
            for ( GAVCoordinate coord : dependencyPlugins )
            {
                PluginDescriptor importPlugin = getActivatedPlugins().get( coord );

                List<String> exports = importPlugin.getExportedClassnames();

                for ( String export : exports )
                {
                    // import ALL
                    try
                    {
                        pluginDescriptor.getPluginRealm().importFrom( importPlugin.getPluginRealm().getId(), export );
                    }
                    catch ( NoSuchRealmException e )
                    {
                        // will not happen
                    }
                }

                discoveryContext.getPluginDescriptor().getImportedPlugins().add( importPlugin );
            }

            // add classpaths dependencies needed to take part in gleaning
            try
            {
                List<File> dependenciesToBeGleaned = addClasspathDependencies( pluginArtifact, discoveryContext, true );

                for ( File dependency : dependenciesToBeGleaned )
                {
                    pluginDescriptor.getGleanedResources().addAll( extractContents( dependency ) );
                }
            }
            catch ( NoSuchPluginRepositoryArtifactException e )
            {
                result
                    .setThrowable( new DependencyNotFoundException( pluginArtifact.getCoordinate(), e.getCoordinate() ) );

                response.addPluginResponse( result );

                this.pluginActions.put( pluginCoordinates, result );
            }

            // glean the realm with plugin JAR only
            try
            {
                findComponents( discoveryContext );
            }
            catch ( GleanerException e )
            {
                throw new InvalidPluginException( discoveryContext.getPluginDescriptor().getPluginCoordinates(), e );
            }

            // after gleaning, add the rest of the classpath dependencies
            try
            {
                addClasspathDependencies( pluginArtifact, discoveryContext, false );
            }
            catch ( NoSuchPluginRepositoryArtifactException e )
            {
                result
                    .setThrowable( new DependencyNotFoundException( pluginArtifact.getCoordinate(), e.getCoordinate() ) );

                response.addPluginResponse( result );

                this.pluginActions.put( pluginCoordinates, result );
            }

            // ==
            // "real work" starts here

            // do all kind of discoveries needed
            discoverPlexusPluginComponents( discoveryContext );

            // validate it
            validatePlugin( discoveryContext );

            // register it
            registerPlugin( discoveryContext );

            // stuff the result
            result.setPluginDescriptor( discoveryContext.getPluginDescriptor() );

            // set result
            result.setAchievedGoal( PluginActivationResult.ACTIVATED );
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
        if ( !result.isSuccessful() && pluginDescriptor != null && pluginDescriptor.getPluginRealm() != null )
        {
            // drop the realm
            try
            {
                plexusContainer.removeComponentRealm( pluginDescriptor.getPluginRealm() );
            }
            catch ( PlexusContainerException e )
            {
                // TODO: ?
                e.printStackTrace();
            }
        }

        // notifications
        if ( result.isSuccessful() )
        {
            applicationEventMulticaster.notifyEventListeners( new PluginActivatedEvent( this, discoveryContext
                .getPluginDescriptor() ) );
        }
        else
        {
            applicationEventMulticaster.notifyEventListeners( new PluginRejectedEvent( this, pluginCoordinates, result
                .getThrowable() ) );
        }

        response.addPluginResponse( result );

        this.pluginActions.put( pluginCoordinates, result );
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

    protected List<File> addClasspathDependencies( PluginRepositoryArtifact pluginArtifact,
        PluginDiscoveryContext context, boolean hasComponentsFilter )
        throws NoSuchPluginRepositoryArtifactException
    {
        PluginDescriptor pluginDescriptor = context.getPluginDescriptor();

        // get classpath dependecies (not inter-plugin but other libs, jars)
        List<File> dependencies =
            new ArrayList<File>( pluginDescriptor.getPluginMetadata().getClasspathDependencies().size() );

        for ( ClasspathDependency dependency : pluginDescriptor.getPluginMetadata().getClasspathDependencies() )
        {
            if ( dependency.isHasComponents() == hasComponentsFilter )
            {
                GAVCoordinate dependencyCoordinates =
                    new GAVCoordinate( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(),
                        dependency.getClassifier(), dependency.getType() );

                PluginRepositoryArtifact dependencyArtifact =
                    pluginRepositoryManager.resolveDependencyArtifact( pluginArtifact, dependencyCoordinates );

                dependencies.add( dependencyArtifact.getFile() );
            }
        }

        // file the realm
        for ( File dependencyFile : dependencies )
        {
            // TODO: check dependency clashes
            pluginDescriptor.getPluginRealm().addURL( toUrl( dependencyFile ) );
        }

        return dependencies;
    }

    // ==
    // Plugin JAR mungling
    // ==

    protected PluginDescriptor scanPluginJar( GAVCoordinate pluginCoordinates, PluginRepositoryArtifact pluginArtifact )
        throws InvalidPluginException, IOException
    {
        File pluginJar = pluginArtifact.getFile();

        PluginDescriptor pluginDescriptor = new PluginDescriptor();

        pluginDescriptor.setPluginCoordinates( pluginCoordinates );

        PluginMetadata pluginMetadata;
        try
        {
            pluginMetadata = pluginArtifact.getNexusPluginRepository().getPluginMetadata( pluginCoordinates );
        }
        catch ( NoSuchPluginRepositoryArtifactException e )
        {
            throw new InvalidPluginException( pluginCoordinates, e );
        }

        if ( pluginMetadata != null )
        {
            pluginDescriptor.setPluginMetadata( pluginMetadata );
        }
        else
        {
            throw new InvalidPluginException( pluginCoordinates, "The file \"" + pluginJar.getAbsolutePath()
                + "\" is not a Nexus Plugin, it does not have plugin metadata!" );
        }

        // extract the contents of the plugin jar, that will be exported
        List<String> exports = extractContents( pluginJar );

        if ( exports != null && exports.size() > 0 )
        {
            // add them to exports
            pluginDescriptor.getExportedResources().addAll( exports );

            // but also to the stuff "to be gleaned"
            pluginDescriptor.getGleanedResources().addAll( exports );
        }

        List<PluginStaticResourceModel> staticResources = extractStaticResources( pluginDescriptor );

        if ( staticResources != null && staticResources.size() > 0 )
        {
            pluginDescriptor.getPluginStaticResourceModels().addAll( staticResources );
        }

        return pluginDescriptor;
    }

    protected List<String> extractContents( File pluginJar )
        throws IOException
    {
        if ( pluginJar.isDirectory() )
        {
            return extractDirContents( pluginJar );
        }
        else
        {
            return extractJarContents( pluginJar );
        }
    }

    private List<String> extractDirContents( File pluginDir )
    {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir( pluginDir );
        ds.addDefaultExcludes();
        ds.scan();

        ArrayList<String> result = new ArrayList<String>();
        for ( String entry : ds.getIncludedFiles() )
        {
            result.add(entry);
        }
        return result;
    }

    private List<String> extractJarContents( File pluginJar )
        throws ZipException, IOException
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
                ZipEntry e = (ZipEntry) en.nextElement();

                if ( !e.isDirectory() )
                {
                    String name = e.getName();

                    result.add( name );
                }
            }

            return result;
        }
        finally
        {
            if ( jar != null )
            {
                jar.close();
            }
        }
    }

    protected List<PluginStaticResourceModel> extractStaticResources( PluginDescriptor plugin )
        throws IOException
    {
        ArrayList<PluginStaticResourceModel> result = new ArrayList<PluginStaticResourceModel>();

        for ( String resourceName : plugin.getExportedResources() )
        {
            if ( resourceName.startsWith( "static/" ) )
            {
                PluginStaticResourceModel model =
                    new PluginStaticResourceModel( resourceName, "/" + resourceName, mimeUtil
                        .getMimeType( resourceName ) );

                result.add( model );
            }
        }

        return result;
    }

    // ==
    // Component Discovery
    // ==

    public List<ComponentDescriptor<?>> discoverPlexusPluginComponents( PluginDiscoveryContext pluginDiscoveryContext )
        throws InvalidPluginException, IOException
    {
        try
        {
            List<ComponentDescriptor<?>> discoveredComponentDescriptors = new ArrayList<ComponentDescriptor<?>>();

            discoveredComponentDescriptors.addAll( plexusContainer.discoverComponents( pluginDiscoveryContext
                .getPluginDescriptor().getPluginRealm() ) );

            // collecting
            for ( ComponentDescriptor<?> componentDescriptor : pluginDiscoveryContext.getPluginDescriptor()
                .getComponents() )
            {
                discoveredComponentDescriptors.add( componentDescriptor );
            }

            return discoveredComponentDescriptors;
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

    protected void findComponents( PluginDiscoveryContext pluginDiscoveryContext )
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
    }

    protected void convertPluginMetadata( PluginDiscoveryContext pluginDiscoveryContext )
        throws GleanerException, IOException
    {
        PluginDescriptor pd = pluginDiscoveryContext.getPluginDescriptor();

        for ( String resourceName : pd.getGleanedResources() )
        {
            String className = ClasspathUtils.convertClassBinaryNameToCanonicalName( resourceName );

            if ( className != null && pd.getPluginRealm().getRealmResource( resourceName ) != null )
            {
                PlexusComponentGleanerRequest request =
                    new PlexusComponentGleanerRequest( className, resourceName, pd.getPluginRealm() );

                // ignore implemented interfaces that are not (yet?) on classpath
                request.setIgnoreNotFoundImplementedInterfaces( true );

                // repository type is one more shot
                request.getPluralComponentAnnotations().add( RepositoryType.class );

                // listen for repository types
                // RepositoryType: we have to register those with RepositoryTypeRegistry
                request.getMarkerAnnotations().add( RepositoryType.class );

                // TODO: can we detect these in some smarter way?
                // ie. a class could be marked as @Component, but the build may not have been used plexus plugin!
                // Component: we want to _avoid_ them, since they are most probably processed by plexus plugin!
                request.getMarkerAnnotations().add( Component.class );

                PlexusComponentGleanerResponse response = plexusComponentGleaner.glean( request );

                if ( response != null && !response.getMarkerAnnotations().containsKey( Component.class ) )
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
            }
        }

        // register NexusResourceBundle if needed
        if ( !pd.getPluginStaticResourceModels().isEmpty() )
        {
            // if we have static resources, register a NexusResourceBundle with this plugin too
            ComponentDescriptor<NexusResourceBundle> pluginBundle = new ComponentDescriptor<NexusResourceBundle>();

            pluginBundle.setRole( NexusResourceBundle.class.getName() );
            pluginBundle.setRoleHint( pd.getPluginCoordinates().toCompositeForm() );
            pluginBundle.setImplementation( PluginResourceBundle.class.getName() );

            ComponentRequirement pluginManagerRequirement = new ComponentRequirement();
            pluginManagerRequirement.setRole( NexusPluginManager.class.getName() );
            pluginManagerRequirement.setFieldName( "nexusPluginManager" );
            pluginBundle.addRequirement( pluginManagerRequirement );

            XmlPlexusConfiguration pluginBundleConfiguration = new XmlPlexusConfiguration();
            pluginBundleConfiguration.addChild( "pluginKey", pd.getPluginCoordinates().toCompositeForm() );
            pluginBundle.setConfiguration( pluginBundleConfiguration );

            pd.addComponentDescriptor( pluginBundle );
        }
    }

    // ==
    // Registering plugin to manager, repoTypeRegistry (if needed) and plexus
    // ==

    protected void registerPlugin( PluginDiscoveryContext pluginDiscoveryContext )
        throws InvalidPluginException
    {
        PluginDescriptor pluginDescriptor = pluginDiscoveryContext.getPluginDescriptor();

        // add it to "known" plugins
        if ( !activatedPlugins.containsKey( pluginDescriptor.getPluginCoordinates() ) )
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

                repositoryTypeRegistry.registerRepositoryTypeDescriptors( repoTypeDescriptor );
            }

            // add it to map
            activatedPlugins.put( pluginDescriptor.getPluginCoordinates(), pluginDescriptor );

            // set is as registered
            pluginDiscoveryContext.setPluginRegistered( true );
        }
    }

}
