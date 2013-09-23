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
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.cache.CacheManager;
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
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.usermanagement.NoSuchUserManagerException;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserStatus;
import org.sonatype.security.usermanagement.xml.SecurityXmlUserManager;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * The class DefaultNexusConfiguration is responsible for config management. It actually keeps in sync Nexus internal
 * state with p ersisted user configuration. All changes incoming thru its iface is reflect/maintained in Nexus current
 * state and Nexus user config.
 *
 * @author cstamas
 */
@Component(role = NexusConfiguration.class)
public class DefaultNexusConfiguration
    extends AbstractLoggingComponent
    implements NexusConfiguration, Initializable
{
  @Requirement
  private EventBus eventBus;

  /**
   * The path cache is referenced here only for UTs sake. At deploy/runtime, this does not matter, as CacheManager is
   * already part of component dependency graph. This reference here is merely for UTs, as almost all of them (from
   * nexus-app module onwards) does "awake" this component, and hence, by having this reference here, we actually
   * pull
   * and have Plexus manage the "lifecycle" of CacheManager component (and indirectly, EhCacheManager lifecycle).
   */
  @Requirement
  @SuppressWarnings("unused")
  private CacheManager pathCache;

  @Requirement(hint = "file")
  private ApplicationConfigurationSource configurationSource;

  /**
   * The global local storage context.
   */
  private LocalStorageContext globalLocalStorageContext;

  /**
   * The global remote storage context.
   */
  private RemoteStorageContext globalRemoteStorageContext;

  @Requirement
  private GlobalRemoteConnectionSettings globalRemoteConnectionSettings;

  @Requirement
  private GlobalRemoteProxySettings globalRemoteProxySettings;

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

  @Requirement(role = ScheduledTaskDescriptor.class)
  private List<ScheduledTaskDescriptor> scheduledTaskDescriptors;

  @Requirement
  private SecuritySystem securitySystem;

  @org.codehaus.plexus.component.annotations.Configuration(value = "${nexus-work}")
  private File workingDirectory;

  @Requirement
  private VetoFormatter vetoFormatter;

  /**
   * The config dir
   */
  private File configurationDirectory;

  /**
   * The temp dir
   */
  private File temporaryDirectory;

  /**
   * Names of the conf files
   */
  private Map<String, String> configurationFiles;

  /**
   * The default maxInstance count
   */
  private int defaultRepositoryMaxInstanceCountLimit = Integer.MAX_VALUE;

  /**
   * The map with per-repotype limitations
   */
  private Map<RepositoryTypeDescriptor, Integer> repositoryMaxInstanceCountLimits;

  @Requirement
  private List<ConfigurationModifier> configurationModifiers;

  // ==

  private File canonicalize(final File file) {
    try {
      return file.getCanonicalFile();
    }
    catch (IOException e) {
      final String message =
          "\r\n******************************************************************************\r\n"
              + "* Could not canonicalize file [ "
              + file
              + "]!!!! *\r\n"
              + "* Nexus cannot function properly until the process has read+write permissions to this folder *\r\n"
              + "******************************************************************************";
      getLogger().error(message);
      throw Throwables.propagate(e);
    }
  }

  private File forceMkdir(final File directory) {
    try {
      FileUtils.forceMkdir(directory);
      return directory;
    }
    catch (IOException e) {
      final String message =
          "\r\n******************************************************************************\r\n"
              + "* Could not create directory [ "
              + directory
              + "]!!!! *\r\n"
              + "* Nexus cannot function properly until the process has read+write permissions to this folder *\r\n"
              + "******************************************************************************";
      getLogger().error(message);
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void initialize()
      throws InitializationException
  {
    workingDirectory = canonicalize(workingDirectory);
    if (!workingDirectory.isDirectory()) {
      forceMkdir(workingDirectory);
    }

    temporaryDirectory = canonicalize(new File(System.getProperty("java.io.tmpdir")));
    if (!temporaryDirectory.isDirectory()) {
      forceMkdir(temporaryDirectory);
    }

    configurationDirectory = canonicalize(new File(getWorkingDirectory(), "conf"));
    if (!configurationDirectory.isDirectory()) {
      forceMkdir(configurationDirectory);
    }
  }

  @Override
  public void loadConfiguration()
      throws ConfigurationException, IOException
  {
    loadConfiguration(false);
  }

  @Override
  public synchronized void loadConfiguration(boolean force)
      throws ConfigurationException, IOException
  {
    if (force || configurationSource.getConfiguration() == null) {
      getLogger().info("Loading Nexus Configuration...");

      configurationSource.loadConfiguration();

      boolean modified = false;
      for (ConfigurationModifier modifier : configurationModifiers) {
        modified |= modifier.apply(configurationSource.getConfiguration());
      }

      if (modified) {
        configurationSource.backupConfiguration();
        configurationSource.storeConfiguration();
      }

      globalLocalStorageContext = new DefaultLocalStorageContext(null);

      // create global remote ctx
      // this one has no parent
      globalRemoteStorageContext = new DefaultRemoteStorageContext(null);

      globalRemoteConnectionSettings.configure(this);

      globalRemoteStorageContext.setRemoteConnectionSettings(globalRemoteConnectionSettings);

      globalRemoteProxySettings.configure(this);

      globalRemoteStorageContext.setRemoteProxySettings(globalRemoteProxySettings);

      ConfigurationPrepareForLoadEvent loadEvent = new ConfigurationPrepareForLoadEvent(this);

      eventBus.post(loadEvent);

      if (loadEvent.isVetoed()) {
        getLogger().info(
            vetoFormatter.format(new VetoFormatterRequest(loadEvent, getLogger().isDebugEnabled())));

        throw new ConfigurationException("The Nexus configuration is invalid!");
      }

      applyConfiguration();

      // we successfully loaded config
      eventBus.post(new ConfigurationLoadEvent(this));
    }
  }

  protected String changesToString(final Collection<Configurable> changes) {
    final StringBuilder sb = new StringBuilder();

    if (changes != null) {
      sb.append(Collections2.transform(changes, new Function<Configurable, String>()
      {
        @Override
        public String apply(final Configurable input) {
          return input.getName();
        }
      }));
    }

    return sb.toString();
  }

  protected void logApplyConfiguration(final Collection<Configurable> changes) {
    final String userId = getCurrentUserId();

    if (changes != null && changes.size() > 0) {
      if (StringUtils.isBlank(userId)) {
        // should not really happen, we should always have subject (at least anon), but...
        getLogger().info("Applying Nexus Configuration due to changes in {}...", changesToString(changes));
      }
      else {
        // usually what happens on config change
        getLogger().info("Applying Nexus Configuration due to changes in {} made by {}...",
            changesToString(changes), userId);
      }
    }
    else {
      if (StringUtils.isBlank(userId)) {
        // usually on boot: no changes since "all" changed, and no subject either
        getLogger().info("Applying Nexus Configuration...");
      }
      else {
        // inperfection of config framework, ie. on adding new component to config system (new repo)
        getLogger().info("Applying Nexus Configuration made by {}...", userId);
      }
    }
  }

  /**
   * Returns the userId ("main principal" in Shiro lingo) of the user that is the principal of currently executing
   * activity (like configuration save) for logging purposes only. It uses Shiro API to get the information, and will
   * return the String userId, or {@code null} if it's impossible to determine it, as current thread (the one
   * invoking
   * this method) does not have bound Subject. If more information needed about current user, Shiro and/or Security
   * API of Nexus should be used, this method is not a definitive source of users in Nexus Security.
   */
  protected String getCurrentUserId() {
    try {
      final Subject subject = SecurityUtils.getSubject();
      if (subject != null && subject.getPrincipal() != null) {
        return subject.getPrincipal().toString();
      }
    }
    catch (final Exception e) {
      // NEXUS-5749: Prevent interruption of configuration save (and hence, data loss) for any
      // exception thrown while gathering userId for logging purposes.
      getLogger().warn("Could not obtain Shiro subject:", e);
    }
    return null;
  }

  public synchronized boolean applyConfiguration() {
    getLogger().debug("Applying Nexus Configuration...");

    ConfigurationPrepareForSaveEvent prepare = new ConfigurationPrepareForSaveEvent(this);

    eventBus.post(prepare);

    if (!prepare.isVetoed()) {
      logApplyConfiguration(prepare.getChanges());

      eventBus.post(new ConfigurationCommitEvent(this));

      eventBus.post(new ConfigurationChangeEvent(this, prepare.getChanges(), getCurrentUserId()));

      return true;
    }
    else {
      getLogger().info(vetoFormatter.format(new VetoFormatterRequest(prepare, getLogger().isDebugEnabled())));

      eventBus.post(new ConfigurationRollbackEvent(this));

      return false;
    }
  }

  @Override
  public synchronized void saveConfiguration()
      throws IOException
  {
    if (applyConfiguration()) {
      // TODO: when NEXUS-2215 is fixed, this should be remove/moved/cleaned
      // START <<<
      // validate before we do anything
      ValidationRequest request = new ValidationRequest(configurationSource.getConfiguration());
      ValidationResponse response = configurationValidator.validateModel(request);
      if (!response.isValid()) {
        this.getLogger().error("Saving nexus configuration caused unexpected error:\n" + response.toString());
        throw new IOException("Saving nexus configuration caused unexpected error:\n" + response.toString());
      }
      // END <<<

      configurationSource.storeConfiguration();

      // we successfully saved config
      eventBus.post(new ConfigurationSaveEvent(this));
    }
  }

  @Deprecated
  // see above
  protected void applyAndSaveConfiguration()
      throws IOException
  {
    saveConfiguration();
  }

  @Override
  @Deprecated
  public Configuration getConfigurationModel() {
    return configurationSource.getConfiguration();
  }

  @Override
  public ApplicationConfigurationSource getConfigurationSource() {
    return configurationSource;
  }

  @Override
  public boolean isInstanceUpgraded() {
    return configurationSource.isInstanceUpgraded();
  }

  @Override
  public boolean isConfigurationUpgraded() {
    return configurationSource.isConfigurationUpgraded();
  }

  @Override
  public boolean isConfigurationDefaulted() {
    return configurationSource.isConfigurationDefaulted();
  }

  @Override
  public LocalStorageContext getGlobalLocalStorageContext() {
    return globalLocalStorageContext;
  }

  @Override
  public RemoteStorageContext getGlobalRemoteStorageContext() {
    return globalRemoteStorageContext;
  }

  @Override
  public File getWorkingDirectory() {
    return workingDirectory;
  }

  @Override
  public File getWorkingDirectory(String key) {
    return getWorkingDirectory(key, true);
  }

  @Override
  public File getWorkingDirectory(final String key, final boolean createIfNeeded) {
    final File keyedDirectory = new File(getWorkingDirectory(), key);
    if (createIfNeeded) {
      forceMkdir(keyedDirectory);
    }
    return canonicalize(keyedDirectory);
  }

  @Override
  public File getTemporaryDirectory() {
    return temporaryDirectory;
  }

  @Override
  public File getConfigurationDirectory() {
    return configurationDirectory;
  }

  @Override
  @Deprecated
  public Repository createRepositoryFromModel(CRepository repository)
      throws ConfigurationException
  {
    return runtimeConfigurationBuilder.createRepositoryFromModel(getConfigurationModel(), repository);
  }

  @Override
  public List<ScheduledTaskDescriptor> listScheduledTaskDescriptors() {
    return Collections.unmodifiableList(scheduledTaskDescriptors);
  }

  @Override
  public ScheduledTaskDescriptor getScheduledTaskDescriptor(String id) {
    for (ScheduledTaskDescriptor descriptor : scheduledTaskDescriptors) {
      if (descriptor.getId().equals(id)) {
        return descriptor;
      }
    }

    return null;
  }

  // ------------------------------------------------------------------
  // Security

  @Override
  public boolean isSecurityEnabled() {
    return getSecuritySystem() != null && getSecuritySystem().isSecurityEnabled();
  }

  @Override
  public void setSecurityEnabled(boolean enabled)
      throws IOException
  {
    getSecuritySystem().setSecurityEnabled(enabled);
  }

  @Override
  public void setRealms(List<String> realms)
      throws org.sonatype.configuration.validation.InvalidConfigurationException
  {
    getSecuritySystem().setRealms(realms);
  }

  @Override
  public boolean isAnonymousAccessEnabled() {
    return getSecuritySystem() != null && getSecuritySystem().isAnonymousAccessEnabled();
  }

  @Override
  public void setAnonymousAccess(final boolean enabled, final String username, final String password)
      throws InvalidConfigurationException
  {
    if (enabled) {
      if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
        throw new InvalidConfigurationException(
            "Anonymous access is getting enabled without valid username and/or password!");
      }

      final String oldUsername = getSecuritySystem().getAnonymousUsername();
      final String oldPassword = getSecuritySystem().getAnonymousPassword();

      // try to enable the "anonymous" user defined in XML realm, but ignore any problem (users might
      // delete
      // or already disabled it, or completely removed XML realm)
      // this is needed as below we will try a login
      final boolean statusChanged = setAnonymousUserEnabled(username, true);

      // detect change
      if (!StringUtils.equals(oldUsername, username) || !StringUtils.equals(oldPassword, password)) {
        try {
          // test authc with changed credentials
          try {
            // try to "log in" with supplied credentials
            // the anon user a) should exists
            securitySystem.getUser(username);
            // b) the pwd must work
            securitySystem.authenticate(new UsernamePasswordToken(username, password));
          }
          catch (UserNotFoundException e) {
            final String msg = "User \"" + username + "'\" does not exist.";
            getLogger().warn(
                "Nexus refused to apply configuration, the supplied anonymous information is wrong: " + msg,
                e);
            throw new InvalidConfigurationException(msg, e);
          }
          catch (AuthenticationException e) {
            final String msg = "The password of user \"" + username + "\" is incorrect.";
            getLogger().warn(
                "Nexus refused to apply configuration, the supplied anonymous information is wrong: " + msg,
                e);
            throw new InvalidConfigurationException(msg, e);
          }
        }
        catch (InvalidConfigurationException e) {
          if (statusChanged) {
            setAnonymousUserEnabled(username, false);
          }
          throw e;
        }

        // set the changed username/pw
        getSecuritySystem().setAnonymousUsername(username);
        getSecuritySystem().setAnonymousPassword(password);
      }

      getSecuritySystem().setAnonymousAccessEnabled(true);
    }
    else {
      // get existing username from XML realm, if we can (if security config about to be disabled still holds this
      // info)
      final String existingUsername = getSecuritySystem().getAnonymousUsername();

      if (!StringUtils.isBlank(existingUsername)) {
        // try to disable the "anonymous" user defined in XML realm, but ignore any problem (users might delete
        // or already disabled it, or completely removed XML realm)
        setAnonymousUserEnabled(existingUsername, false);
      }

      getSecuritySystem().setAnonymousAccessEnabled(false);
    }

  }

  protected boolean setAnonymousUserEnabled(final String anonymousUsername, final boolean enabled)
      throws InvalidConfigurationException
  {
    try {
      final User anonymousUser = getSecuritySystem().getUser(anonymousUsername, SecurityXmlUserManager.SOURCE);
      final UserStatus oldStatus = anonymousUser.getStatus();
      if (enabled) {
        anonymousUser.setStatus(UserStatus.active);
      }
      else {
        anonymousUser.setStatus(UserStatus.disabled);
      }
      getSecuritySystem().updateUser(anonymousUser);
      return !oldStatus.equals(anonymousUser.getStatus());
    }
    catch (UserNotFoundException e) {
      // ignore, anon user maybe manually deleted from XML realm by Nexus admin, is okay (kinda expected)
      getLogger().debug(
          "Anonymous user not found while trying to disable it (as part of disabling anonymous access)!", e);
      return false;
    }
    catch (NoSuchUserManagerException e) {
      // ignore, XML realm removed from configuration by Nexus admin, is okay (kinda expected)
      getLogger().debug(
          "XML Realm not found while trying to disable Anonymous user (as part of disabling anonymous access)!",
          e);
      return false;
    }
    catch (InvalidConfigurationException e) {
      // do not ignore, and report, as this jeopardizes whole security functionality
      // we did not perform any _change_ against security sofar (we just did reading from it),
      // so it is okay to bail out at this point
      getLogger().warn(
          "XML Realm reported invalid configuration while trying to disable Anonymous user (as part of disabling anonymous access)!",
          e);
      throw e;
    }
  }

  @Override
  @Deprecated
  public void setAnonymousAccessEnabled(boolean enabled) {
    getSecuritySystem().setAnonymousAccessEnabled(enabled);
  }

  @Override
  public String getAnonymousUsername() {
    return getSecuritySystem().getAnonymousUsername();
  }

  @Override
  @Deprecated
  public void setAnonymousUsername(String val)
      throws org.sonatype.configuration.validation.InvalidConfigurationException
  {
    getSecuritySystem().setAnonymousUsername(val);
  }

  @Override
  public String getAnonymousPassword() {
    return getSecuritySystem().getAnonymousPassword();
  }

  @Override
  @Deprecated
  public void setAnonymousPassword(String val)
      throws org.sonatype.configuration.validation.InvalidConfigurationException
  {
    getSecuritySystem().setAnonymousPassword(val);
  }

  @Override
  public List<String> getRealms() {
    return getSecuritySystem().getRealms();
  }

  // ------------------------------------------------------------------
  // Booting

  @Override
  public void createInternals()
      throws ConfigurationException
  {
    createRepositories();
  }

  @Override
  public void dropInternals() {
    dropRepositories();
  }

  protected void createRepositories()
      throws ConfigurationException
  {
    List<CRepository> reposes = getConfigurationModel().getRepositories();

    for (CRepository repo : reposes) {

      if (!repo.getProviderRole().equals(GroupRepository.class.getName())) {
        instantiateRepository(getConfigurationModel(), repo);
      }
    }

    for (CRepository repo : reposes) {
      if (repo.getProviderRole().equals(GroupRepository.class.getName())) {
        instantiateRepository(getConfigurationModel(), repo);
      }
    }
  }

  protected void dropRepositories() {
    for (Repository repository : repositoryRegistry.getRepositories()) {
      try {
        repositoryRegistry.removeRepositorySilently(repository.getId());
      }
      catch (NoSuchRepositoryException e) {
        // will not happen
      }
    }
  }

  protected Repository instantiateRepository(final Configuration configuration, final CRepository repositoryModel)
      throws ConfigurationException
  {
    checkRepositoryMaxInstanceCountForCreation(repositoryModel);

    // create it, will do runtime validation
    Repository repository = runtimeConfigurationBuilder.createRepositoryFromModel(configuration, repositoryModel);

    // register with repoRegistry
    repositoryRegistry.addRepository(repository);

    // give it back
    return repository;
  }

  protected void releaseRepository(final Repository repository, final Configuration configuration,
                                   final CRepository repositoryModel)
      throws ConfigurationException
  {
    // release it
    runtimeConfigurationBuilder.releaseRepository(repository, configuration, repositoryModel);
  }

  // ------------------------------------------------------------------
  // CRUD-like ops on config sections
  // Globals are mandatory: RU

  // CRepository and CreposioryShadow helper

  private ApplicationValidationContext getRepositoryValidationContext() {
    ApplicationValidationContext result = new ApplicationValidationContext();

    fillValidationContextRepositoryIds(result);

    return result;
  }

  private void fillValidationContextRepositoryIds(ApplicationValidationContext context) {
    context.addExistingRepositoryIds();

    List<CRepository> repositories = getConfigurationModel().getRepositories();

    if (repositories != null) {
      for (CRepository repo : repositories) {
        context.getExistingRepositoryIds().add(repo.getId());
      }
    }
  }

  // ----------------------------------------------------------------------------------------------------------
  // Repositories
  // ----------------------------------------------------------------------------------------------------------

  protected Map<RepositoryTypeDescriptor, Integer> getRepositoryMaxInstanceCountLimits() {
    if (repositoryMaxInstanceCountLimits == null) {
      repositoryMaxInstanceCountLimits = new ConcurrentHashMap<RepositoryTypeDescriptor, Integer>();
    }

    return repositoryMaxInstanceCountLimits;
  }

  @Override
  public void setDefaultRepositoryMaxInstanceCount(int count) {
    if (count < 0) {
      getLogger().info("Default repository maximal instance limit set to UNLIMITED.");

      this.defaultRepositoryMaxInstanceCountLimit = Integer.MAX_VALUE;
    }
    else {
      getLogger().info("Default repository maximal instance limit set to " + count + ".");

      this.defaultRepositoryMaxInstanceCountLimit = count;
    }
  }

  @Override
  public void setRepositoryMaxInstanceCount(RepositoryTypeDescriptor rtd, int count) {
    if (count < 0) {
      getLogger().info("Repository type " + rtd.toString() + " maximal instance limit set to UNLIMITED.");

      getRepositoryMaxInstanceCountLimits().remove(rtd);
    }
    else {
      getLogger().info("Repository type " + rtd.toString() + " maximal instance limit set to " + count + ".");

      getRepositoryMaxInstanceCountLimits().put(rtd, count);
    }
  }

  @Override
  public int getRepositoryMaxInstanceCount(RepositoryTypeDescriptor rtd) {
    Integer limit = getRepositoryMaxInstanceCountLimits().get(rtd);

    if (null != limit) {
      return limit;
    }
    else {
      return defaultRepositoryMaxInstanceCountLimit;
    }
  }

  protected void checkRepositoryMaxInstanceCountForCreation(CRepository repositoryModel)
      throws ConfigurationException
  {
    RepositoryTypeDescriptor rtd =
        repositoryTypeRegistry.getRepositoryTypeDescriptor(repositoryModel.getProviderRole(),
            repositoryModel.getProviderHint());

    int maxCount;

    if (null == rtd) {
      // no check done
      String msg =
          String.format(
              "Repository \"%s\" (repoId=%s) corresponding type is not registered in Core, hence it's maxInstace check cannot be performed: Repository type %s:%s is unknown to Nexus Core. It is probably contributed by an old Nexus plugin. Please contact plugin developers to upgrade the plugin, and register the new repository type(s) properly!",
              repositoryModel.getName(), repositoryModel.getId(), repositoryModel.getProviderRole(),
              repositoryModel.getProviderHint());

      getLogger().warn(msg);

      return;
    }

    if (rtd.getRepositoryMaxInstanceCount() != RepositoryType.UNLIMITED_INSTANCES) {
      maxCount = rtd.getRepositoryMaxInstanceCount();
    }
    else {
      maxCount = getRepositoryMaxInstanceCount(rtd);
    }

    if (rtd.getInstanceCount() >= maxCount) {
      String msg =
          "Repository \"" + repositoryModel.getName() + "\" (id=" + repositoryModel.getId()
              + ") cannot be created. It's repository type " + rtd.toString() + " is limited to " + maxCount
              + " instances, and it already has " + String.valueOf(rtd.getInstanceCount()) + " of them.";

      getLogger().warn(msg);

      throw new ConfigurationException(msg);
    }
  }

  // CRepository: CRUD

  protected void validateRepository(CRepository settings, boolean create)
      throws ConfigurationException
  {
    ApplicationValidationContext ctx = getRepositoryValidationContext();

    if (!create && !StringUtils.isEmpty(settings.getId())) {
      // remove "itself" from the list to avoid hitting "duplicate repo" problem
      ctx.getExistingRepositoryIds().remove(settings.getId());
    }

    ValidationResponse vr = configurationValidator.validateRepository(ctx, settings);

    if (!vr.isValid()) {
      throw new InvalidConfigurationException(vr);
    }
  }

  @Override
  public synchronized Repository createRepository(CRepository settings)
      throws ConfigurationException, IOException
  {
    validateRepository(settings, true);

    // create it, will do runtime validation
    Repository repository = instantiateRepository(getConfigurationModel(), settings);

    // now add it to config, since it is validated and successfully created
    getConfigurationModel().addRepository(settings);

    // save
    saveConfiguration();

    return repository;
  }

  @Override
  public void deleteRepository(String id)
      throws NoSuchRepositoryException, IOException, ConfigurationException, AccessDeniedException
  {
    deleteRepository(id, false);
  }

  @Override
  public synchronized void deleteRepository(String id, boolean force)
      throws NoSuchRepositoryException, IOException, ConfigurationException, AccessDeniedException
  {
    Repository repository = repositoryRegistry.getRepository(id);

    if (!force && !repository.isUserManaged()) {
      throw new AccessDeniedException("Not allowed to delete non-user-managed repository '" + id + "'.");
    }

    // put out of service so wont be accessed any longer
    repository.setLocalStatus(LocalStatus.OUT_OF_SERVICE);
    // disable indexing for same purpose
    repository.setIndexable(false);
    repository.setSearchable(false);

    // remove dependants too

    // =======
    // shadows
    // (fail if any repo references the currently processing one)
    List<ShadowRepository> shadows = repositoryRegistry.getRepositoriesWithFacet(ShadowRepository.class);

    for (Iterator<ShadowRepository> i = shadows.iterator(); i.hasNext(); ) {
      ShadowRepository shadow = i.next();

      if (repository.getId().equals(shadow.getMasterRepository().getId())) {
        throw new RepositoryDependentException(repository, shadow);
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

    for (Iterator<CPathMappingItem> i = pathMappings.iterator(); i.hasNext(); ) {
      CPathMappingItem item = i.next();

      item.removeRepository(id);
    }

    // ===========
    // and finally
    // this cleans it properly from the registry (from reposes and repo groups)
    repositoryRegistry.removeRepository(id);

    List<CRepository> reposes = getConfigurationModel().getRepositories();

    for (Iterator<CRepository> i = reposes.iterator(); i.hasNext(); ) {
      CRepository repo = i.next();

      if (repo.getId().equals(id)) {
        i.remove();

        saveConfiguration();

        releaseRepository(repository, getConfigurationModel(), repo);

        return;
      }
    }

    throw new NoSuchRepositoryException(id);
  }

  // ===

  @Override
  public Collection<CRemoteNexusInstance> listRemoteNexusInstances() {
    List<CRemoteNexusInstance> result = null;

    if (getConfigurationModel().getRemoteNexusInstances() != null) {
      result = Collections.unmodifiableList(getConfigurationModel().getRemoteNexusInstances());
    }

    return result;
  }

  @Override
  public CRemoteNexusInstance readRemoteNexusInstance(String alias)
      throws IOException
  {
    List<CRemoteNexusInstance> knownInstances = getConfigurationModel().getRemoteNexusInstances();

    for (Iterator<CRemoteNexusInstance> i = knownInstances.iterator(); i.hasNext(); ) {
      CRemoteNexusInstance nexusInstance = i.next();

      if (nexusInstance.getAlias().equals(alias)) {
        return nexusInstance;
      }
    }

    return null;
  }

  @Override
  public void createRemoteNexusInstance(CRemoteNexusInstance settings)
      throws IOException
  {
    getConfigurationModel().addRemoteNexusInstance(settings);

    applyAndSaveConfiguration();
  }

  @Override
  public void deleteRemoteNexusInstance(String alias)
      throws IOException
  {
    List<CRemoteNexusInstance> knownInstances = getConfigurationModel().getRemoteNexusInstances();

    for (Iterator<CRemoteNexusInstance> i = knownInstances.iterator(); i.hasNext(); ) {
      CRemoteNexusInstance nexusInstance = i.next();

      if (nexusInstance.getAlias().equals(alias)) {
        i.remove();
      }
    }

    applyAndSaveConfiguration();
  }

  @Override
  public Map<String, String> getConfigurationFiles() {
    if (configurationFiles == null) {
      configurationFiles = new HashMap<String, String>();

      File configDirectory = getConfigurationDirectory();

      int key = 1;

      // Tamas:
      // configDirectory.listFiles() may be returning null... in this case, it is 99.9% not true (otherwise nexus
      // would not start at all), but in general, be more explicit about checks.

      if (configDirectory.isDirectory() && configDirectory.listFiles() != null) {
        for (File file : configDirectory.listFiles()) {
          if (file.exists() && file.isFile()) {
            configurationFiles.put(Integer.toString(key), file.getName());

            key++;
          }
        }
      }
    }
    return configurationFiles;
  }

  @Override
  public NexusStreamResponse getConfigurationAsStreamByKey(String key)
      throws IOException
  {
    String fileName = getConfigurationFiles().get(key);

    if (fileName != null) {
      File configFile = new File(getConfigurationDirectory(), fileName);

      if (configFile.canRead() && configFile.isFile()) {
        NexusStreamResponse response = new NexusStreamResponse();

        response.setName(fileName);

        if (fileName.endsWith(".xml")) {
          response.setMimeType("text/xml");
        }
        else {
          response.setMimeType("text/plain");
        }

        response.setSize(configFile.length());
        response.setFromByte(0);
        response.setBytesCount(configFile.length());
        response.setInputStream(new FileInputStream(configFile));

        return response;
      }
      else {
        return null;
      }
    }
    else {
      return null;
    }
  }

  protected SecuritySystem getSecuritySystem() {
    return this.securitySystem;
  }
}
