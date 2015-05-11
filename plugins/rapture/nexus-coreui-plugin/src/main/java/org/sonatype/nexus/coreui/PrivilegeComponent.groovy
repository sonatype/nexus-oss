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
package org.sonatype.nexus.coreui

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.security.SecuritySystem
import org.sonatype.nexus.security.authz.AuthorizationManager
import org.sonatype.nexus.security.privilege.Privilege
import org.sonatype.nexus.security.privilege.PrivilegeDescriptor
import org.sonatype.nexus.security.user.UserManager
import org.sonatype.nexus.validation.Validate

import com.google.common.collect.Maps
import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import groovy.transform.PackageScope
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.hibernate.validator.constraints.NotEmpty

/**
 * Privilege {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_Privilege')
class PrivilegeComponent
    extends DirectComponentSupport
{
  public static final String DEFAULT_SOURCE = UserManager.DEFAULT_SOURCE

  @Inject
  SecuritySystem securitySystem

  @Inject
  List<PrivilegeDescriptor> privilegeDescriptors

  /**
   * Retrieves privileges.
   * @return a list of privileges
   */
  @DirectMethod
  @RequiresPermissions('security:privileges:read')
  List<PrivilegeXO> read() {
    return securitySystem.listPrivileges().collect {input ->
      return asPrivilegeXO(input)
    }
  }

  /**
   * Deletes a privilege, if is not readonly.
   * @param id of privilege to be deleted
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:privileges:delete')
  @Validate
  void remove(final @NotEmpty(message = '[id] may not be empty') String id) {
    AuthorizationManager authorizationManager = securitySystem.getAuthorizationManager(DEFAULT_SOURCE)
    if (authorizationManager.getPrivilege(id)?.isReadOnly()) {
      throw new IllegalAccessException("Privilege [${id}] is readonly and cannot be deleted")
    }
    authorizationManager.deletePrivilege(id)
  }

  /**
   * Convert privilege to XO.
   */
  @PackageScope
  PrivilegeXO asPrivilegeXO(Privilege input) {
    return new PrivilegeXO(
        id: input.id,
        version: input.version,
        name: input.name,
        description: input.description,
        type: input.type,
        readOnly: input.readOnly,
        properties: Maps.newHashMap(input.properties),
        permission: input.permission
    )
  }
}
