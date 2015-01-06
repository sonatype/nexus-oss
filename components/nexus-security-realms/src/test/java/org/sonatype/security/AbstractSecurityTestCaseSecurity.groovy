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
package org.sonatype.security

import org.sonatype.security.configuration.model.SecurityConfiguration
import org.sonatype.security.model.CUser
import org.sonatype.security.model.CUserRoleMapping
import org.sonatype.security.model.Configuration

/**
 * @since 3.0
 */
class AbstractSecurityTestCaseSecurity
{

  static SecurityConfiguration security() {
    return new SecurityConfiguration(
        anonymousAccessEnabled: false,
        anonymousUsername: 'anonymous',
        realms: [
            'NexusAuthenticatingRealm',
            'NexusAuthorizingRealm'
        ]
    )
  }

  static Configuration securityModel() {
    return new Configuration(
        users: [
            new CUser(
                id: 'admin',
                password: 'f865b53623b121fd34ee5426c792e5c33af8c227',
                firstName: 'Administrator',
                status: 'active',
                email: 'changeme@yourcompany.com'
            ),
            new CUser(
                id: 'deployment',
                password: 'b2a0e378437817cebdf753d7dff3dd75483af9e0',
                firstName: 'Deployment',
                lastName: 'User',
                status: 'active',
                email: 'changeme1@yourcompany.com'
            ),
            new CUser(
                id: 'anonymous',
                password: '0a92fab3230134cca6eadd9898325b9b2ae67998',
                firstName: 'Nexus',
                lastName: 'Anonynmous User',
                status: 'active',
                email: 'changeme2@yourcompany.com'
            )
        ],
        userRoleMappings: [
            new CUserRoleMapping(
                userId: 'admin',
                source: 'default',
                roles: ['admin']
            ),
            new CUserRoleMapping(
                userId: 'deployment',
                source: 'default',
                roles: ['deployment', 'repo-all-full']
            ),
            new CUserRoleMapping(
                userId: 'anonymous',
                source: 'default',
                roles: ['anonymous', 'repo-all-read']
            )
        ],
    )
  }

}

