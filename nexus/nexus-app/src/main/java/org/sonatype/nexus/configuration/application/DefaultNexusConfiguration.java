/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.configuration.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.configuration.Configurable;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationCommitEvent;
import org.sonatype.nexus.configuration.ConfigurationLoadEvent;
import org.sonatype.nexus.configuration.ConfigurationPrepareForLoadEvent;
import org.sonatype.nexus.configuration.ConfigurationPrepareForSaveEvent;
import org.sonatype.nexus.configuration.ConfigurationRollbackEvent;
import org.sonatype.nexus.configuration.ConfigurationSaveEvent;
import org.sonatype.nexus.configuration.application.runtime.ApplicationRuntimeConfigurationBuilder;
import org.sonatype.nexus.configuration.model.CPathMappingItem;
import org.sonatype.nexus.configuration.model.CRemoteNexusInstance;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.source.ApplicationConfigurationSource;
import org.sonatype.nexus.configuration.validator.ApplicationConfigurationValidator;
import org.sonatype.nexus.configuration.validator.ApplicationValidationContext;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.events.VetoFormatter;
import org.sonatype.nexus.proxy.events.VetoFormatterRequest;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.local.DefaultLocalStorageContext;
import org.sonatype.nexus.proxy.storage.local.LocalStorageContext;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.security.SecuritySystem;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * The class DefaultNexusConfiguration is responsible for config management. It actually keeps in sync Nexus internal
 * state with p ersisted user configuration. All changes incoming thru its iface is reflect/maintained in Nexus current
 * state and Nexus user config.
 * 
 * @author cstamas
 */
