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

package org.sonatype.nexus.coreui

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.hibernate.validator.constraints.NotEmpty
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.validation.Create
import org.sonatype.nexus.validation.Update
import org.sonatype.nexus.validation.Validate
import org.sonatype.security.SecuritySystem
import org.sonatype.security.authorization.AuthorizationManager
import org.sonatype.security.authorization.Role
import org.sonatype.security.usermanagement.xml.SecurityXmlUserManager

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.groups.Default

/**
 * Role {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_Role')
class RoleComponent
extends DirectComponentSupport
{

  public static final String DEFAULT_SOURCE = SecurityXmlUserManager.SOURCE

  @Inject
  SecuritySystem securitySystem

  @Inject
  List<AuthorizationManager> authorizationManagers

  /**
   * Retrieves roles form all available {@link AuthorizationManager}s.
   * @return a list of roles
   */
  @DirectMethod
  @RequiresPermissions('security:roles:read')
  List<RoleXO> read() {
    // finding if a role is a mapped role (stupid but that is all nexus allows)
    Map<String, List<String>> mappings = [:]
    authorizationManagers.each { manager ->
      if (manager.source != DEFAULT_SOURCE) {
        manager.listRoles().each { role ->
          def sources = mappings[role.roleId]
          if (!sources) {
            mappings << [(role.roleId): sources = []]
          }
          sources << role.source
        }
      }
    }
    return securitySystem.listRoles(DEFAULT_SOURCE).collect { input ->
      List<String> mappingsPerRole = mappings[input.roleId]
      return asRoleXO(input, mappingsPerRole ? mappingsPerRole.join(',') : input.source)
    }
  }

  /**
   * Retrieves available role sources.
   * @return list of sources
   */
  @DirectMethod
  List<ReferenceXO> readSources() {
    return authorizationManagers.findResults { manager ->
      return manager.source == DEFAULT_SOURCE ? null : manager
    }.collect { manager ->
      return new ReferenceXO(
          id: manager.source,
          name: manager.source
      )
    }
  }

  /**
   * Retrieves roles from specified source.
   * @param source to retrieve roles from
   * @return a list of roles
   */
  @DirectMethod
  @RequiresPermissions('security:roles:read')
  @Validate
  List<RoleXO> readFromSource(final @NotEmpty(message = '[source] may not be empty') String source) {
    return securitySystem.listRoles(source).collect { input ->
      return asRoleXO(input, input.source)
    }
  }

  /**
   * Creates a role.
   * @param roleXO to be created
   * @return created role
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:roles:create')
  @Validate(groups = [Create.class, Default.class])
  RoleXO create(final @NotNull(message = '[roleXO] may not be null') @Valid RoleXO roleXO) {
    return asRoleXO(securitySystem.getAuthorizationManager(DEFAULT_SOURCE).addRole(
        new Role(
            roleId: roleXO.id,
            source: roleXO.source,
            name: roleXO.name,
            description: roleXO.description,
            readOnly: false,
            privileges: roleXO.privileges,
            roles: roleXO.roles
        )
    ), roleXO.source)
  }

  /**
   * Updates a role.
   * @param roleXO to be updated
   * @return updated role
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:roles:update')
  @Validate(groups = [Update.class, Default.class])
  RoleXO update(final @NotNull(message = '[roleXO] may not be null') @Valid RoleXO roleXO) {
    return asRoleXO(securitySystem.getAuthorizationManager(DEFAULT_SOURCE).updateRole(
        new Role(
            roleId: roleXO.id,
            source: roleXO.source,
            name: roleXO.name,
            description: roleXO.description,
            readOnly: false,
            privileges: roleXO.privileges,
            roles: roleXO.roles
        )
    ), roleXO.source)
  }

  /**
   * Deletes a role.
   * @param id of role to be deleted
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:roles:delete')
  @Validate
  void delete_(final @NotEmpty(message = '[id] may not be empty') String id) {
    securitySystem.getAuthorizationManager(DEFAULT_SOURCE).deleteRole(id)
  }

  private static RoleXO asRoleXO(Role input, String source) {
    return new RoleXO(
        id: input.roleId,
        source: (source == DEFAULT_SOURCE || !source) ? 'Nexus' : source,
        name: input.name,
        description: input.description,
        readOnly: input.readOnly,
        privileges: input.privileges,
        roles: input.roles
    )
  }

}
