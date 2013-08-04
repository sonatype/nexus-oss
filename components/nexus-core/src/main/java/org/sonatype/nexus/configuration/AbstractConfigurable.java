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

package org.sonatype.nexus.configuration;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.Subscribe;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * Abstract class to implement configurable components to "click" them in into generic configuration environment.
 * <p/>
 * NOTE: Don't convert AbstractConfigurable plexus components to sisu, they will be initialized BEFORE configuration is
 * loaded.
 *
 * @author cstamas
 */
public abstract class AbstractConfigurable
    implements Configurable, Initializable, Disposable
{

  /**
   * The configuration
   */
  private CoreConfiguration coreConfiguration;

  @Requirement
  private EventBus eventBus;

  /**
   * True as long as this is registered with event bus.
   */
  private boolean registeredWithEventBus;

  public AbstractConfigurable() {

  }

  public AbstractConfigurable(final EventBus eventBus) {
    this.eventBus = eventBus;
  }

  protected boolean isConfigured() {
    return coreConfiguration != null;
  }

  public void initialize()
      throws InitializationException
  {
    registerWithEventBus();

    try {
      initializeConfiguration();
    }
    catch (ConfigurationException e) {
      throw new InitializationException("Cannot configure the component!", e);
    }
  }

  protected void initializeConfiguration()
      throws ConfigurationException
  {
    // someone needs this, someone not
    // for example, whoever is configured using framework, will not need this,
    // but we still have components on their own, like DefaultTaskConfigManager
    // that are driven by spice Scheduler
  }

  public void dispose() {
    unregisterFromEventBus();
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
  public final void onEvent(final ConfigurationValidateEvent evt) {
    try {
      // validate
      getCurrentCoreConfiguration().validateChanges();
    }
    catch (ConfigurationException e) {
      // put a veto
      evt.putVeto(this, e);
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

  protected abstract ApplicationConfiguration getApplicationConfiguration();

  // Configurable iface

  public final CoreConfiguration getCurrentCoreConfiguration() {
    return coreConfiguration;
  }

  public final void configure(Object config)
      throws ConfigurationException
  {
    this.coreConfiguration = wrapConfiguration(config);

    // "pull" the config to make it dirty
    getCurrentConfiguration(true);

    // do commit
    doConfigure();
  }

  public boolean isDirty() {
    final CoreConfiguration cc = getCurrentCoreConfiguration();
    return cc != null && cc.isDirty();
  }

  protected void prepareForSave()
      throws ConfigurationException
  {
    if (isDirty()) {
      getCurrentCoreConfiguration().validateChanges();

      if (getConfigurator() != null) {
        // prepare for save: transfer what we have in memory (if any) to model
        getConfigurator().prepareForSave(this, getApplicationConfiguration(), getCurrentCoreConfiguration());
      }
    }
  }

  public boolean commitChanges()
      throws ConfigurationException
  {
    if (isDirty()) {
      doConfigure();

      return true;
    }
    else {
      return false;
    }
  }

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

  protected void doConfigure()
      throws ConfigurationException
  {
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

  protected abstract Configurator getConfigurator();

  protected abstract Object getCurrentConfiguration(boolean forWrite);

  protected abstract CoreConfiguration wrapConfiguration(Object configuration)
      throws ConfigurationException;

}
