/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.security.ldap.persist;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.sonatype.security.ldap.persist.validation.LdapConfigurationValidator;
import com.sonatype.security.ldap.realms.EnterpriseLdapAuthenticatingRealm;
import com.sonatype.security.ldap.realms.persist.model.CLdapConfiguration;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.ldap.realms.persist.LdapClearCacheEvent;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class DefaultLdapConfigurationManager
    extends ComponentSupport
    implements LdapConfigurationManager
{
  private final LdapConfigurationSource configurationSource;

  private final LdapConfigurationValidator configurationValidator;

  private final EventBus eventBus;

  private final SecuritySystem securitySystem;

  /**
   * This will hold the current configuration in memory, to reload, will need to set this to null
   */
  private CLdapConfiguration ldapConfiguration = null;

  @Inject
  public DefaultLdapConfigurationManager(final LdapConfigurationSource configurationSource,
                                         final LdapConfigurationValidator configurationValidator,
                                         final EventBus eventBus,
                                         final SecuritySystem securitySystem)
  {
    this.configurationSource = checkNotNull(configurationSource);
    this.configurationValidator = checkNotNull(configurationValidator);
    this.eventBus = eventBus;
    this.securitySystem = checkNotNull(securitySystem);
  }

  @Override
  public synchronized void clearCache() {
    ldapConfiguration = null;
    // fire event
    eventBus.post(new LdapClearCacheEvent(this));
  }

  @Override
  public synchronized void setServerOrder(final List<String> orderdServerIds)
      throws InvalidConfigurationException
  {
    final List<CLdapServerConfiguration> ldapServers = getConfiguration().getServers();
    final ValidationResponse vr = configurationValidator.validateLdapServerOrder(ldapServers, orderdServerIds);
    if (vr.getValidationErrors().size() > 0) {
      throw new InvalidConfigurationException(vr);
    }

    // build a map so its easier
    final Map<String, CLdapServerConfiguration> idToServerMap = Maps.newHashMap();
    for (CLdapServerConfiguration ldapServer : ldapServers) {
      idToServerMap.put(ldapServer.getId(), ldapServer);
    }
    // now reorder them
    final List<CLdapServerConfiguration> newOrderedldapServers = Lists.newArrayList();
    for (String serverId : orderdServerIds) {
      newOrderedldapServers.add(idToServerMap.get(serverId));
    }
    getConfiguration().setServers(newOrderedldapServers);
    save();
  }

  @Override
  public synchronized List<CLdapServerConfiguration> listLdapServerConfigurations() {
    return ImmutableList.copyOf(getConfiguration().getServers());
  }

  @Override
  public synchronized CLdapServerConfiguration getLdapServerConfiguration(final String id)
      throws InvalidConfigurationException,
             LdapServerNotFoundException
  {
    for (CLdapServerConfiguration ldapServer : getConfiguration().getServers()) {
      if (ldapServer.getId().equals(id)) {
        return ldapServer;
      }
    }
    throw new LdapServerNotFoundException("Ldap Server: '" + id + "' was not found.");
  }

  @Override
  public synchronized void addLdapServerConfiguration(final CLdapServerConfiguration ldapServerConfiguration)
      throws InvalidConfigurationException
  {
    final ValidationResponse vr = configurationValidator
        .validateLdapServerConfiguration(ldapServerConfiguration, false);
    if (vr.getValidationErrors().size() > 0) {
      throw new InvalidConfigurationException(vr);
    }
    final boolean wasUnconfigured = getConfiguration().getServers().isEmpty();
    getConfiguration().addServer(ldapServerConfiguration);
    save();
    if (wasUnconfigured) {
      mayActivateLdapRealm();
    }
  }

  @Override
  public synchronized void updateLdapServerConfiguration(final CLdapServerConfiguration ldapServerConfiguration)
      throws InvalidConfigurationException,
             LdapServerNotFoundException
  {
    final ValidationResponse vr = configurationValidator
        .validateLdapServerConfiguration(ldapServerConfiguration, true);
    if (vr.getValidationErrors().size() > 0) {
      throw new InvalidConfigurationException(vr);
    }

    // this list is ordered so we need to replace the old one
    final CLdapConfiguration ldapConfiguration = getConfiguration();
    for (int ii = 0; ii < ldapConfiguration.getServers().size(); ii++) {
      CLdapServerConfiguration ldapServer = ldapConfiguration.getServers().get(ii);
      if (ldapServer.getId().equals(ldapServerConfiguration.getId())) {
        ldapConfiguration.getServers().remove(ii);
        ldapConfiguration.getServers().add(ii, ldapServerConfiguration);
      }
    }
    save();
  }

  @Override
  public synchronized void deleteLdapServerConfiguration(String id)
      throws InvalidConfigurationException,
             LdapServerNotFoundException
  {
    final boolean lastEntry = getConfiguration().getServers().size() == 1;
    for (Iterator<CLdapServerConfiguration> iter = getConfiguration().getServers().iterator(); iter
        .hasNext(); ) {
      CLdapServerConfiguration ldapServer = iter.next();
      if (ldapServer.getId().equals(id)) {
        iter.remove();
        save();
        if (lastEntry) {
          mayDeactivateLdapRealm();
        }
        return;
      }
    }
    throw new LdapServerNotFoundException("Ldap Server: '" + id + "' was not found.");
  }

  /**
   * Activates, if not activated already, the {@link EnterpriseLdapAuthenticatingRealm} as last realm in system.
   *
   * @since 2.7.0
   */
  private void mayActivateLdapRealm() throws InvalidConfigurationException {
    final List<String> activeRealms = securitySystem.getRealms();
    if (!activeRealms.contains(EnterpriseLdapAuthenticatingRealm.ID)) {
      activeRealms.add(EnterpriseLdapAuthenticatingRealm.ID);
      securitySystem.setRealms(activeRealms);
    }
  }

  /**
   * Deactivates, if not deactivated already, the {@link EnterpriseLdapAuthenticatingRealm realm in system.
   *
   * @since 2.7.0
   */
  private void mayDeactivateLdapRealm() throws InvalidConfigurationException {
    final List<String> activeRealms = securitySystem.getRealms();
    if (activeRealms.contains(EnterpriseLdapAuthenticatingRealm.ID)) {
      activeRealms.remove(EnterpriseLdapAuthenticatingRealm.ID);
      securitySystem.setRealms(activeRealms);
    }
  }

  private CLdapConfiguration getConfiguration() {
    if (ldapConfiguration == null) {
      try {
        final CLdapConfiguration config = configurationSource.load();
        final ValidationResponse vr = configurationValidator
            .validateModel(new ValidationRequest<CLdapConfiguration>(config));
        if (vr.getValidationErrors().size() > 0) {
          throw new InvalidConfigurationException(vr);
        }
        ldapConfiguration = config;
      }
      catch (ConfigurationException e) {
        log.error("Invalid LDAP Configuration", e);
      }
      catch (IOException e) {
        log.error("IOException while retrieving LDAP configuration file", e);
      }
    }
    return ldapConfiguration;
  }

  private void save() {
    try {
      configurationSource.save(ldapConfiguration);
    }
    catch (IOException e) {
      log.error("IOException while storing LDAP configuration file", e);
    }
    // fire clear cache event
    eventBus.post(new LdapClearCacheEvent(this));
  }
}
