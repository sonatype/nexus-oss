/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.configuration.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.ConfigurationPrepareForSaveEvent;
import org.sonatype.nexus.configuration.application.runtime.ApplicationRuntimeConfigurationBuilder;
import org.sonatype.nexus.configuration.model.CErrorReporting;
import org.sonatype.nexus.configuration.model.CPathMappingItem;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteNexusInstance;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.CRestApiSettings;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.RemoteSettingsUtil;
import org.sonatype.nexus.configuration.source.ApplicationConfigurationSource;
import org.sonatype.nexus.configuration.validator.ApplicationConfigurationValidator;
import org.sonatype.nexus.configuration.validator.ApplicationValidationContext;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.target.Target;
import org.sonatype.nexus.proxy.target.TargetRegistry;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.security.SecuritySystem;

/**
 * The class DefaultNexusConfiguration is responsible for config management. It actually keeps in sync Nexus internal
 * state with p ersisted user configuration. All changes incoming thru its iface is reflect/maintained in Nexus current
 * state and Nexus user config.
 *
 * @author cstamas
 */
@Component( role = NexusConfiguration.class )
public class DefaultNexusConfiguration
    extends AbstractLogEnabled
    implements NexusConfiguration
{
    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    /**
     * The configuration source.
     */
    @Requirement( hint = "file" )
    private ApplicationConfigurationSource configurationSource;

    /**
     * The config validator.
     */
    @Requirement
    private ApplicationConfigurationValidator configurationValidator;

    /**
     * The runtime configuration builder.
     */
    @Requirement
    private ApplicationRuntimeConfigurationBuilder runtimeConfigurationBuilder;

    /**
     * The repository registry.
     */
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    /**
     * The target registry.
     */
    @Requirement
    private TargetRegistry targetRegistry;

    /**
     * The available scheduled task descriptors
     */
    @Requirement( role = ScheduledTaskDescriptor.class )
    private List<ScheduledTaskDescriptor> scheduledTaskDescriptors;

    @Requirement
    private SecuritySystem securitySystem;
    
    /** The global remote storage context. */
    private RemoteStorageContext remoteStorageContext;

    @org.codehaus.plexus.component.annotations.Configuration( value = "${nexus-work}" )
    private File workingDirectory;

    /** The config dir */
    private File configurationDirectory;

    /** The temp dir */
    private File temporaryDirectory;

    /** The trash */
    private File wastebasketDirectory;

    /** Names of the conf files */
    private Map<String, String> configurationFiles;

    public void loadConfiguration()
        throws ConfigurationException, IOException
    {
        loadConfiguration( false );
    }

    public void loadConfiguration( boolean force )
        throws ConfigurationException, IOException
    {
        if ( force || configurationSource.getConfiguration() == null )
        {
            getLogger().info( "Loading Nexus Configuration..." );

            configurationSource.loadConfiguration();

            configurationDirectory = null;

            temporaryDirectory = null;

            wastebasketDirectory = null;

            // create shared remote ctx
            // this one has no parent
            remoteStorageContext = new DefaultRemoteStorageContext( null );

            if ( getConfiguration().getGlobalConnectionSettings() != null )
            {
                RemoteConnectionSettings rcs =
                    RemoteSettingsUtil.convertFromModel( getConfiguration().getGlobalConnectionSettings() );

                remoteStorageContext.setRemoteConnectionSettings( rcs );
            }

            if ( getConfiguration().getGlobalHttpProxySettings() != null )
            {
                RemoteProxySettings rps =
                    RemoteSettingsUtil.convertFromModel( getConfiguration().getGlobalHttpProxySettings() );

                remoteStorageContext.setRemoteProxySettings( rps );
            }

            applyConfiguration();
        }
    }

    public void applyConfiguration()
    {
        getLogger().info( "Applying Nexus Configuration..." );

        ConfigurationPrepareForSaveEvent prepare = new ConfigurationPrepareForSaveEvent( this );

        applicationEventMulticaster.notifyEventListeners( prepare );

        if ( !prepare.isVetoed() )
        {
            configurationDirectory = null;

            temporaryDirectory = null;

            wastebasketDirectory = null;

            applicationEventMulticaster
                .notifyEventListeners( new ConfigurationChangeEvent( this, prepare.getChanges() ) );
        }
        else
        {
            getLogger().info( "... applying was vetoed by: " + prepare.getVetos() );
        }
    }

    public void saveConfiguration()
        throws IOException
    {
        applyConfiguration();

        configurationSource.storeConfiguration();
    }

    @Deprecated
    // see above
    protected void applyAndSaveConfiguration()
        throws IOException
    {
        saveConfiguration();
    }

    public Configuration getConfiguration()
    {
        return configurationSource.getConfiguration();
    }
    
    public ApplicationConfigurationSource getConfigurationSource()
    {
        return configurationSource;
    }

    public boolean isInstanceUpgraded()
    {
        // TODO: this is not quite true: we might keep model ver but upgrade JARs of Nexus only in a release
        // we should store the nexus version somewhere in working storage and trigger some household stuff
        // if version changes.
        return configurationSource.isConfigurationUpgraded();
    }

    public boolean isConfigurationUpgraded()
    {
        return configurationSource.isConfigurationUpgraded();
    }

    public boolean isConfigurationDefaulted()
    {
        return configurationSource.isConfigurationDefaulted();
    }

    public RemoteStorageContext getGlobalRemoteStorageContext()
    {
        return remoteStorageContext;
    }

    public File getWorkingDirectory()
    {
        // Create the dir if doesn't exist, throw runtime exception on failure
        // bad bad bad
        if ( !workingDirectory.exists() && !workingDirectory.mkdirs() )
        {
            String message =
                "\r\n******************************************************************************\r\n"
                    + "* Could not create work directory [ " + workingDirectory.toString() + "]!!!! *\r\n"
                    + "* Nexus cannot start properly until the process has read+write permissions to this folder *\r\n"
                    + "******************************************************************************";

            getLogger().fatalError( message );
        }

        return workingDirectory;
    }

    public File getWorkingDirectory( String key )
    {
        return new File( getWorkingDirectory(), key );
    }

    public File getTemporaryDirectory()
    {
        if ( temporaryDirectory == null )
        {
            temporaryDirectory = new File( System.getProperty( "java.io.tmpdir" ) );

            if ( !temporaryDirectory.exists() )
            {
                temporaryDirectory.mkdirs();
            }
        }
        return temporaryDirectory;
    }

    public File getConfigurationDirectory()
    {
        if ( configurationDirectory == null )
        {
            configurationDirectory = new File( getWorkingDirectory(), "conf" );

            if ( !configurationDirectory.exists() )
            {
                configurationDirectory.mkdirs();
            }

        }
        return configurationDirectory;
    }

    public File getWastebasketDirectory()
    {
        if ( wastebasketDirectory == null )
        {
            wastebasketDirectory = getWorkingDirectory( "trash" );
        }
        return wastebasketDirectory;
    }

    public Repository createRepositoryFromModel( Configuration configuration, CRepository repository )
        throws ConfigurationException
    {
        return runtimeConfigurationBuilder.createRepositoryFromModel( configuration, repository );
    }

    public List<ScheduledTaskDescriptor> listScheduledTaskDescriptors()
    {
        return Collections.unmodifiableList( scheduledTaskDescriptors );
    }

    public ScheduledTaskDescriptor getScheduledTaskDescriptor( String id )
    {
        for ( ScheduledTaskDescriptor descriptor : scheduledTaskDescriptors )
        {
            if ( descriptor.getId().equals( id ) )
            {
                return descriptor;
            }
        }

        return null;
    }

    // ------------------------------------------------------------------
    // Security

    public boolean isSecurityEnabled()
    {
        return getSecuritySystem() != null && getSecuritySystem().isSecurityEnabled();
    }

    public void setSecurityEnabled( boolean enabled )
        throws IOException
    {
        getSecuritySystem().setSecurityEnabled( enabled );
    }

    public void setRealms( List<String> realms )
    throws org.sonatype.configuration.validation.InvalidConfigurationException
    {
        getSecuritySystem().setRealms( realms );
    }

    public boolean isAnonymousAccessEnabled()
    {
        return getSecuritySystem() != null && getSecuritySystem().isAnonymousAccessEnabled();
    }

    public void setAnonymousAccessEnabled( boolean enabled )
    {
        getSecuritySystem().setAnonymousAccessEnabled( enabled );
    }

    public String getAnonymousUsername()
    {
        return getSecuritySystem().getAnonymousUsername();
    }

    public void setAnonymousUsername( String val ) throws org.sonatype.configuration.validation.InvalidConfigurationException
    {
        getSecuritySystem().setAnonymousUsername( val );
    }

    public String getAnonymousPassword()
    {
        return getSecuritySystem().getAnonymousPassword();
    }

    public void setAnonymousPassword( String val )
        throws org.sonatype.configuration.validation.InvalidConfigurationException
    {
        getSecuritySystem().setAnonymousPassword( val );
    }

    public List<String> getRealms()
    {
        return getSecuritySystem().getRealms();
    }

    // ------------------------------------------------------------------
    // Booting

    public void createInternals()
        throws ConfigurationException
    {
        createRepositories();

        createRepositoryTargets();
    }

    public void dropInternals()
    {
        dropRepositories();

        dropRepositoryTargets();
    }

    @SuppressWarnings( "unchecked" )
    protected void createRepositories()
        throws ConfigurationException
    {
        List<CRepository> reposes = getConfiguration().getRepositories();

        for ( CRepository repo : reposes )
        {
            Repository repository = createRepositoryFromModel( getConfiguration(), repo );

            repositoryRegistry.addRepository( repository );
        }
    }

    protected void dropRepositories()
    {
        for ( Repository repository : repositoryRegistry.getRepositories() )
        {
            try
            {
                repositoryRegistry.removeRepositorySilently( repository.getId() );
            }
            catch ( NoSuchRepositoryException e )
            {
                // will not happen
            }
        }
    }

    @SuppressWarnings( "unchecked" )
    protected void createRepositoryTargets()
        throws ConfigurationException
    {
        List<CRepositoryTarget> targets = getConfiguration().getRepositoryTargets();

        if ( targets == null )
        {
            return;
        }

        for ( CRepositoryTarget settings : targets )
        {
            ContentClass contentClass = null;

            for ( ContentClass cl : repositoryTypeRegistry.getContentClasses() )
            {
                if ( settings.getContentClass().equals( cl.getId() ) )
                {
                    contentClass = cl;

                    break;
                }
            }

            if ( contentClass == null )
            {
                throw new ConfigurationException( "Could not find ContentClass with ID='" + settings.getContentClass()
                    + "'" );
            }

            targetRegistry.addRepositoryTarget( new Target( settings.getId(), settings.getName(), contentClass,
                                                            settings.getPatterns() ) );
        }
    }

    protected void dropRepositoryTargets()
    {
        // nothing?
    }

    // ------------------------------------------------------------------
    // REST API

    public String getBaseUrl()
    {
        if ( getConfiguration().getRestApi() != null )
        {
            return getConfiguration().getRestApi().getBaseUrl();
        }
        else
        {
            return null;
        }
    }

    public void setBaseUrl( String baseUrl )
        throws IOException
    {
        if ( getConfiguration().getRestApi() == null )
        {
            getConfiguration().setRestApi( new CRestApiSettings() );
        }

        getConfiguration().getRestApi().setBaseUrl( baseUrl );

        applyAndSaveConfiguration();
    }

    public boolean isForceBaseUrl()
    {
        if ( getConfiguration().getRestApi() != null )
        {
            return getConfiguration().getRestApi().isForceBaseUrl();
        }
        else
        {
            return false;
        }
    }

    public void setForceBaseUrl( boolean force )
        throws IOException
    {
        if ( getConfiguration().getRestApi() == null )
        {
            getConfiguration().setRestApi( new CRestApiSettings() );
        }

        getConfiguration().getRestApi().setForceBaseUrl( force );

        applyAndSaveConfiguration();
    }

    public int getSessionExpiration()
    {
        if ( getConfiguration().getRestApi() != null )
        {
            return getConfiguration().getRestApi().getSessionExpiration();
        }
        else
        {
            return -1;
        }
    }

    public void setSessionExpiration( int value )
        throws IOException
    {
        if ( getConfiguration().getRestApi() == null )
        {
            getConfiguration().setRestApi( new CRestApiSettings() );
        }

        getConfiguration().getRestApi().setSessionExpiration( value );

        applyAndSaveConfiguration();
    }

    // ------------------------------------------------------------------
    // CRUD-like ops on config sections
    // Globals are mandatory: RU

    // CRemoteConnectionSettings are mandatory: RU

    public CRemoteConnectionSettings readGlobalRemoteConnectionSettings()
    {
        return getConfiguration().getGlobalConnectionSettings();
    }

    public void updateGlobalRemoteConnectionSettings( CRemoteConnectionSettings settings )
        throws ConfigurationException, IOException
    {
        remoteStorageContext.setRemoteConnectionSettings( RemoteSettingsUtil.convertFromModel( settings ) );

        getConfiguration().setGlobalConnectionSettings( settings );

        applyAndSaveConfiguration();
    }

    // CRemoteHttpProxySettings are optional: CRUD

    public void createGlobalRemoteHttpProxySettings( CRemoteHttpProxySettings settings )
        throws ConfigurationException, IOException
    {
        remoteStorageContext.setRemoteProxySettings( RemoteSettingsUtil.convertFromModel( settings ) );

        getConfiguration().setGlobalHttpProxySettings( settings );

        applyAndSaveConfiguration();
    }

    public CRemoteHttpProxySettings readGlobalRemoteHttpProxySettings()
    {
        return getConfiguration().getGlobalHttpProxySettings();
    }

    public void updateGlobalRemoteHttpProxySettings( CRemoteHttpProxySettings settings )
        throws ConfigurationException, IOException
    {
        createGlobalRemoteHttpProxySettings( settings );
    }

    public void deleteGlobalRemoteHttpProxySettings()
        throws IOException
    {
        remoteStorageContext.removeRemoteProxySettings();

        getConfiguration().setGlobalHttpProxySettings( null );

        applyAndSaveConfiguration();
    }

    // CRouting are mandatory: RU

    public CRouting readRouting()
    {
        return getConfiguration().getRouting();
    }

    public void updateRouting( CRouting settings )
        throws ConfigurationException, IOException
    {
        getConfiguration().setRouting( settings );

        applyAndSaveConfiguration();
    }

    // CRepository and CreposioryShadow helper

    private ApplicationValidationContext getRepositoryValidationContext()
    {
        ApplicationValidationContext result = new ApplicationValidationContext();

        fillValidationContextRepositoryIds( result );

        return result;
    }

    private void fillValidationContextRepositoryIds( ApplicationValidationContext context )
    {
        context.addExistingRepositoryIds();

        List<CRepository> repositories = getConfiguration().getRepositories();

        if ( repositories != null )
        {
            for ( CRepository repo : repositories )
            {
                context.getExistingRepositoryIds().add( repo.getId() );
            }
        }
    }

    // CRepository: CRUD

    protected void validateRepository( CRepository settings, boolean create )
        throws ConfigurationException
    {
        ApplicationValidationContext ctx = getRepositoryValidationContext();

        if ( !create && !StringUtils.isEmpty( settings.getId() ) )
        {
            // remove "itself" from the list to avoid hitting "duplicate repo" problem
            ctx.getExistingRepositoryIds().remove( settings.getId() );
        }

        ValidationResponse vr = configurationValidator.validateRepository( ctx, settings );

        if ( !vr.isValid() )
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public Repository createRepository( CRepository settings )
        throws ConfigurationException, IOException
    {
        validateRepository( settings, true );

        getConfiguration().addRepository( settings );

        Repository repository = runtimeConfigurationBuilder.createRepositoryFromModel( getConfiguration(), settings );

        repositoryRegistry.addRepository( repository );

        saveConfiguration();

        return repository;
    }

    public void deleteRepository( String id )
        throws NoSuchRepositoryException, IOException, ConfigurationException
    {
        Repository repository = repositoryRegistry.getRepository( id );

        repository.setLocalStatus( LocalStatus.OUT_OF_SERVICE );

        // remove dependants too

        // =======
        // shadows
        // (fail if any repo references the currently processing one)
        List<ShadowRepository> shadows = repositoryRegistry.getRepositoriesWithFacet( ShadowRepository.class );

        for ( Iterator<ShadowRepository> i = shadows.iterator(); i.hasNext(); )
        {
            ShadowRepository shadow = i.next();

            if ( repository.getId().equals( shadow.getMasterRepository() ) )
            {
                throw new ConfigurationException( "The repository with ID " + id
                    + " is not deletable, it has dependant repositories!" );
            }
        }

        // ======
        // groups
        // (correction in config only, registry DOES handle it)
        // since NEXUS-1770, groups are "self maintaining"

        // ===========
        // pahMappings
        // (correction, since registry is completely unaware of this component)

        List<CPathMappingItem> pathMappings = getConfiguration().getRepositoryGrouping().getPathMappings();

        for ( Iterator<CPathMappingItem> i = pathMappings.iterator(); i.hasNext(); )
        {
            CPathMappingItem item = i.next();

            item.removeRepository( id );
        }

        // ===========
        // and finally
        // this cleans it properly from the registry (from reposes and repo groups)
        repositoryRegistry.removeRepository( id );

        List<CRepository> reposes = getConfiguration().getRepositories();

        for ( Iterator<CRepository> i = reposes.iterator(); i.hasNext(); )
        {
            CRepository repo = i.next();

            if ( repo.getId().equals( id ) )
            {
                i.remove();

                applyAndSaveConfiguration();

                return;
            }
        }

        throw new NoSuchRepositoryException( id );
    }

    // CGroupsSettingPathMapping: CRUD

    protected void validateRoutePattern( CPathMappingItem settings )
        throws ConfigurationException
    {
        ValidationResponse res = configurationValidator.validateGroupsSettingPathMappingItem( null, settings );

        if ( !res.isValid() )
        {
            throw new InvalidConfigurationException( res );
        }
    }

    public Collection<CPathMappingItem> listGroupsSettingPathMapping()
    {
        if ( getConfiguration().getRepositoryGrouping() != null
            && getConfiguration().getRepositoryGrouping().getPathMappings() != null )
        {
            return new ArrayList<CPathMappingItem>( getConfiguration().getRepositoryGrouping().getPathMappings() );
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public void createGroupsSettingPathMapping( CPathMappingItem settings )
        throws NoSuchRepositoryException, ConfigurationException, IOException
    {
        validateRoutePattern( settings );

        if ( getConfiguration().getRepositoryGrouping() == null )
        {
            getConfiguration().setRepositoryGrouping( new CRepositoryGrouping() );
        }

        getConfiguration().getRepositoryGrouping().addPathMapping( settings );

        applyAndSaveConfiguration();
    }

    public CPathMappingItem readGroupsSettingPathMapping( String id )
        throws IOException
    {
        List<CPathMappingItem> items = getConfiguration().getRepositoryGrouping().getPathMappings();

        for ( Iterator<CPathMappingItem> i = items.iterator(); i.hasNext(); )
        {
            CPathMappingItem mapping = i.next();

            if ( mapping.getId().equals( id ) )
            {
                return mapping;
            }
        }

        return null;
    }

    public void updateGroupsSettingPathMapping( CPathMappingItem settings )
        throws NoSuchRepositoryException, ConfigurationException, IOException
    {
        validateRoutePattern( settings );

        List<CPathMappingItem> items = getConfiguration().getRepositoryGrouping().getPathMappings();

        for ( Iterator<CPathMappingItem> i = items.iterator(); i.hasNext(); )
        {
            CPathMappingItem mapping = i.next();

            if ( mapping.getId().equals( settings.getId() ) )
            {
                mapping.setRouteType( settings.getRouteType() );

                mapping.setRoutePatterns( settings.getRoutePatterns() );

                mapping.setRepositories( settings.getRepositories() );

                break;
            }
        }

        applyAndSaveConfiguration();
    }

    public void deleteGroupsSettingPathMapping( String id )
        throws IOException
    {
        if ( getConfiguration().getRepositoryGrouping() == null
            || getConfiguration().getRepositoryGrouping().getPathMappings() == null )
        {
            return;
        }

        List<CPathMappingItem> items = getConfiguration().getRepositoryGrouping().getPathMappings();

        for ( Iterator<CPathMappingItem> i = items.iterator(); i.hasNext(); )
        {
            CPathMappingItem mapping = i.next();

            if ( mapping.getId().equals( id ) )
            {
                i.remove();
            }
        }

        applyAndSaveConfiguration();
    }

    // ===

    public Collection<CRemoteNexusInstance> listRemoteNexusInstances()
    {
        List<CRemoteNexusInstance> result = null;

        if ( getConfiguration().getRemoteNexusInstances() != null )
        {
            result = Collections.unmodifiableList( getConfiguration().getRemoteNexusInstances() );
        }

        return result;
    }

    public CRemoteNexusInstance readRemoteNexusInstance( String alias )
        throws IOException
    {
        List<CRemoteNexusInstance> knownInstances = getConfiguration().getRemoteNexusInstances();

        for ( Iterator<CRemoteNexusInstance> i = knownInstances.iterator(); i.hasNext(); )
        {
            CRemoteNexusInstance nexusInstance = i.next();

            if ( nexusInstance.getAlias().equals( alias ) )
            {
                return nexusInstance;
            }
        }

        return null;
    }

    public void createRemoteNexusInstance( CRemoteNexusInstance settings )
        throws IOException
    {
        getConfiguration().addRemoteNexusInstance( settings );

        applyAndSaveConfiguration();
    }

    public void deleteRemoteNexusInstance( String alias )
        throws IOException
    {
        List<CRemoteNexusInstance> knownInstances = getConfiguration().getRemoteNexusInstances();

        for ( Iterator<CRemoteNexusInstance> i = knownInstances.iterator(); i.hasNext(); )
        {
            CRemoteNexusInstance nexusInstance = i.next();

            if ( nexusInstance.getAlias().equals( alias ) )
            {
                i.remove();
            }
        }

        applyAndSaveConfiguration();
    }

    // Repository Targets

    protected void validateCRepositoryTarget( CRepositoryTarget settings, boolean create )
        throws ConfigurationException
    {
        ApplicationValidationContext ctx = null;

        // checking for uniqueness only on CREATE event
        if ( create && getConfiguration().getRepositoryTargets() != null )
        {
            ctx = new ApplicationValidationContext();

            ctx.addExistingRepositoryTargetIds();

            List<CRepositoryTarget> targets = getConfiguration().getRepositoryTargets();

            for ( CRepositoryTarget target : targets )
            {
                ctx.getExistingRepositoryTargetIds().add( target.getId() );
            }
        }

        ValidationResponse res = configurationValidator.validateRepositoryTarget( ctx, settings );

        if ( res.isValid() )
        {
            boolean contentClassExists = false;

            for ( ContentClass cc : repositoryTypeRegistry.getContentClasses() )
            {
                if ( cc.getId().equals( settings.getContentClass() ) )
                {
                    contentClassExists = true;
                    break;
                }
            }

            if ( !contentClassExists )
            {
                throw new ConfigurationException(
                                                  "The Repository Target 'ContentClass' must exists: there is no class with id='"
                                                      + settings.getContentClass() + "'!" );
            }
        }
        else
        {
            throw new InvalidConfigurationException( res );
        }
    }

    public Collection<CRepositoryTarget> listRepositoryTargets()
    {
        List<CRepositoryTarget> result = null;

        if ( getConfiguration().getRepositoryTargets() != null )
        {
            result = Collections.unmodifiableList( getConfiguration().getRepositoryTargets() );
        }

        return result;
    }

    public void createRepositoryTarget( CRepositoryTarget settings )
        throws ConfigurationException, IOException
    {
        validateCRepositoryTarget( settings, true );

        ContentClass contentClass = null;

        for ( ContentClass cl : repositoryTypeRegistry.getContentClasses() )
        {
            if ( settings.getContentClass().equals( cl.getId() ) )
            {
                contentClass = cl;

                break;
            }
        }

        if ( contentClass == null )
        {
            throw new ConfigurationException( "Could not find ContentClass with ID='" + settings.getContentClass()
                + "'" );
        }

        targetRegistry.addRepositoryTarget( new Target( settings.getId(), settings.getName(), contentClass, settings
            .getPatterns() ) );

        getConfiguration().addRepositoryTarget( settings );

        applyAndSaveConfiguration();
    }

    public CRepositoryTarget readRepositoryTarget( String id )
    {
        List<CRepositoryTarget> targets = getConfiguration().getRepositoryTargets();

        for ( Iterator<CRepositoryTarget> i = targets.iterator(); i.hasNext(); )
        {
            CRepositoryTarget target = i.next();

            if ( target.getId().equals( id ) )
            {
                return target;
            }
        }

        return null;
    }

    public void updateRepositoryTarget( CRepositoryTarget settings )
        throws ConfigurationException, IOException
    {
        validateCRepositoryTarget( settings, false );

        CRepositoryTarget oldTarget = readRepositoryTarget( settings.getId() );

        if ( oldTarget != null )
        {
            oldTarget.setContentClass( settings.getContentClass() );

            oldTarget.setName( settings.getName() );

            oldTarget.getPatterns().clear();

            oldTarget.getPatterns().addAll( settings.getPatterns() );

            ContentClass contentClass = null;

            for ( ContentClass cl : repositoryTypeRegistry.getContentClasses() )
            {
                if ( oldTarget.getContentClass().equals( cl.getId() ) )
                {
                    contentClass = cl;

                    break;
                }
            }

            if ( contentClass == null )
            {
                throw new ConfigurationException( "Could not find ContentClass with ID='" + oldTarget.getContentClass()
                    + "'" );
            }

            targetRegistry.addRepositoryTarget( new Target( oldTarget.getId(), oldTarget.getName(), contentClass,
                                                            oldTarget.getPatterns() ) );

            applyAndSaveConfiguration();
        }
        else
        {
            throw new IllegalArgumentException( "Repository target with ID='" + settings.getId() + "' does not exists!" );
        }
    }

    public void deleteRepositoryTarget( String id )
        throws IOException
    {
        targetRegistry.removeRepositoryTarget( id );

        List<CRepositoryTarget> targets = getConfiguration().getRepositoryTargets();

        for ( Iterator<CRepositoryTarget> i = targets.iterator(); i.hasNext(); )
        {
            CRepositoryTarget target = i.next();

            if ( target.getId().equals( id ) )
            {
                i.remove();
            }
        }

        applyAndSaveConfiguration();
    }

    public CSmtpConfiguration readSmtpConfiguration()
    {
        return getConfiguration().getSmtpConfiguration();
    }

    public void updateSmtpConfiguration( CSmtpConfiguration settings )
        throws ConfigurationException, IOException
    {
        getConfiguration().setSmtpConfiguration( settings );

        applyAndSaveConfiguration();
    }
    
    public CErrorReporting readErrorReporting()
    {
        return getConfiguration().getErrorReporting();
    }
    
    public void updateErrorReporting( CErrorReporting errorReporting )
        throws ConfigurationException,
            IOException
    {
        getConfiguration().setErrorReporting( errorReporting );
        
        saveConfiguration();
    }

    public Map<String, String> getConfigurationFiles()
    {
        if ( configurationFiles == null )
        {
            configurationFiles = new HashMap<String, String>();

            File configDirectory = getConfigurationDirectory();

            int key = 1;

            // Tamas:
            // configDirectory.listFiles() may be returning null... in this case, it is 99.9% not true (otherwise nexus
            // would not start at all), but in general, be more explicit about checks.

            if ( configDirectory.isDirectory() && configDirectory.listFiles() != null )
            {
                for ( File file : configDirectory.listFiles() )
                {
                    if ( file.exists() && file.isFile() )
                    {
                        configurationFiles.put( Integer.toString( key ), file.getName() );

                        key++;
                    }
                }
            }
        }
        return configurationFiles;
    }

    public NexusStreamResponse getConfigurationAsStreamByKey( String key )
        throws IOException
    {
        String fileName = getConfigurationFiles().get( key );

        if ( fileName != null )
        {
            File configFile = new File( getConfigurationDirectory(), fileName );

            if ( configFile.canRead() && configFile.isFile() )
            {
                NexusStreamResponse response = new NexusStreamResponse();

                response.setName( fileName );

                if ( fileName.endsWith( ".xml" ) )
                {
                    response.setMimeType( "text/xml" );
                }
                else
                {
                    response.setMimeType( "text/plain" );
                }

                response.setSize( configFile.length() );
                response.setFromByte( 0 );
                response.setBytesCount( configFile.length() );
                response.setInputStream( new FileInputStream( configFile ) );

                return response;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
    
    protected SecuritySystem getSecuritySystem()
    {
        return this.securitySystem;
    }
}
