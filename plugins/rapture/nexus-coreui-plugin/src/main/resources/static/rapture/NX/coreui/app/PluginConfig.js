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
Ext.define('NX.coreui.app.PluginConfig', {

  namespaces: [
    'NX.coreui'
  ],

  controllers: [
    'NX.coreui.controller.Capabilities',
    'NX.coreui.controller.BrowseRepositories',
    'NX.coreui.controller.BrowseRepositoryItemInfo',
    'NX.coreui.controller.BrowseRepositoryItemClm',
    'NX.coreui.controller.BrowseStorage',
    'NX.coreui.controller.BrowseIndex',
    'NX.coreui.controller.MavenUpload',
    'NX.coreui.controller.PluginConsole',
    'NX.coreui.controller.Repositories',
    'NX.coreui.controller.RepositoryTargets',
    'NX.coreui.controller.RepositoryRoutes',
    'NX.coreui.controller.Privileges',
    'NX.coreui.controller.Roles',
    'NX.coreui.controller.Security',
    'NX.coreui.controller.System',
    'NX.coreui.controller.Tasks',
    'NX.coreui.controller.UploadArtifact',
    'NX.coreui.controller.Users'
  ]
});