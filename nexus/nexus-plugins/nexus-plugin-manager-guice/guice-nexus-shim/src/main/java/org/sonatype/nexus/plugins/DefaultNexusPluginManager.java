/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.inject.PropertyBinding;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.nexus.scanners.AnnotatedNexusBeanSource;
import org.sonatype.guice.nexus.scanners.AnnotatedNexusComponentScanner;
import org.sonatype.guice.plexus.binders.PlexusBeanManager;
import org.sonatype.guice.plexus.binders.PlexusBindingModule;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.config.Roles;
import org.sonatype.guice.plexus.locators.GuiceBeanLocator;
import org.sonatype.guice.plexus.scanners.XmlPlexusBeanSource;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.plugins.events.PluginActivatedEvent;
import org.sonatype.nexus.plugins.events.PluginRejectedEvent;
import org.sonatype.nexus.plugins.repository.NoSuchPluginRepositoryArtifactException;
import org.sonatype.nexus.plugins.repository.PluginRepositoryArtifact;
import org.sonatype.nexus.plugins.repository.PluginRepositoryManager;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.ClasspathDependency;
import org.sonatype.plugins.model.PluginDependency;
import org.sonatype.plugins.model.PluginMetadata;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Jsr330;

/**
 * Default {@link NexusPluginManager} implementation backed by a {@link PluginRepositoryManager}.
 */
