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
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.plugins.events.PluginActivatedEvent;
import org.sonatype.nexus.plugins.events.PluginDeactivatedEvent;
import org.sonatype.nexus.plugins.events.PluginRejectedEvent;
import org.sonatype.nexus.plugins.plexus.NexusPluginsComponentRepository;
import org.sonatype.nexus.plugins.repository.NoSuchPluginRepositoryArtifactException;
import org.sonatype.nexus.plugins.repository.PluginRepositoryArtifact;
import org.sonatype.nexus.plugins.repository.PluginRepositoryManager;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugin.metadata.gleaner.GleanerException;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleaner;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleanerRequest;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleanerResponse;
import org.sonatype.plugins.model.PluginDependency;
import org.sonatype.plugins.model.PluginMetadata;
import org.sonatype.plugins.model.io.xpp3.PluginModelXpp3Reader;

/**
 * Plugin Manager implementation.
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
    private MimeUtil mimeUtil;

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Requirement
    private PlexusComponentGleaner plexusComponentGleaner;

    private final Map<GAVCoordinate, PluginDescriptor> activatedPlugins =
        new HashMap<GAVCoordinate, PluginDescriptor>();

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

    public Map<GAVCoordinate, PluginMetadata> getAvailablePlugins()
    {
        return Collections.unmodifiableMap( pluginRepositoryManager.findAvailablePlugins() );
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

    public boolean installPluginBundle( File bundle )
        throws IOException
    {
        // TODO
        return false;
    }

    public boolean uninstallPluginBundle( GAVCoordinate coords )
        throws IOException
    {
        // TODO
        return false;
    }

    public PluginManagerResponse activatePlugin( GAVCoordinate pluginCoordinate )
    {
        if ( getActivatedPlugins().containsKey( pluginCoordinate ) )
        {
            PluginManagerResponse response = new PluginManagerResponse( pluginCoordinate );

            return response;
        }

        try
        {
            PluginRepositoryArtifact pluginArtifact = pluginRepositoryManager.resolveArtifact( pluginCoordinate );

            return doActivatePlugin( pluginArtifact );
        }
        catch ( NoSuchPluginRepositoryArtifactException e )
        {
            PluginResponse result = new PluginResponse( pluginCoordinate, PluginActivationResult.ACTIVATED );

            result.setThrowable( new NoSuchPluginException( pluginCoordinate ) );

            PluginManagerResponse response = new PluginManagerResponse( pluginCoordinate );

            response.addPluginResponse( result );

            return response;
        }
    }

    public PluginManagerResponse deactivatePlugin( GAVCoordinate pluginCoordinates )
    {
        PluginManagerResponse response = new PluginManagerResponse( pluginCoordinates );

        PluginResponse result = new PluginResponse( pluginCoordinates, PluginActivationResult.DEACTIVATED );

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

        response.addPluginResponse( result );

        return response;
    }

    // ==

    protected PluginManagerResponse doActivatePlugin( PluginRepositoryArtifact pluginArtifact )
    {
        PluginManagerResponse response = new PluginManagerResponse( pluginArtifact.getCoordinate() );

        GAVCoordinate pluginCoordinates = pluginArtifact.getCoordinate();

        PluginResponse result = new PluginResponse( pluginCoordinates, PluginActivationResult.ACTIVATED );

        PluginDescriptor pluginDescriptor = null;

        PluginDiscoveryContext discoveryContext = null;

        try
        {
            File pluginFile = pluginArtifact.getFile();

            if ( pluginFile == null || !pluginFile.isFile() )
            {
                // this is not a nexus plugin!
                result.setThrowable( new NoSuchPluginException( pluginCoordinates ) );

                response.addPluginResponse( result );

                return response;
            }

            // create validator
            NexusPluginValidator validator = new DefaultNexusPluginValidator();

            // scan the jar
            pluginDescriptor = scanPluginJar( pluginCoordinates, pluginFile );

            // create plugin realm as container child
            pluginDescriptor.setPluginRealm( plexusContainer.createChildRealm( pluginCoordinates.toCompositeForm() ) );

            // add plugin jar to it
            pluginDescriptor.getPluginRealm().addURL( toUrl( pluginFile ) );

            // create discovery context
            discoveryContext = new PluginDiscoveryContext( pluginDescriptor, validator );

            // resolve inter-plugin deps
            List<GAVCoordinate> dependencyPlugins =
                new ArrayList<GAVCoordinate>( pluginDescriptor.getPluginMetadata().getPluginDependencies().size() );

            for ( PluginDependency dependency : pluginDescriptor.getPluginMetadata().getPluginDependencies() )
            {
                // we use GAV here only, neglecting CT
                GAVCoordinate depCoord =
                    new GAVCoordinate( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion() );

                if ( !activatedPlugins.containsKey( depCoord ) )
                {
                    // try to activate it in recursion
                    response.addPluginManagerResponse( activatePlugin( depCoord ) );
                }
            }

            // before going further, we must ensure that we resolved all the dependencies
            if ( !response.isSuccessful() )
            {
                return response;
            }

            // add imports
            for ( GAVCoordinate coord : dependencyPlugins )
            {
                PluginDescriptor importPlugin = getActivatedPlugins().get( coord );

                List<String> exports = importPlugin.getExports();

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

            // get classpath dependecies (not inter-plugin but other libs, jars)
            List<File> dependencies =
                new ArrayList<File>( pluginDescriptor.getPluginMetadata().getClasspathDependencies().size() );

            for ( PluginDependency dependency : pluginDescriptor.getPluginMetadata().getClasspathDependencies() )
            {
                GAVCoordinate dependencyCoordinates =
                    new GAVCoordinate( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(),
                                       dependency.getClassifier(), dependency.getType() );

                try
                {
                    PluginRepositoryArtifact dependencyArtifact =
                        pluginRepositoryManager.resolveDependencyArtifact( pluginArtifact, dependencyCoordinates );

                    dependencies.add( dependencyArtifact.getFile() );
                }
                catch ( NoSuchPluginRepositoryArtifactException e )
                {
                    result.setThrowable( new DependencyNotFoundException( pluginCoordinates, dependencyCoordinates ) );

                    response.addPluginResponse( result );
                }
            }

            // file the realm
            for ( File dependencyFile : dependencies )
            {
                // TODO: check dependency clashes
                pluginDescriptor.getPluginRealm().addURL( toUrl( dependencyFile ) );
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

        return response;
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

    // ==
    // Plugin JAR mungling
    // ==

    protected PluginDescriptor scanPluginJar( GAVCoordinate pluginCoordinates, File pluginJar )
        throws InvalidPluginException, IOException
    {
        PluginDescriptor pluginDescriptor = new PluginDescriptor();

        pluginDescriptor.setPluginCoordinates( pluginCoordinates );

        PluginMetadata pluginMetadata = extractPluginMetadata( pluginJar );

        if ( pluginMetadata != null )
        {
            pluginDescriptor.setPluginMetadata( pluginMetadata );
        }
        else
        {
            throw new InvalidPluginException( pluginCoordinates, "The file \"" + pluginJar.getAbsolutePath()
                + "\" is not a Nexus Plugin, it does not have plugin metadata!" );
        }

        List<String> exports = extractExports( pluginJar );

        if ( exports != null && exports.size() > 0 )
        {
            pluginDescriptor.getExports().addAll( exports );
        }

        List<PluginStaticResourceModel> staticResources = extractStaticResources( pluginJar );

        if ( staticResources != null && staticResources.size() > 0 )
        {
            pluginDescriptor.getPluginStaticResourceModels().addAll( staticResources );
        }

        return pluginDescriptor;
    }

    protected PluginMetadata extractPluginMetadata( File pluginJar )
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
                jar.close();
            }
        }
    }

    protected List<String> extractExports( File pluginJar )
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

                    // for now, only classes are in, and META-INF is filtered out
                    if ( name.startsWith( "META-INF" ) )
                    {
                        // skip it
                    }
                    else if ( name.endsWith( ".class" ) )
                    {
                        // class name without ".class"
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
                jar.close();
            }
        }
    }

    protected List<PluginStaticResourceModel> extractStaticResources( File pluginJar )
        throws IOException
    {
        ZipFile jar = null;

        try
        {
            jar = new ZipFile( pluginJar );

            ArrayList<PluginStaticResourceModel> result = new ArrayList<PluginStaticResourceModel>();

            @SuppressWarnings( "unchecked" )
            Enumeration en = jar.entries();

            while ( en.hasMoreElements() )
            {
                ZipEntry e = (ZipEntry) en.nextElement();

                if ( !e.isDirectory() )
                {
                    String name = e.getName();

                    if ( name.startsWith( "static/" ) )
                    {
                        PluginStaticResourceModel model =
                            new PluginStaticResourceModel( name, "/" + name, mimeUtil
                                .getMimeType( jarEntryToUrl( pluginJar, name ) ) );

                        result.add( model );
                    }
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

    private URL jarEntryToUrl( File jar, String entryName )
        throws IOException
    {
        // ugh, ugly
        URL result = new URL( "jar:" + toUrl( jar ).toString() + "!/" + entryName );

        return result;
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
