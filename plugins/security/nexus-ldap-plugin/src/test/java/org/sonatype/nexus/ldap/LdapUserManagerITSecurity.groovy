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
package org.sonatype.nexus.ldap

import org.sonatype.nexus.security.config.CUser
import org.sonatype.nexus.security.config.CUserRoleMapping
import org.sonatype.nexus.security.config.MemorySecurityConfiguration

/**
 * @since 3.0
 */
class LdapUserManagerITSecurity
{

  static MemorySecurityConfiguration securityModel() {
    return new MemorySecurityConfiguration(
        users: [
            new CUser(
                id: 'brianf',
                password: 'b2a0e378437817cebdf753d7dff3dd75483af9e0',
                firstName: 'Brian',
                lastName: 'User',
                email: 'change@me.com',
                status: 'active'
            )
        ],
        userRoleMappings: [
            new CUserRoleMapping(userId: 'brianf', source: 'LDAP', roles: ['admin']),
            new CUserRoleMapping(userId: 'cstamas', source: 'LDAP', roles: ['anonymous', 'developer'])
        ]
    )
  }

}

