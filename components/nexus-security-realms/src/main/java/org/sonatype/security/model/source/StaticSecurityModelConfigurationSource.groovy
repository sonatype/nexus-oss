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
package org.sonatype.security.model.source

import org.sonatype.security.model.CUser
import org.sonatype.security.model.CUserRoleMapping
import org.sonatype.security.model.Configuration

import javax.inject.Named
import javax.inject.Singleton

/**
 * Security model configuration defaults.
 *
 * @since 3.0
 */
@Named('static')
@Singleton
class StaticSecurityModelConfigurationSource
extends PreconfiguredSecurityModelConfigurationSource
implements SecurityModelConfigurationSource
{

  StaticSecurityModelConfigurationSource() {
    super(new Configuration(
        users: [
            new CUser(
                id: 'admin',
                password: '$shiro1$SHA-512$1024$NE+wqQq/TmjZMvfI7ENh/g==$V4yPw8T64UQ6GfJfxYq2hLsVrBY8D1v+bktfOxGdt4b/9BthpWPNUy/CBk6V9iA0nHpzYzJFWO8v/tZFtES8CA==',
                firstName: 'Administrator',
                lastName: 'User',
                status: 'active',
                email: 'changeme@yourcompany.com'
            ),
            new CUser(
                id: 'deployment',
                password: '$shiro1$SHA-512$1024$xaOIRR7k7PL9Gf8SzkZ/eg==$CkCu6iigYnoWPb7vVkqCp7bt0rFAvHmHoA61S2H+59ithA6vhW8PsgoidziAirE1uglffKrcAJ++8RdHvIpTLA==',
                firstName: 'Deployment',
                lastName: 'User',
                status: 'active',
                email: 'changeme1@yourcompany.com'
            ),
            new CUser(
                id: 'anonymous',
                password: '$shiro1$SHA-512$1024$CPJm1XWdYNg5eCAYp4L4HA==$HIGwnJhC07ZpgeVblZcFRD1F6KH+xPG8t7mIcEMbfycC+n5Ljudyoj9dzdinrLmChTrmKMCw2/z29F7HeLbTbQ==',
                firstName: 'Nexus',
                lastName: 'Anonymous User',
                status: 'active',
                email: 'changeme2@yourcompany.com'
            )
        ],
        userRoleMappings: [
            new CUserRoleMapping(
                userId: 'admin',
                source: 'default',
                roles: ['nx-admin']
            ),
            new CUserRoleMapping(
                userId: 'deployment',
                source: 'default',
                roles: ['nx-deployment', 'repository-any-full']
            ),
            new CUserRoleMapping(
                userId: 'anonymous',
                source: 'default',
                roles: ['anonymous', 'repository-any-read']
            )
        ],
    ))
  }

}

