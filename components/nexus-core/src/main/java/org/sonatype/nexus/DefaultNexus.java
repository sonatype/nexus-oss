/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.index.events.ReindexRepositoriesEvent;
import org.sonatype.nexus.index.events.ReindexRepositoriesRequest;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalRequest;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalResult;
import org.sonatype.nexus.maven.tasks.SnapshotRemover;
import org.sonatype.nexus.plugins.NexusPluginManager;
import org.sonatype.nexus.plugins.PluginManagerResponse;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppingEvent;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.packaging.ArtifactPackagingMapper;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.SynchronizeShadowsTask;
import org.sonatype.nexus.templates.NoSuchTemplateIdException;
import org.sonatype.nexus.templates.TemplateManager;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.security.SecuritySystem;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.annotations.VisibleForTesting;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.slf4j.Logger;

/**
 * The default Nexus implementation.
 *
 * @author Jason van Zyl
 * @author cstamas
 */
@Component(role = Nexus.class)
public class DefaultNexus
    extends AbstractLoggingComponent
    implements Nexus, Initializable, Startable
{

  @Requirement
  private EventBus eventBus;

  @Requirement
  private NexusPluginManager nexusPluginManager;

  /**
   * The nexus configuration.
   */
  @Requirement
  private NexusConfiguration nexusConfiguration;

  /**
   * The repository registry.
   */
  @Requirement
  private RepositoryRegistry repositoryRegistry;

  /**
   * The Scheduler.
   */
  @Requirement
  private NexusScheduler nexusScheduler;

  /**
   * The snapshot remover component.
   */
  @Requirement
  private SnapshotRemover snapshotRemover;

  /**
   * The SecurityConfiguration component.
   */
  @Requirement
  private RepositoryRouter rootRepositoryRouter;

  /**
   * Template manager.
   */
  @Requirement
  private TemplateManager templateManager;

  /**
   * The status holding component.
   */
  @Requirement
  private ApplicationStatusSource applicationStatusSource;

  /**
   * Security component
   */
  @Requirement
  private SecuritySystem securitySystem;

  @Requirement
  private ArtifactPackagingMapper artifactPackagingMapper;

  private static final String MAPPING_PROPERTIES_FILE = "packaging2extension-mapping.properties";

  // ----------------------------------------------------------------------------------------------------------
  // SystemStatus
  // ----------------------------------------------------------------------------------------------------------

  public SystemStatus getSystemStatus() {
    return applicationStatusSource.getSystemStatus();
  }

  public boolean setState(SystemState state) {
    SystemState currentState = getSystemStatus().getState();

    // only Stopped or BrokenConfig Nexus may be started
    if (SystemState.STARTED.equals(state)
        && (SystemState.STOPPED.equals(currentState) || SystemState.BROKEN_CONFIGURATION.equals(currentState))) {
      try {
        start();

        return true;
      }
      catch (StartingException e) {
        getLogger().error("Could not start Nexus! (currentState=" + currentState.toString() + ")", e);
      }

      return false;
    }
    // only Started Nexus may be stopped
    else if (SystemState.STOPPED.equals(state) && SystemState.STARTED.equals(currentState)) {
      try {
        stop();

        return true;
      }
      catch (StoppingException e) {
        getLogger().error("Could not stop STARTED Nexus! (currentState=" + currentState.toString() + ")", e);
      }

      return false;
    }
    else {
      throw new IllegalArgumentException("Illegal STATE: '" + state.toString() + "', currentState='"
          + currentState.toString() + "'");
    }
  }

  // ----------------------------------------------------------------------------------------------------------
  // Config
  // ----------------------------------------------------------------------------------------------------------

  public NexusConfiguration getNexusConfiguration() {
    return nexusConfiguration;
  }

  // ----------------------------------------------------------------------------------------------------------
  // Repositories
  // ----------------------------------------------------------------------------------------------------------

  public StorageItem dereferenceLinkItem(StorageLinkItem item)
      throws NoSuchResourceStoreException, ItemNotFoundException, AccessDeniedException, IllegalOperationException,
             StorageException

  {
    return getRootRouter().dereferenceLink(item);
  }

  public RepositoryRouter getRootRouter() {
    return rootRepositoryRouter;
  }

  // ----------------------------------------------------------------------------
  // Repo maintenance
  // ----------------------------------------------------------------------------

  public void deleteRepository(String id)
      throws NoSuchRepositoryException, IOException, ConfigurationException, AccessDeniedException
  {
    deleteRepository(id, false);
  }

  public void deleteRepository(String id, boolean force)
      throws NoSuchRepositoryException, IOException, ConfigurationException, AccessDeniedException
  {
    Repository repository = repositoryRegistry.getRepository(id);

    if (!force && !repository.isUserManaged()) {
      throw new AccessDeniedException("Not allowed to delete non-user-managed repository '" + id + "'.");
    }

    // delete the configuration
    nexusConfiguration.deleteRepository(id);
  }

  // Maintenance
  // ----------------------------------------------------------------------------

  public NexusStreamResponse getConfigurationAsStream()
      throws IOException
  {
    NexusStreamResponse response = new NexusStreamResponse();

    response.setName("current");

    response.setMimeType("text/xml");

    // TODO:
    response.setSize(0);

    response.setInputStream(nexusConfiguration.getConfigurationSource().getConfigurationAsStream());

    return response;
  }

  @Deprecated
  public void expireAllCaches(ResourceStoreRequest request) {
    for (Repository repository : repositoryRegistry.getRepositories()) {
      if (repository.getLocalStatus().shouldServiceRequest()) {
        repository.expireCaches(request);
      }
    }
  }

  @Deprecated
  public void reindexAllRepositories(String path, boolean fullReindex)
      throws IOException
  {
    this.eventBus.post(new ReindexRepositoriesEvent(this, new ReindexRepositoriesRequest(path, fullReindex)));
  }

  @Deprecated
  public Collection<String> evictAllUnusedProxiedItems(ResourceStoreRequest req, long timestamp)
      throws IOException
  {
    ArrayList<String> result = new ArrayList<String>();

    for (Repository repository : repositoryRegistry.getRepositories()) {
      if (LocalStatus.IN_SERVICE.equals(repository.getLocalStatus())) {
        result.addAll(repository.evictUnusedItems(req, timestamp));
      }
    }

    return result;
  }

  @Deprecated
  public void rebuildMavenMetadataAllRepositories(ResourceStoreRequest req)
      throws IOException
  {
    List<Repository> reposes = repositoryRegistry.getRepositories();

    for (Repository repo : reposes) {
      if (repo instanceof MavenRepository) {
        ((MavenRepository) repo).recreateMavenMetadata(req);
      }
    }
  }

  @Deprecated
  public void rebuildAttributesAllRepositories(ResourceStoreRequest req)
      throws IOException
  {
    List<Repository> reposes = repositoryRegistry.getRepositories();

    for (Repository repo : reposes) {
      repo.recreateAttributes(req, null);
    }
  }

  @Deprecated
  public SnapshotRemovalResult removeSnapshots(SnapshotRemovalRequest request)
      throws NoSuchRepositoryException, IllegalArgumentException
  {
    return snapshotRemover.removeSnapshots(request);
  }

  public Map<String, String> getConfigurationFiles() {
    return nexusConfiguration.getConfigurationFiles();
  }

  public NexusStreamResponse getConfigurationAsStreamByKey(String key)
      throws IOException
  {
    return nexusConfiguration.getConfigurationAsStreamByKey(key);
  }

  // ===========================
  // Nexus Application lifecycle

  public void initialize()
      throws InitializationException
  {
    logInitialize();

    artifactPackagingMapper.setPropertiesFile(new File(nexusConfiguration.getConfigurationDirectory(),
        MAPPING_PROPERTIES_FILE));

    // load locally present plugins
    getLogger().info("Activating locally installed plugins...");

    Collection<PluginManagerResponse> activationResponse = nexusPluginManager.activateInstalledPlugins();

    for (PluginManagerResponse response : activationResponse) {
      if (response.isSuccessful()) {
        getLogger().info(response.formatAsString(getLogger().isDebugEnabled()));
      }
      else {
        getLogger().warn(response.formatAsString(getLogger().isDebugEnabled()));
      }
    }

    applicationStatusSource.setState(SystemState.STOPPED);

    applicationStatusSource.getSystemStatus().setOperationMode(OperationMode.STANDALONE);

    applicationStatusSource.getSystemStatus().setInitializedAt(new Date());

    eventBus.post(new NexusInitializedEvent(this));
  }

  /**
   * Logs a message indicating initialization of this component
   */
  @VisibleForTesting
  protected void logInitialize() {
    final StringBuilder sysInfoLog = new StringBuilder();
    sysInfoLog.append("\n-------------------------------------------------\n\n");
    sysInfoLog.append("Initializing ").append(getNexusNameForLogs());
    sysInfoLog.append("\n\n-------------------------------------------------");
    getLogger().info(sysInfoLog.toString());
  }

  /**
   * @return the full name of this Nexus to use in logs including App name, version and long edition if defined
   */
  @VisibleForTesting
  protected final String getNexusNameForLogs() {
    final StringBuilder msg = new StringBuilder();
    msg.append(getSystemStatus().getAppName());
    msg.append(" ").append(getSystemStatus().getVersion());
    return msg.toString();
  }

  public void start()
      throws StartingException
  {
    try {
      startService();
    }
    catch (Exception e) {
      throw new StartingException("Could not start Nexus!", e);
    }
  }

  public void stop()
      throws StoppingException
  {
    try {
      stopService();
    }
    catch (Exception e) {
      throw new StoppingException("Could not stop Nexus!", e);
    }
  }

  protected void startService()
      throws Exception
  {
    applicationStatusSource.getSystemStatus().setState(SystemState.STARTING);

    try {
      // force config load and validation
      // applies configuration and notifies listeners
      nexusConfiguration.loadConfiguration(true);

      // essential service
      securitySystem.start();
      // "ping" it to load configuration (as it is lazy)
      securitySystem.getAnonymousUsername();

      // create internals
      nexusConfiguration.createInternals();

      // init tasks
      nexusScheduler.initializeTasks();

      // notify about start
      eventBus.post(new ConfigurationChangeEvent(nexusConfiguration, null, null));

      applicationStatusSource.getSystemStatus().setLastConfigChange(new Date());

      applicationStatusSource.getSystemStatus().setFirstStart(nexusConfiguration.isConfigurationDefaulted());

      applicationStatusSource.getSystemStatus().setInstanceUpgraded(nexusConfiguration.isInstanceUpgraded());

      applicationStatusSource.getSystemStatus().setConfigurationUpgraded(nexusConfiguration.isConfigurationUpgraded());

      if (applicationStatusSource.getSystemStatus().isFirstStart()) {
        getLogger().info("This is 1st start of new Nexus instance.");

        // TODO: a virgin instance, inital config created
      }

      if (applicationStatusSource.getSystemStatus().isInstanceUpgraded()) {
        getLogger().info("This is an upgraded instance of Nexus.");

        // TODO: perform upgrade or something
      }

      // sync shadows now, those needed
      synchronizeShadowsAtStartup();

      applicationStatusSource.getSystemStatus().setState(SystemState.STARTED);

      applicationStatusSource.getSystemStatus().setStartedAt(new Date());

      if (getLogger().isInfoEnabled()) {
        final File workDir = nexusConfiguration.getWorkingDirectory();
        String workDirPath = null;
        if (workDir != null) {
          try {
            workDirPath = workDir.getCanonicalPath();
          }
          catch (IOException ioe) {
            workDirPath = workDir.getAbsolutePath();
          }
        }
        getLogger().info("Nexus Work Directory : {}", workDirPath);
        getLogger().info("Started {}", getNexusNameForLogs());
      }

      eventBus.post(new NexusStartedEvent(this));
    }
    catch (IOException e) {
      applicationStatusSource.getSystemStatus().setState(SystemState.BROKEN_IO);

      applicationStatusSource.getSystemStatus().setErrorCause(e);

      getLogger().error("Could not start Nexus, bad IO exception!", e);

      throw new StartingException("Could not start Nexus!", e);
    }
    catch (ConfigurationException e) {
      applicationStatusSource.getSystemStatus().setState(SystemState.BROKEN_CONFIGURATION);

      applicationStatusSource.getSystemStatus().setErrorCause(e);

      getLogger().error("Could not start Nexus, user configuration exception!", e);

      throw new StartingException("Could not start Nexus!", e);
    }
  }

  protected void stopService()
      throws Exception
  {
    applicationStatusSource.getSystemStatus().setState(SystemState.STOPPING);

    // Due to no dependency mechanism in NX for components, we need to fire off a hint about shutdown first
    eventBus.post(new NexusStoppingEvent(this));

    nexusScheduler.shutdown();

    eventBus.post(new NexusStoppedEvent(this));

    nexusConfiguration.dropInternals();

    securitySystem.stop();

    applicationStatusSource.getSystemStatus().setState(SystemState.STOPPED);

    getLogger().info("Stopped {}", getNexusNameForLogs());
  }

  private void synchronizeShadowsAtStartup() {
    Collection<ShadowRepository> shadows = repositoryRegistry.getRepositoriesWithFacet(ShadowRepository.class);

    for (ShadowRepository shadow : shadows) {
      // spawn tasks to do it
      if (shadow.isSynchronizeAtStartup()) {
        SynchronizeShadowsTask task = nexusScheduler.createTaskInstance(SynchronizeShadowsTask.class);

        task.setShadowRepositoryId(shadow.getId());

        nexusScheduler.submit("Shadow Sync (" + shadow.getId() + ")", task);
      }
    }
  }

  // ----------------------------------------------------------------------------
  // Repo templates
  // ----------------------------------------------------------------------------

  public TemplateSet getRepositoryTemplates() {
    return templateManager.getTemplates().getTemplates(RepositoryTemplate.class);
  }

  public RepositoryTemplate getRepositoryTemplateById(String id)
      throws NoSuchTemplateIdException
  {
    return (RepositoryTemplate) templateManager.getTemplate(RepositoryTemplate.class, id);
  }

  @Override
  @VisibleForTesting
  protected Logger getLogger() {
    return super.getLogger();
  }
}
