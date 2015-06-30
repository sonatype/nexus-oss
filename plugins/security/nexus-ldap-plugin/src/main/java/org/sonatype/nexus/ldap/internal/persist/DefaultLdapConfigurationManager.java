/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ldap.internal.persist;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.ldap.LdapRealm;
import org.sonatype.nexus.ldap.internal.LdapConstants;
import org.sonatype.nexus.ldap.internal.events.LdapClearCacheEvent;
import org.sonatype.nexus.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.nexus.ldap.internal.persist.entity.Validator;
import org.sonatype.nexus.security.realm.RealmManager;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link LdapConfigurationManager}.
 */
@Named
@Singleton
public class DefaultLdapConfigurationManager
    extends ComponentSupport
    implements LdapConfigurationManager
{
  private final LdapConfigurationSource configurationSource;

  private final Validator validator;

  private final EventBus eventBus;

  private final RealmManager realmManager;

  /**
   * Configuration cache.
   */
  private final LinkedHashMap<String, LdapConfiguration> cache;

  private boolean cachePrimed;

  @Inject
  public DefaultLdapConfigurationManager(final LdapConfigurationSource configurationSource,
                                         final Validator validator,
                                         final EventBus eventBus,
                                         final RealmManager realmManager)
  {
    this.configurationSource = checkNotNull(configurationSource);
    this.validator = checkNotNull(validator);
    this.eventBus = checkNotNull(eventBus);
    this.realmManager = checkNotNull(realmManager);
    this.cache = Maps.newLinkedHashMap();
    this.cachePrimed = false;
  }

  @Override
  public synchronized void clearCache() {
    cache.clear();
    cachePrimed = false;
    eventBus.post(new LdapClearCacheEvent(this));
  }

  @Override
  public synchronized void setServerOrder(final List<String> orderdServerIds)
      throws IllegalArgumentException
  {
    checkNotNull(orderdServerIds);
    final LinkedHashMap<String, LdapConfiguration> configuration = getConfiguration();
    final Set<String> newOrder = Sets.newHashSet(orderdServerIds);
    checkArgument(newOrder.size() == orderdServerIds.size(), "Duplicate keys provided: %s", orderdServerIds);
    final Set<String> configIds = configuration.keySet();
    checkArgument(newOrder.equals(configIds), "ID ordering mismatch (the new and existing should differ in order only):  new=%s, existing=%s)", orderdServerIds, configIds);

    for (LdapConfiguration config : configuration.values()) {
      config.setOrder(orderdServerIds.indexOf(config.getId()));
      configurationSource.update(config);
    }
    clearCache();
  }

  @Override
  public synchronized List<LdapConfiguration> listLdapServerConfigurations() {
    return ImmutableList.copyOf(getConfiguration().values());
  }

  @Override
  public synchronized LdapConfiguration getLdapServerConfiguration(final String id)
      throws LdapServerNotFoundException
  {
    checkNotNull(id);
    final LdapConfiguration configuration = getConfiguration().get(id);
    if (configuration != null) {
      return configuration;
    }
    throw new LdapServerNotFoundException("Ldap Server: '" + id + "' was not found.");
  }

  @Override
  public synchronized String addLdapServerConfiguration(final LdapConfiguration ldapServerConfiguration)
      throws IllegalArgumentException
  {
    checkNotNull(ldapServerConfiguration);
    validator.validate(ldapServerConfiguration);
    ldapServerConfiguration.setOrder(Integer.MAX_VALUE); // should be last
    final LinkedHashMap<String, LdapConfiguration> config = getConfiguration();
    final List<String> existingIds = Lists.newArrayList(config.keySet()); // preserve it, as clearCache clears this
    final boolean firstEntry = config.isEmpty();
    configurationSource.create(ldapServerConfiguration);
    clearCache();
    // adjust order that will get rid of MAX_VALUE
    existingIds.add(ldapServerConfiguration.getId());
    setServerOrder(existingIds);
    if (firstEntry) {
      mayActivateLdapRealm();
    }
    return ldapServerConfiguration.getId();
  }


  @Override
  public synchronized void updateLdapServerConfiguration(final LdapConfiguration ldapServerConfiguration)
      throws IllegalArgumentException, LdapServerNotFoundException
  {
    checkNotNull(ldapServerConfiguration);
    validator.validate(ldapServerConfiguration);
    checkArgument(ldapServerConfiguration.getId() != null, "'id' is null, cannot update");
    final LinkedHashMap<String, LdapConfiguration> config = getConfiguration();
    if (!config.containsKey(ldapServerConfiguration.getId())) {
      throw new LdapServerNotFoundException("Ldap Server: '" + ldapServerConfiguration.getId() + "' was not found.");
    }
    configurationSource.update(ldapServerConfiguration);
    clearCache();
  }

  @Override
  public synchronized void deleteLdapServerConfiguration(final String id)
      throws LdapServerNotFoundException
  {
    checkNotNull(id);
    final LinkedHashMap<String, LdapConfiguration> config = getConfiguration();
    final boolean lastEntry = config.size() == 1;
    if (config.containsKey(id)) {
      configurationSource.delete(id);
      clearCache();
      if (lastEntry) {
        mayDeactivateLdapRealm();
      }
      return;
    }
    throw new LdapServerNotFoundException("Ldap Server: '" + id + "' was not found.");
  }

  /**
   * Activates, if not activated already, the {@link LdapRealm} as last realm in system.
   *
   * @since 2.7.0
   */
  private void mayActivateLdapRealm() {
    realmManager.enableRealm(LdapConstants.REALM_NAME);
  }

  /**
   * Deactivates, if not deactivated already, the {@link LdapRealm realm in system.
   *
   * @since 2.7.0
   */
  private void mayDeactivateLdapRealm() {
    realmManager.disableRealm(LdapConstants.REALM_NAME);
  }

  /**
   * Primes the cache if not primed and returns it, so to say "lazily" loads. Should be called only from synchronized
   * method.
   */
  private LinkedHashMap<String, LdapConfiguration> getConfiguration() {
    if (!cachePrimed) {
      try {
        cache.clear();
        final List<LdapConfiguration> ldapConfigurations = Lists.newArrayList(configurationSource.loadAll());
        Collections.sort(ldapConfigurations, new Comparator<LdapConfiguration>()
        {
          @Override
          public int compare(final LdapConfiguration o1, final LdapConfiguration o2) {
            return o1.getOrder() - o2.getOrder();
          }
        });
        for (LdapConfiguration ldapConfiguration : ldapConfigurations) {
          cache.put(ldapConfiguration.getId(), ldapConfiguration);
        }
      }
      catch (Exception e) {
        log.warn("Cannot retrieve LDAP configuration", e);
        throw Throwables.propagate(e);
      }
      finally {
        cachePrimed = true;
      }
    }
    return cache;
  }
}
