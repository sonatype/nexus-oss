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
package com.sonatype.nexus.ssl.plugin.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CProperty
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * SSL plugin static security resource.
 *
 * @since 3.0
 */
@Named
@Singleton
class SecurityResource
implements StaticSecurityResource
{
  @Override
  Configuration getConfiguration() {
    return new Configuration(
        privileges: [
            new CPrivilege(
                id: 'ssl-truststore-read',
                type: 'method',
                name: 'Nexus SSL Trust Store - (read)',
                description: 'Give permission to read certificates from Nexus SSL Trust Store.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:ssl:truststore')
                ])
            ,
            new CPrivilege(
                id: 'ssl-truststore-create',
                type: 'method',
                name: 'Nexus SSL Trust Store - (create,read)',
                description: 'Give permission to create,read certificates from Nexus SSL Trust Store.',
                properties: [
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'permission', value: 'nexus:ssl:truststore')
                ])
            ,
            new CPrivilege(
                id: 'ssl-truststore-update',
                type: 'method',
                name: 'Nexus SSL Trust Store - (create,read)',
                description: 'Give permission to update,read certificates from Nexus SSL Trust Store.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'permission', value: 'nexus:ssl:truststore')
                ])
            ,
            new CPrivilege(
                id: 'ssl-truststore-delete',
                type: 'method',
                name: 'Nexus SSL Trust Store - (delete,read)',
                description: 'Give permission to delete,read certificates from Nexus SSL Trust Store.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'permission', value: 'nexus:ssl:truststore')
                ])
        ],
        roles: [
            new CRole(
                id: 'ssl-truststore-view',
                name: 'Nexus SSL: Trust Store View',
                description: 'Gives access to view Nexus SSL Trust Store',
                privileges: ['ssl-truststore-read', 'ssl-certificates-read']
            )
            ,
            new CRole(
                id: 'ssl-truststore-admin',
                name: 'Nexus SSL: Trust Store Administration',
                description: 'Gives access to manage Nexus SSL Trust Store',
                privileges: ['ssl-truststore-create', 'ssl-truststore-update', 'ssl-truststore-delete']
            )
        ]
    )
  }
}

