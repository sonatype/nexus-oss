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
package org.sonatype.nexus.internal.security;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.TargetRegistryEventRemove;
import org.sonatype.nexus.proxy.targets.TargetPrivilegeDescriptor;
import org.sonatype.nexus.security.SecuritySystem;
import org.sonatype.nexus.security.authz.NoSuchAuthorizationManagerException;
import org.sonatype.nexus.security.config.SecurityConfigurationManager;
import org.sonatype.nexus.security.internal.AuthorizationManagerImpl;
import org.sonatype.nexus.security.privilege.NoSuchPrivilegeException;
import org.sonatype.nexus.security.privilege.Privilege;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
@Named
public class SecurityCleanupEventInspector
    extends ComponentSupport
    implements EventSubscriber
{
  private final SecurityConfigurationManager configManager;

  private final SecuritySystem security;

  @Inject
  public SecurityCleanupEventInspector(SecurityConfigurationManager configManager, SecuritySystem security) {
    this.configManager = checkNotNull(configManager);
    this.security = checkNotNull(security);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final RepositoryRegistryEventRemove event) {
    String repositoryId = event.getRepository().getId();

    try {
      // Delete target privs that match repo/groupId
      cleanupPrivileges(TargetPrivilegeDescriptor.P_REPOSITORY_ID, repositoryId);
      cleanupPrivileges(TargetPrivilegeDescriptor.P_GROUP_ID, repositoryId);
    }
    catch (NoSuchPrivilegeException | NoSuchAuthorizationManagerException e) {
      log.error("Unable to clean privileges attached to repository", e);
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void on(final TargetRegistryEventRemove event) {
    String targetId = event.getTarget().getId();

    try {
      cleanupPrivileges(TargetPrivilegeDescriptor.P_TARGET_ID, targetId);
    }
    catch (NoSuchPrivilegeException | NoSuchAuthorizationManagerException e) {
      log.error("Unable to clean privileges attached to target: {}", targetId, e);
    }
  }

  private void cleanupPrivileges(String propertyId, String propertyValue)
      throws NoSuchPrivilegeException, NoSuchAuthorizationManagerException
  {
    Set<Privilege> privileges = security.listPrivileges();

    final Set<String> removedIds = new HashSet<String>();

    for (Privilege privilege : privileges) {
      if (!privilege.isReadOnly() && privilege.getType().equals(TargetPrivilegeDescriptor.TYPE)
          && (propertyValue.equals(privilege.getPrivilegeProperty(propertyId)))) {
        log.debug("Removing Privilege {} because repository was removed", privilege.getName());
        security.getAuthorizationManager(AuthorizationManagerImpl.SOURCE).deletePrivilege(
            privilege.getId());
        removedIds.add(privilege.getId());
      }
    }

    for (String privilegeId : removedIds) {
      configManager.cleanRemovedPrivilege(privilegeId);
    }
  }
}
