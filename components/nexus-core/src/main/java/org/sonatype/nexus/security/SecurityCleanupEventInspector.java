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

package org.sonatype.nexus.security;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeGroupPropertyDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeRepositoryPropertyDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeRepositoryTargetPropertyDescriptor;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.TargetRegistryEventRemove;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authorization.NoSuchAuthorizationManagerException;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.xml.SecurityXmlAuthorizationManager;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.ConfigurationManagerAction;

import com.google.common.base.Throwables;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component(role = EventInspector.class, hint = "SecurityCleanupEventInspector")
public class SecurityCleanupEventInspector
    extends AbstractEventInspector
{
  @Requirement(hint = "default")
  private ConfigurationManager configManager;

  @Requirement
  private SecuritySystem security;

  public boolean accepts(Event<?> evt) {
    return evt instanceof RepositoryRegistryEventRemove || evt instanceof TargetRegistryEventRemove;
  }

  public void inspect(Event<?> evt) {
    if (evt instanceof RepositoryRegistryEventRemove) {
      RepositoryRegistryEventRemove rEvt = (RepositoryRegistryEventRemove) evt;

      String repositoryId = rEvt.getRepository().getId();

      try {
        // Delete target privs that match repo/groupId
        cleanupPrivileges(TargetPrivilegeRepositoryPropertyDescriptor.ID, repositoryId);
        cleanupPrivileges(TargetPrivilegeGroupPropertyDescriptor.ID, repositoryId);
      }
      catch (NoSuchPrivilegeException e) {
        getLogger().error("Unable to clean privileges attached to repository", e);
      }
      catch (NoSuchAuthorizationManagerException e) {
        getLogger().error("Unable to clean privileges attached to repository", e);
      }
    }
    if (evt instanceof TargetRegistryEventRemove) {
      TargetRegistryEventRemove rEvt = (TargetRegistryEventRemove) evt;

      String targetId = rEvt.getTarget().getId();

      try {
        cleanupPrivileges(TargetPrivilegeRepositoryTargetPropertyDescriptor.ID, targetId);
      }
      catch (NoSuchPrivilegeException e) {
        getLogger().error("Unable to clean privileges attached to target: " + targetId, e);
      }
      catch (NoSuchAuthorizationManagerException e) {
        getLogger().error("Unable to clean privileges attached to target: " + targetId, e);
      }
    }
  }

  protected void cleanupPrivileges(String propertyId, String propertyValue)
      throws NoSuchPrivilegeException, NoSuchAuthorizationManagerException
  {
    Set<Privilege> privileges = security.listPrivileges();

    final Set<String> removedIds = new HashSet<String>();

    for (Privilege privilege : privileges) {
      if (!privilege.isReadOnly() && privilege.getType().equals(TargetPrivilegeDescriptor.TYPE)
          && (propertyValue.equals(privilege.getPrivilegeProperty(propertyId)))) {
        getLogger().debug("Removing Privilege " + privilege.getName() + " because repository was removed");
        security.getAuthorizationManager(SecurityXmlAuthorizationManager.SOURCE).deletePrivilege(
            privilege.getId());
        removedIds.add(privilege.getId());
      }
    }

    try {
      configManager.runWrite(new ConfigurationManagerAction()
      {
        @Override
        public void run()
            throws Exception
        {
          for (String privilegeId : removedIds) {
            configManager.cleanRemovedPrivilege(privilegeId);
          }
          configManager.save();
        }

      });
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }
}
