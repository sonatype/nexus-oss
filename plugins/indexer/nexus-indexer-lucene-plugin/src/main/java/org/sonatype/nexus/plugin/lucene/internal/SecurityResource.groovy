/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugin.lucene.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * Lucene indexer plugin static security resource.
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
                id: 'search', // 17
                type: 'method',
                name: 'Search Repositories',
                description: 'Give permission to perform searches of repository content.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:index'
                ]
            ),
            new CPrivilege(
                id: 'reindex', // 18
                type: 'method',
                name: 'Reindex',
                description: 'Give permission to Reindex repository content.  The extents of this privilege are related to the allowed targets.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'nexus:index'
                ]
            ),
            new CPrivilege(
                id: 'search-checksum', // 19
                type: 'method',
                name: 'Checksum Search',
                description: 'Give permission to perform checksum type searches of repository content.  The extents of this privilege are related to the allowed targets.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:identify'
                ]
            )
        ],
        roles: [
            new CRole(
                id: 'ui-search',
                name: 'UI: Search',
                description: 'Gives access to the Search screen in Nexus UI',
                privileges: ['search', 'search-checksum', 'artifact-read']
            ),
            new CRole(
                id: 'anonymous',
                roles: ['ui-search']
            )
        ]
    )
  }
}