@Component( role = NexusConfiguration.class )
public class DefaultNexusConfiguration
    extends AbstractLoggingComponent
    implements NexusConfiguration
{
    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Requirement( hint = "file" )
    private ApplicationConfigurationSource configurationSource;

    /** The global local storage context. */
    private LocalStorageContext globalLocalStorageContext;

    /** The global remote storage context. */
    private RemoteStorageContext globalRemoteStorageContext;

    @Requirement
    private GlobalRemoteConnectionSettings globalRemoteConnectionSettings;

    @Requirement
    private GlobalHttpProxySettings globalHttpProxySettings;

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

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement( role = ScheduledTaskDescriptor.class )
    private List<ScheduledTaskDescriptor> scheduledTaskDescriptors;

    @Requirement
    private SecuritySystem securitySystem;

    @org.codehaus.plexus.component.annotations.Configuration( value = "${nexus-work}" )
    private File workingDirectory;

    @Requirement
    private VetoFormatter vetoFormatter;

    /** The config dir */
    private File configurationDirectory;

    /** The temp dir */
    private File temporaryDirectory;

    /** Names of the conf files */
    private Map<String, String> configurationFiles;

    /** The default maxInstance count */
    private int defaultRepositoryMaxInstanceCountLimit = Integer.MAX_VALUE;

    /** The map with per-repotype limitations */
    private Map<RepositoryTypeDescriptor, Integer> repositoryMaxInstanceCountLimits;

    @Requirement
    private List<ConfigurationModifier> configurationModifiers;

    // ==

    public void loadConfiguration()
        throws ConfigurationException, IOException
    {
        loadConfiguration( false );
    }

    public synchronized void loadConfiguration( boolean force )
        throws ConfigurationException, IOException
    {
        if ( force || configurationSource.getConfiguration() == null )
        {
            getLogger().info( "Loading Nexus Configuration..." );

            configurationSource.loadConfiguration();

            boolean modified = false;
            for ( ConfigurationModifier modifier : configurationModifiers )
            {
                modified |= modifier.apply( configurationSource.getConfiguration() );
            }

            if ( modified )
            {
                configurationSource.backupConfiguration();
                configurationSource.storeConfiguration();
            }

            configurationDirectory = null;

            temporaryDirectory = null;

            globalLocalStorageContext = new DefaultLocalStorageContext( null );

            // create global remote ctx
            // this one has no parent
            globalRemoteStorageContext = new DefaultRemoteStorageContext( null );

            globalRemoteConnectionSettings.configure( this );

            globalRemoteStorageContext.setRemoteConnectionSettings( globalRemoteConnectionSettings );

            globalHttpProxySettings.configure( this );

            globalRemoteStorageContext.setRemoteProxySettings( globalHttpProxySettings );

            ConfigurationPrepareForLoadEvent loadEvent = new ConfigurationPrepareForLoadEvent( this );

            applicationEventMulticaster.notifyEventListeners( loadEvent );

            if ( loadEvent.isVetoed() )
            {
                getLogger().info(
                    vetoFormatter.format( new VetoFormatterRequest( loadEvent, getLogger().isDebugEnabled() ) ) );

                throw new ConfigurationException( "The Nexus configuration is invalid!" );
            }

            applyConfiguration();

            // we successfully loaded config
            applicationEventMulticaster.notifyEventListeners( new ConfigurationLoadEvent( this ) );
        }
    }

    protected String changesToString( final Collection<Configurable> changes )
    {
        final StringBuilder sb = new StringBuilder();

        if ( changes != null )
        {
            sb.append( Collections2.transform( changes, new Function<Configurable, String>()
            {
                @Override
                public String apply( final Configurable input )
                {
                    return input.getName();
                }
            } ) );
        }

        return sb.toString();
    }

    protected void logApplyConfiguration( final Collection<Configurable> changes )
    {
        final String userId = getCurrentUserId();

        if ( changes != null && changes.size() > 0 )
        {
            if ( StringUtils.isBlank( userId ) )
            {
                // should not really happen, we should always have subject (at least anon), but...
                getLogger().info( "Applying Nexus Configuration due to changes in {}...", changesToString( changes ) );
            }
            else
            {
                // usually what happens on config change
                getLogger().info( "Applying Nexus Configuration due to changes in {} made by {}...",
                    changesToString( changes ), userId );
            }
        }
        else
        {
            if ( StringUtils.isBlank( userId ) )
            {
                // usually on boot: no changes since "all" changed, and no subject either
                getLogger().info( "Applying Nexus Configuration..." );
            }
            else
            {
                // inperfection of config framework, ie. on adding new component to config system (new repo)
                getLogger().info( "Applying Nexus Configuration made by {}...", userId );
            }
        }
    }

    protected String getCurrentUserId()
    {
        Subject subject = ThreadContext.getSubject(); // Use ThreadContext directly, SecurityUtils will associate a
                                                      // new Subject with the thread.
        if ( subject != null && subject.getPrincipal() != null )
        {
            return subject.getPrincipal().toString();
        }
        else
        {
            return null;
        }
    }

    public synchronized boolean applyConfiguration()
    {
        getLogger().debug( "Applying Nexus Configuration..." );

        ConfigurationPrepareForSaveEvent prepare = new ConfigurationPrepareForSaveEvent( this );

        applicationEventMulticaster.notifyEventListeners( prepare );

        if ( !prepare.isVetoed() )
        {
            logApplyConfiguration( prepare.getChanges() );

            configurationDirectory = null;

            temporaryDirectory = null;

            applicationEventMulticaster.notifyEventListeners( new ConfigurationCommitEvent( this ) );

            applicationEventMulticaster.notifyEventListeners( new ConfigurationChangeEvent( this, prepare.getChanges(),
                getCurrentUserId() ) );

            return true;
        }
        else
        {
            getLogger().info( vetoFormatter.format( new VetoFormatterRequest( prepare, getLogger().isDebugEnabled() ) ) );

            applicationEventMulticaster.notifyEventListeners( new ConfigurationRollbackEvent( this ) );

            return false;
        }
    }

    public synchronized void saveConfiguration()
        throws IOException
    {
        if ( applyConfiguration() )
        {
            // TODO: when NEXUS-2215 is fixed, this should be remove/moved/cleaned
            // START <<<
            // validate before we do anything
            ValidationRequest request = new ValidationRequest( configurationSource.getConfiguration() );
            ValidationResponse response = configurationValidator.validateModel( request );
            if ( !response.isValid() )
            {
                this.getLogger().error( "Saving nexus configuration caused unexpected error:\n" + response.toString() );
                throw new IOException( "Saving nexus configuration caused unexpected error:\n" + response.toString() );
            }
            // END <<<

            configurationSource.storeConfiguration();

            // we successfully saved config
            applicationEventMulticaster.notifyEventListeners( new ConfigurationSaveEvent( this ) );
        }
    }

    @Deprecated
    // see above
    protected void applyAndSaveConfiguration()
        throws IOException
    {
        saveConfiguration();
    }

    @Deprecated
    public Configuration getConfigurationModel()
    {
        return configurationSource.getConfiguration();
    }

    public ApplicationConfigurationSource getConfigurationSource()
    {
        return configurationSource;
    }

    public boolean isInstanceUpgraded()
    {
        return configurationSource.isInstanceUpgraded();
    }

    public boolean isConfigurationUpgraded()
    {
        return configurationSource.isConfigurationUpgraded();
    }

    public boolean isConfigurationDefaulted()
    {
        return configurationSource.isConfigurationDefaulted();
    }

    public LocalStorageContext getGlobalLocalStorageContext()
    {
        return globalLocalStorageContext;
    }

    public RemoteStorageContext getGlobalRemoteStorageContext()
    {
        return globalRemoteStorageContext;
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

            getLogger().error( message );
        }

        return workingDirectory;
    }

    public File getWorkingDirectory( String key )
    {
        return getWorkingDirectory( key, true );
    }

    public File getWorkingDirectory( final String key, final boolean createIfNeeded )
    {
        File keyedDirectory = new File( getWorkingDirectory(), key );

        if ( createIfNeeded )
        {
            if ( !keyedDirectory.isDirectory() && !keyedDirectory.mkdirs() )
            {
                String message =
                    "\r\n******************************************************************************\r\n"
                        + "* Could not create work directory [ "
                        + keyedDirectory.toString()
                        + "]!!!! *\r\n"
                        + "* Nexus cannot start properly until the process has read+write permissions to this folder *\r\n"
                        + "******************************************************************************";

                getLogger().error( message );
            }
        }

        return keyedDirectory;
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

    @Deprecated
    public Repository createRepositoryFromModel( CRepository repository )
        throws ConfigurationException
    {
        return runtimeConfigurationBuilder.createRepositoryFromModel( getConfigurationModel(), repository );
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

    public void setAnonymousUsername( String val )
        throws org.sonatype.configuration.validation.InvalidConfigurationException
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
    }

    public void dropInternals()
    {
        dropRepositories();
    }

    protected void createRepositories()
        throws ConfigurationException
    {
        List<CRepository> reposes = getConfigurationModel().getRepositories();

        for ( CRepository repo : reposes )
        {

            if ( !repo.getProviderRole().equals( GroupRepository.class.getName() ) )
            {
                instantiateRepository( getConfigurationModel(), repo );
            }
        }

        for ( CRepository repo : reposes )
        {
            if ( repo.getProviderRole().equals( GroupRepository.class.getName() ) )
            {
                instantiateRepository( getConfigurationModel(), repo );
            }
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

    protected Repository instantiateRepository( final Configuration configuration, final CRepository repositoryModel )
        throws ConfigurationException
    {
        checkRepositoryMaxInstanceCountForCreation( repositoryModel );

        // create it, will do runtime validation
        Repository repository = runtimeConfigurationBuilder.createRepositoryFromModel( configuration, repositoryModel );

        // register with repoRegistry
        repositoryRegistry.addRepository( repository );

        // give it back
        return repository;
    }

    protected void releaseRepository( final Repository repository, final Configuration configuration,
                                      final CRepository repositoryModel )
        throws ConfigurationException
    {
        // release it
        runtimeConfigurationBuilder.releaseRepository( repository, configuration, repositoryModel );
    }

    // ------------------------------------------------------------------
    // CRUD-like ops on config sections
    // Globals are mandatory: RU

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

        List<CRepository> repositories = getConfigurationModel().getRepositories();

        if ( repositories != null )
        {
            for ( CRepository repo : repositories )
            {
                context.getExistingRepositoryIds().add( repo.getId() );
            }
        }
    }

    // ----------------------------------------------------------------------------------------------------------
    // Repositories
    // ----------------------------------------------------------------------------------------------------------

    protected Map<RepositoryTypeDescriptor, Integer> getRepositoryMaxInstanceCountLimits()
    {
        if ( repositoryMaxInstanceCountLimits == null )
        {
            repositoryMaxInstanceCountLimits = new ConcurrentHashMap<RepositoryTypeDescriptor, Integer>();
        }

        return repositoryMaxInstanceCountLimits;
    }

    public void setDefaultRepositoryMaxInstanceCount( int count )
    {
        if ( count < 0 )
        {
            getLogger().info( "Default repository maximal instance limit set to UNLIMITED." );

            this.defaultRepositoryMaxInstanceCountLimit = Integer.MAX_VALUE;
        }
        else
        {
            getLogger().info( "Default repository maximal instance limit set to " + count + "." );

            this.defaultRepositoryMaxInstanceCountLimit = count;
        }
    }

    public void setRepositoryMaxInstanceCount( RepositoryTypeDescriptor rtd, int count )
    {
        if ( count < 0 )
        {
            getLogger().info( "Repository type " + rtd.toString() + " maximal instance limit set to UNLIMITED." );

            getRepositoryMaxInstanceCountLimits().remove( rtd );
        }
        else
        {
            getLogger().info( "Repository type " + rtd.toString() + " maximal instance limit set to " + count + "." );

            getRepositoryMaxInstanceCountLimits().put( rtd, count );
        }
    }

    public int getRepositoryMaxInstanceCount( RepositoryTypeDescriptor rtd )
    {
        Integer limit = getRepositoryMaxInstanceCountLimits().get( rtd );

        if ( null != limit )
        {
            return limit;
        }
        else
        {
            return defaultRepositoryMaxInstanceCountLimit;
        }
    }

    protected void checkRepositoryMaxInstanceCountForCreation( CRepository repositoryModel )
        throws ConfigurationException
    {
        RepositoryTypeDescriptor rtd =
            repositoryTypeRegistry.getRepositoryTypeDescriptor( repositoryModel.getProviderRole(),
                repositoryModel.getProviderHint() );

        int maxCount;

        if ( null == rtd )
        {
            // no check done
            String msg =
                String.format(
                    "Repository \"%s\" (repoId=%s) corresponding type is not registered in Core, hence it's maxInstace check cannot be performed: Repository type %s:%s is unknown to Nexus Core. It is probably contributed by an old Nexus plugin. Please contact plugin developers to upgrade the plugin, and register the new repository type(s) properly!",
                    repositoryModel.getName(), repositoryModel.getId(), repositoryModel.getProviderRole(),
                    repositoryModel.getProviderHint() );

            getLogger().warn( msg );

            return;
        }

        if ( rtd.getRepositoryMaxInstanceCount() != RepositoryType.UNLIMITED_INSTANCES )
        {
            maxCount = rtd.getRepositoryMaxInstanceCount();
        }
        else
        {
            maxCount = getRepositoryMaxInstanceCount( rtd );
        }

        if ( rtd.getInstanceCount() >= maxCount )
        {
            String msg =
                "Repository \"" + repositoryModel.getName() + "\" (id=" + repositoryModel.getId()
                    + ") cannot be created. It's repository type " + rtd.toString() + " is limited to " + maxCount
                    + " instances, and it already has " + String.valueOf( rtd.getInstanceCount() ) + " of them.";

            getLogger().warn( msg );

            throw new ConfigurationException( msg );
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

    public synchronized Repository createRepository( CRepository settings )
        throws ConfigurationException, IOException
    {
        validateRepository( settings, true );

        // create it, will do runtime validation
        Repository repository = instantiateRepository( getConfigurationModel(), settings );

        // now add it to config, since it is validated and successfully created
        getConfigurationModel().addRepository( settings );

        // save
        saveConfiguration();

        return repository;
    }

    public synchronized void deleteRepository( String id )
        throws NoSuchRepositoryException, IOException, ConfigurationException
    {
        Repository repository = repositoryRegistry.getRepository( id );
        // put out of service so wont be accessed any longer
        repository.setLocalStatus( LocalStatus.OUT_OF_SERVICE );
        // disable indexing for same purpose
        repository.setIndexable( false );
        repository.setSearchable( false );

        // remove dependants too

        // =======
        // shadows
        // (fail if any repo references the currently processing one)
        List<ShadowRepository> shadows = repositoryRegistry.getRepositoriesWithFacet( ShadowRepository.class );

        for ( Iterator<ShadowRepository> i = shadows.iterator(); i.hasNext(); )
        {
            ShadowRepository shadow = i.next();

            if ( repository.getId().equals( shadow.getMasterRepository().getId() ) )
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

        List<CPathMappingItem> pathMappings = getConfigurationModel().getRepositoryGrouping().getPathMappings();

        for ( Iterator<CPathMappingItem> i = pathMappings.iterator(); i.hasNext(); )
        {
            CPathMappingItem item = i.next();

            item.removeRepository( id );
        }

        // ===========
        // and finally
        // this cleans it properly from the registry (from reposes and repo groups)
        repositoryRegistry.removeRepository( id );

        List<CRepository> reposes = getConfigurationModel().getRepositories();

        for ( Iterator<CRepository> i = reposes.iterator(); i.hasNext(); )
        {
            CRepository repo = i.next();

            if ( repo.getId().equals( id ) )
            {
                i.remove();

                saveConfiguration();

                releaseRepository( repository, getConfigurationModel(), repo );

                return;
            }
        }

        throw new NoSuchRepositoryException( id );
    }

    // ===

    public Collection<CRemoteNexusInstance> listRemoteNexusInstances()
    {
        List<CRemoteNexusInstance> result = null;

        if ( getConfigurationModel().getRemoteNexusInstances() != null )
        {
            result = Collections.unmodifiableList( getConfigurationModel().getRemoteNexusInstances() );
        }

        return result;
    }

    public CRemoteNexusInstance readRemoteNexusInstance( String alias )
        throws IOException
    {
        List<CRemoteNexusInstance> knownInstances = getConfigurationModel().getRemoteNexusInstances();

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
        getConfigurationModel().addRemoteNexusInstance( settings );

        applyAndSaveConfiguration();
    }

    public void deleteRemoteNexusInstance( String alias )
        throws IOException
    {
        List<CRemoteNexusInstance> knownInstances = getConfigurationModel().getRemoteNexusInstances();

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
