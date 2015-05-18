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

package org.sonatype.nexus.repository.internal

import javax.inject.Named
import javax.inject.Singleton

import org.sonatype.nexus.security.config.CPrivilege
import org.sonatype.nexus.security.config.MemorySecurityConfiguration
import org.sonatype.nexus.security.config.StaticSecurityConfigurationResource

/**
 * Repository security configuration.
 *
 * @since 3.0
 */
@Named
@Singleton
class StaticSecurityConfigurationResourceImpl
    implements StaticSecurityConfigurationResource
{
  @Override
  MemorySecurityConfiguration getConfiguration() {
    return new MemorySecurityConfiguration(
        privileges: [
            //
            // nexus:repository-view
            //

            new CPrivilege(
                id: 'repository-view-*-*-*',
                type: 'repository-view',
                properties: [
                    format: '*',
                    repository: '*',
                    actions: '*'
                ]
            ),
            new CPrivilege(
                id: 'repository-view-*-*-browse',
                type: 'repository-view',
                properties: [
                    format: '*',
                    repository: '*',
                    actions: 'browse'
                ]
            ),
            new CPrivilege(
                id: 'repository-view-*-*-read',
                type: 'repository-view',
                properties: [
                    format: '*',
                    repository: '*',
                    actions: 'read'
                ]
            ),
            new CPrivilege(
                id: 'repository-view-*-*-edit',
                type: 'repository-view',
                properties: [
                    format: '*',
                    repository: '*',
                    actions: 'edit'
                ]
            ),
            new CPrivilege(
                id: 'repository-view-*-*-add',
                type: 'repository-view',
                properties: [
                    format: '*',
                    repository: '*',
                    actions: 'add'
                ]
            ),
            new CPrivilege(
                id: 'repository-view-*-*-delete',
                type: 'repository-view',
                properties: [
                    format: '*',
                    repository: '*',
                    actions: 'delete'
                ]
            ),
        ]
    )
  }
}

