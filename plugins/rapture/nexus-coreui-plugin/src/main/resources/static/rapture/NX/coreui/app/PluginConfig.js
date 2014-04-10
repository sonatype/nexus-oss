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
/**
 * CoreUi plugin configuration.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.app.PluginConfig', {

  namespaces: [
    'NX.coreui'
  ],

  controllers: [
    'NX.coreui.controller.AnonymousSettings',
    { id: 'NX.coreui.controller.Capabilities',
      active: function () {
        return NX.app.Application.pluginActive('org.sonatype.nexus.plugins:nexus-capabilities-plugin');
      }
    },
    'NX.coreui.controller.BrowseRepositories',
    'NX.coreui.controller.BrowseStorage',
    'NX.coreui.controller.BrowseIndex',
    'NX.coreui.controller.ComponentSummary',
    'NX.coreui.controller.ComponentLicense',
    'NX.coreui.controller.ComponentSecurity',
    'NX.coreui.controller.FeatureGroups',
    'NX.coreui.controller.HttpSettings',
    'NX.coreui.controller.GeneralSettings',
    { id: 'NX.coreui.controller.Log',
      active: function () {
        return NX.app.Application.pluginActive('org.sonatype.nexus.plugins:nexus-logging-plugin');
      }
    },
    { id: 'NX.coreui.controller.Loggers',
      active: function () {
        return NX.app.Application.pluginActive('org.sonatype.nexus.plugins:nexus-logging-plugin');
      }
    },
    'NX.coreui.controller.MavenUpload',
    'NX.coreui.controller.NotificationSettings',
    { id: 'NX.coreui.controller.PluginConsole',
      active: function () {
        return NX.app.Application.pluginActive('org.sonatype.nexus.plugins:nexus-plugin-console-plugin');
      }
    },
    'NX.coreui.controller.Repositories',
    'NX.coreui.controller.RepositoryTargets',
    'NX.coreui.controller.RepositoryRoutes',
    'NX.coreui.controller.Privileges',
    'NX.coreui.controller.RealmSettings',
    'NX.coreui.controller.Roles',
    { id: 'NX.coreui.controller.SysInfo',
      active: function () {
        return NX.app.Application.pluginActive('org.sonatype.nexus.plugins:nexus-atlas-plugin');
      }
    },
    { id: 'NX.coreui.controller.SupportZip',
      active: function () {
        return NX.app.Application.pluginActive('org.sonatype.nexus.plugins:nexus-atlas-plugin');
      }
    },
    'NX.coreui.controller.Search',
    'NX.coreui.controller.Tasks',
    'NX.coreui.controller.UploadArtifact',
    'NX.coreui.controller.Users'
  ]
});