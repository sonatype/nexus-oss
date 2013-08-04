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

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.events.GlobalRemoteConnectionSettingsChangedEvent;
import org.sonatype.nexus.configuration.model.CGlobalRemoteConnectionSettingsCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.DefaultRemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;

import org.codehaus.plexus.component.annotations.Component;

@Component(role = GlobalRemoteConnectionSettings.class)
public class DefaultGlobalRemoteConnectionSettings
    extends AbstractConfigurable
    implements GlobalRemoteConnectionSettings
{
  @Override
  protected ApplicationConfiguration getApplicationConfiguration() {
    return null;
  }

  @Override
  protected Configurator getConfigurator() {
    return null;
  }

  @Override
  protected CRemoteConnectionSettings getCurrentConfiguration(boolean forWrite) {
    return ((CGlobalRemoteConnectionSettingsCoreConfiguration) getCurrentCoreConfiguration())
        .getConfiguration(forWrite);
  }

  @Override
  protected CoreConfiguration wrapConfiguration(Object configuration)
      throws ConfigurationException
  {
    if (configuration instanceof ApplicationConfiguration) {
      return new CGlobalRemoteConnectionSettingsCoreConfiguration((ApplicationConfiguration) configuration);
    }
    else {
      throw new ConfigurationException("The passed configuration object is of class \""
          + configuration.getClass().getName() + "\" and not the required \""
          + ApplicationConfiguration.class.getName() + "\"!");
    }
  }

  // ==

  public int getConnectionTimeout() {
    return getCurrentConfiguration(false).getConnectionTimeout();
  }

  public void setConnectionTimeout(int connectionTimeout) {
    getCurrentConfiguration(true).setConnectionTimeout(connectionTimeout);
  }

  public String getQueryString() {
    return getCurrentConfiguration(false).getQueryString();
  }

  public void setQueryString(String queryString) {
    getCurrentConfiguration(true).setQueryString(queryString);
  }

  public int getRetrievalRetryCount() {
    return getCurrentConfiguration(false).getRetrievalRetryCount();
  }

  public void setRetrievalRetryCount(int retrievalRetryCount) {
    getCurrentConfiguration(true).setRetrievalRetryCount(retrievalRetryCount);
  }

  public String getUserAgentCustomizationString() {
    return getCurrentConfiguration(false).getUserAgentCustomizationString();
  }

  public void setUserAgentCustomizationString(String userAgentCustomizationString) {
    getCurrentConfiguration(true).setUserAgentCustomizationString(userAgentCustomizationString);
  }

  // ==

  public RemoteConnectionSettings convertAndValidateFromModel(CRemoteConnectionSettings model)
      throws ConfigurationException
  {
    ((CGlobalRemoteConnectionSettingsCoreConfiguration) getCurrentCoreConfiguration()).doValidateChanges(model);

    if (model != null) {
      RemoteConnectionSettings remoteConnectionSettings = new DefaultRemoteConnectionSettings();

      remoteConnectionSettings.setConnectionTimeout(model.getConnectionTimeout());

      remoteConnectionSettings.setQueryString(model.getQueryString());

      remoteConnectionSettings.setRetrievalRetryCount(model.getRetrievalRetryCount());

      remoteConnectionSettings.setUserAgentCustomizationString(model.getUserAgentCustomizationString());

      return remoteConnectionSettings;
    }
    else {
      return null;
    }
  }

  public CRemoteConnectionSettings convertToModel(RemoteConnectionSettings settings) {
    if (settings == null) {
      return null;
    }
    else {
      CRemoteConnectionSettings model = new CRemoteConnectionSettings();

      model.setConnectionTimeout(settings.getConnectionTimeout());

      model.setQueryString(settings.getQueryString());

      model.setRetrievalRetryCount(settings.getRetrievalRetryCount());

      model.setUserAgentCustomizationString(settings.getUserAgentCustomizationString());

      return model;
    }
  }

  public String getName() {
    return "Global Remote Connection Settings";
  }

  @Override
  public boolean commitChanges()
      throws ConfigurationException
  {
    boolean wasDirty = super.commitChanges();

    if (wasDirty) {
      eventBus().post(new GlobalRemoteConnectionSettingsChangedEvent(this));
    }

    return wasDirty;
  }

}