@Component( role = NexusPluginManager.class )
public final class DefaultNexusPluginManager
    implements NexusPluginManager
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    private ClassRealm containerRealm;

    @Inject
    private PluginRepositoryManager repositoryManager;

    @Inject
    private ApplicationEventMulticaster eventMulticaster;

    @Inject
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Inject
    private MimeUtil mimeUtil;

    @Inject
    private GuiceBeanLocator beanLocator;

    @Inject
    private NexusBeanManager beanManager;

    @Inject
    private Injector rootInjector;

    @Inject
    @Named( PlexusConstants.PLEXUS_KEY )
    @SuppressWarnings( "unchecked" )
    private Map variables;

    private final Map<GAVCoordinate, PluginDescriptor> activePlugins = new HashMap<GAVCoordinate, PluginDescriptor>();

    private final Map<GAVCoordinate, PluginResponse> pluginResponses = new HashMap<GAVCoordinate, PluginResponse>();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Map<GAVCoordinate, PluginDescriptor> getActivatedPlugins()
    {
        return new HashMap<GAVCoordinate, PluginDescriptor>( activePlugins );
    }

    public Map<GAVCoordinate, PluginMetadata> getInstalledPlugins()
    {
        return repositoryManager.findAvailablePlugins();
    }

    public Map<GAVCoordinate, PluginResponse> getPluginResponses()
    {
        return new HashMap<GAVCoordinate, PluginResponse>( pluginResponses );
    }

    public Collection<PluginManagerResponse> activateInstalledPlugins()
    {
        final List<PluginManagerResponse> result = new ArrayList<PluginManagerResponse>();
        for ( final GAVCoordinate gav : repositoryManager.findAvailablePlugins().keySet() )
        {
            result.add( activatePlugin( gav ) );
        }
        return result;
    }

    public boolean isActivatedPlugin( final GAVCoordinate gav )
    {
        return activePlugins.containsKey( gav );
    }

    public PluginManagerResponse activatePlugin( final GAVCoordinate gav )
    {
        final PluginManagerResponse response = new PluginManagerResponse( gav, PluginActivationRequest.ACTIVATE );
        if ( !activePlugins.containsKey( gav ) )
        {
            try
            {
                activatePlugin( repositoryManager.resolveArtifact( gav ), response );
            }
            catch ( final NoSuchPluginRepositoryArtifactException e )
            {
                reportMissingPlugin( response, e );
            }
        }
        return response;
    }

    public PluginManagerResponse deactivatePlugin( final GAVCoordinate gav )
    {
        throw new UnsupportedOperationException(); // TODO
    }

    public boolean installPluginBundle( final URL bundle )
        throws IOException
    {
        throw new UnsupportedOperationException(); // TODO
    }

    public boolean uninstallPluginBundle( final GAVCoordinate gav )
        throws IOException
    {
        throw new UnsupportedOperationException(); // TODO
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private void activatePlugin( final PluginRepositoryArtifact plugin, final PluginManagerResponse response )
        throws NoSuchPluginRepositoryArtifactException
    {
        final GAVCoordinate pluginGAV = plugin.getCoordinate();
        final PluginMetadata metadata = plugin.getPluginMetadata();

        final PluginDescriptor descriptor = new PluginDescriptor( pluginGAV );
        descriptor.setPluginMetadata( metadata );

        final PluginResponse result = new PluginResponse( pluginGAV, PluginActivationRequest.ACTIVATE );
        result.setPluginDescriptor( descriptor );

        activePlugins.put( pluginGAV, descriptor );

        final List<GAVCoordinate> importList = new ArrayList<GAVCoordinate>();
        for ( final PluginDependency pd : metadata.getPluginDependencies() )
        {
            final GAVCoordinate gav = new GAVCoordinate( pd.getGroupId(), pd.getArtifactId(), pd.getVersion() );
            response.addPluginManagerResponse( activatePlugin( gav ) );
            importList.add( gav );
        }
        descriptor.setImportedPlugins( importList );

        if ( !response.isSuccessful() )
        {
            result.setAchievedGoal( PluginActivationResult.BROKEN );
        }
        else
        {
            try
            {
                beanLocator.add( createPluginInjector( plugin, descriptor ) );
                result.setAchievedGoal( PluginActivationResult.ACTIVATED );
            }
            catch ( final Throwable e )
            {
                result.setThrowable( e );
            }
        }

        reportActivationResult( response, result );
    }

    private Injector createPluginInjector( final PluginRepositoryArtifact plugin, final PluginDescriptor descriptor )
        throws NoSuchPluginRepositoryArtifactException, IOException
    {
        final String realmId = descriptor.getPluginCoordinates().toString();
        ClassRealm pluginRealm;
        try
        {
            pluginRealm = containerRealm.createChildRealm( realmId );
        }
        catch ( final DuplicateRealmException e1 )
        {
            try
            {
                pluginRealm = containerRealm.getWorld().getRealm( realmId );
            }
            catch ( final NoSuchRealmException e2 )
            {
                throw new IllegalStateException();
            }
        }

        final List<URL> scanList = new ArrayList<URL>();

        final URL pluginURL = toURL( plugin );
        if ( null != pluginURL )
        {
            pluginRealm.addURL( pluginURL );
            scanList.add( pluginURL );
        }

        for ( final ClasspathDependency d : descriptor.getPluginMetadata().getClasspathDependencies() )
        {
            final GAVCoordinate gav =
                new GAVCoordinate( d.getGroupId(), d.getArtifactId(), d.getVersion(), d.getClassifier(), d.getType() );

            final URL url = toURL( repositoryManager.resolveDependencyArtifact( plugin, gav ) );
            if ( null != url )
            {
                pluginRealm.addURL( url );
                if ( d.isHasComponents() )
                {
                    scanList.add( url );
                }
            }
        }

        for ( final GAVCoordinate gav : descriptor.getImportedPlugins() )
        {
            final String importId = gav.toString();
            for ( final String classname : activePlugins.get( gav ).getExportedClassnames() )
            {
                try
                {
                    pluginRealm.importFrom( importId, classname );
                }
                catch ( final NoSuchRealmException e )
                {
                    // should never happen
                }
            }
        }

        final List<String> exportedClassNames = new ArrayList<String>();
        final List<RepositoryTypeDescriptor> repositoryTypes = new ArrayList<RepositoryTypeDescriptor>();
        final List<PluginStaticResource> staticResources = new ArrayList<PluginStaticResource>();

        final NexusResourceBundle resourceBundle = new NexusResourceBundle()
        {
            @SuppressWarnings( "unchecked" )
            public List getContributedResouces()
            {
                return staticResources;
            }
        };

        final Module resourceBindings = new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( NexusResourceBundle.class ).annotatedWith( Jsr330.named( realmId ) ).toInstance( resourceBundle );
            }
        };

        final ClassSpace pluginSpace = new URLClassSpace( pluginRealm );
        final PlexusBeanSource xmlSource = new XmlPlexusBeanSource( pluginSpace, variables );

        final AnnotatedNexusComponentScanner scanner =
            new AnnotatedNexusComponentScanner( repositoryTypes, exportedClassNames );

        final ClassSpace annSpace = new URLClassSpace( pluginRealm, scanList.toArray( new URL[scanList.size()] ) );
        final PlexusBeanSource annSource = new AnnotatedNexusBeanSource( annSpace, variables, scanner );

        final Module pluginBindings = new PlexusBindingModule( beanManager, xmlSource, annSource );
        final Injector pluginInjector = rootInjector.createChildInjector( pluginBindings, resourceBindings );

        descriptor.setExportedClassnames( exportedClassNames );

        for ( final RepositoryTypeDescriptor r : repositoryTypes )
        {
            repositoryTypeRegistry.registerRepositoryTypeDescriptors( r );
        }
        descriptor.setRepositoryTypes( repositoryTypes );

        final Enumeration<URL> e = pluginSpace.findEntries( "static/", null, true );
        while ( e.hasMoreElements() )
        {
            final URL url = e.nextElement();
            final String path = getPublishedPath( url );
            if ( path != null )
            {
                staticResources.add( new PluginStaticResource( url, path, mimeUtil.getMimeType( url ) ) );
            }
        }
        descriptor.setStaticResources( staticResources );

        return pluginInjector;
    }

    private URL toURL( final PluginRepositoryArtifact artifact )
    {
        try
        {
            return artifact.getFile().toURI().toURL();
        }
        catch ( final MalformedURLException e )
        {
            return null; // should never happen
        }
    }

    private String getPublishedPath( final URL resourceURL )
    {
        final String path = resourceURL.toExternalForm();
        final int index = path.indexOf( "jar!/" );
        return index > 0 ? path.substring( index + 4 ) : null;
    }

    private void reportMissingPlugin( final PluginManagerResponse response,
                                      final NoSuchPluginRepositoryArtifactException cause )
    {
        final GAVCoordinate gav = cause.getCoordinate();
        final PluginResponse result = new PluginResponse( gav, response.getRequest() );
        result.setThrowable( cause );
        result.setAchievedGoal( PluginActivationResult.MISSING );

        response.addPluginResponse( result );
        pluginResponses.put( gav, result );
    }

    private void reportActivationResult( final PluginManagerResponse response, final PluginResponse result )
    {
        final Event<NexusPluginManager> pluginEvent;
        final GAVCoordinate gav = result.getPluginCoordinates();
        if ( result.isSuccessful() )
        {
            pluginEvent = new PluginActivatedEvent( this, result.getPluginDescriptor() );
        }
        else
        {
            pluginEvent = new PluginRejectedEvent( this, gav, result.getThrowable() );
            activePlugins.remove( gav );
        }

        response.addPluginResponse( result );
        pluginResponses.put( gav, result );

        eventMulticaster.notifyEventListeners( pluginEvent );
    }

    private static final class NexusBeanManager
        implements PlexusBeanManager
    {
        @Inject
        private PlexusBeanManager originalManager;

        @Inject
        private Injector rootInjector;

        public boolean manage( final Component component, final DeferredClass<?> clazz )
        {
            originalManager.manage( component, clazz );

            return null == rootInjector.getExistingBinding( Roles.componentKey( component ) );
        }

        public boolean manage( final Class<?> clazz )
        {
            return originalManager.manage( clazz );
        }

        public PropertyBinding manage( final BeanProperty<?> property )
        {
            return originalManager.manage( property );
        }

        public boolean manage( final Object bean )
        {
            return originalManager.manage( bean );
        }

        public boolean unmanage( final Object bean )
        {
            return originalManager.unmanage( bean );
        }

        public boolean unmanage()
        {
            return originalManager.unmanage();
        }
    }
}
