/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.configuration;

import org.sonatype.nexus.common.throwables.ConfigurationException;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class is a special in configuration framework, extended by other framework classes. You do not want to extend
 * this class (most probably), but rather go for {@link AbstractLastingConfigurable}.
 *
 * @author cstamas
 */
public abstract class AbstractConfigurable<C>
    extends ComponentSupport
    implements Configurable<C>
{

  private EventBus eventBus;

  private ApplicationConfiguration applicationConfiguration;

  /**
   * The configuration
   */
  private CoreConfiguration<C> coreConfiguration;

  /**
   * True as long as this is registered with event bus.
   */
  private boolean registeredWithEventBus;

  protected AbstractConfigurable() {}

  /**
   * Constructor used by {@link AbstractLastingConfigurable}.
   */
  public AbstractConfigurable(final EventBus eventBus, final ApplicationConfiguration applicationConfiguration) {
    setEventBus(eventBus);
    setApplicationConfiguration(applicationConfiguration);

    // init
    registerWithEventBus();
  }

  protected void setEventBus(final EventBus eventBus) {
    this.eventBus = checkNotNull(eventBus);
  }

  protected void setApplicationConfiguration(final ApplicationConfiguration applicationConfiguration) {
    this.applicationConfiguration = checkNotNull(applicationConfiguration);
  }

  protected boolean isConfigured() {
    return coreConfiguration != null && coreConfiguration.getConfiguration(false) != null;
  }

  protected void initializeConfiguration() {
    // someone needs this, someone not
    // for example, whoever is configured using framework, will not need this,
    // but we still have components on their own, like DefaultTaskConfigManager
    // that are driven by spice Scheduler
  }

  @Subscribe
  public final void onEvent(final ConfigurationPrepareForLoadEvent evt) {
    try {
      // validate
      initializeConfiguration();
    }
    catch (ConfigurationException e) {
      // put a veto
      evt.putVeto(this, e);
    }
  }

  @Subscribe
  public final void onEvent(final ConfigurationPrepareForSaveEvent evt) {
    if (isDirty()) {
      try {
        // prepare
        prepareForSave();

        // register ourselves as changed
        evt.getChanges().add(this);
      }
      catch (ConfigurationException e) {
        // put a veto
        evt.putVeto(this, e);
      }
    }
  }

  @Subscribe
  public final void onEvent(final ConfigurationCommitEvent evt) {
    try {
      commitChanges();
    }
    catch (ConfigurationException e) {
      // FIXME: log or something?
      rollbackChanges();
    }
  }

  @Subscribe
  public final void onEvent(final ConfigurationRollbackEvent evt) {
    rollbackChanges();
  }

  // TODO: not final only during refactoring!
  protected final ApplicationConfiguration getApplicationConfiguration() {
    return applicationConfiguration;
  }

  // Configurable iface

  @Override
  public CoreConfiguration<C> getCurrentCoreConfiguration() {
    return coreConfiguration;
  }

  @Override
  public final void configure(Object config) {
    this.coreConfiguration = wrapConfiguration(config);

    // "pull" the config to make it dirty
    getCurrentConfiguration(true);

    // do commit
    doConfigure();
  }

  @Override
  public boolean isDirty() {
    final CoreConfiguration<C> cc = getCurrentCoreConfiguration();
    return cc != null && cc.isDirty();
  }

  protected void prepareForSave() {
    if (isDirty()) {
      getCurrentCoreConfiguration().validateChanges();

      if (getConfigurator() != null) {
        // prepare for save: transfer what we have in memory (if any) to model
        getConfigurator().prepareForSave(this, getApplicationConfiguration(), getCurrentCoreConfiguration());
      }
    }
  }

  @Override
  public boolean commitChanges() {
    if (isDirty()) {
      doConfigure();
      return true;
    }
    else {
      return false;
    }
  }

  @Override
  public boolean rollbackChanges() {
    if (isDirty()) {
      getCurrentCoreConfiguration().rollbackChanges();

      return true;
    }
    else {
      return false;
    }
  }

  // ==

  protected void doConfigure() {
    // 1st, validate
    getCurrentCoreConfiguration().validateChanges();

    // 2nd, we apply configurator (it will map things that are not 1:1 from config object)
    if (getConfigurator() != null) {
      // apply config, transfer what is not mappable (if any) from model
      getConfigurator().applyConfiguration(this, getApplicationConfiguration(), getCurrentCoreConfiguration());

      // prepare for save: transfer what we have in memory (if any) to model
      getConfigurator().prepareForSave(this, getApplicationConfiguration(), getCurrentCoreConfiguration());
    }

    // 3rd, commit
    getCurrentCoreConfiguration().commitChanges();
  }

  protected EventBus eventBus() {
    return eventBus;
  }

  public void registerWithEventBus() {
    if (!registeredWithEventBus) {
      eventBus.register(this);
      registeredWithEventBus = true;
    }
  }

  public void unregisterFromEventBus() {
    if (registeredWithEventBus) {
      eventBus.unregister(this);
      registeredWithEventBus = false;
    }
  }

  protected Configurator getConfigurator() {
    // override to return instance if needed
    return null;
  }

  public C getCurrentConfiguration(boolean forWrite) {
    final CoreConfiguration<C> cc = getCurrentCoreConfiguration();
    return cc != null ? cc.getConfiguration(forWrite) : null;
  }

  protected abstract CoreConfiguration<C> wrapConfiguration(Object configuration);
}
