/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global Ext, NX*/

/**
 * NuGet upload controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.NuGetUpload', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.Permissions'
  ],

  stores: [
    'NuGetUploadRepositoryHosted'
  ],
  views: [
    'nuget.NuGetUpload'
  ],
  refs: [
    { ref: 'uploadPanel', selector: 'nx-coreui-nuget-upload' }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    // preload icon to avoid a grey icon when nuget upload feature will be registered
    me.getApplication().getIconController().addIcons({
      'feature-nuget-upload': {
        file: 'upload.png',
        variants: ['x16', 'x32']
      }
    });

    me.onNuGetUploadStateChanged(NX.State.getValue('nuGetUpload'), undefined, true);

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.loadRepositories
        },
        '#State': {
          nugetuploadchanged: me.onNuGetUploadStateChanged
        }
      },
      component: {
        'nx-coreui-nuget-upload': {
          beforerender: me.loadRepositories
        }
      }
    });
  },

  /**
   * @private
   * Loads repositories if upload panel is active.
   */
  loadRepositories: function () {
    var me = this,
        uploadPanel = me.getUploadPanel();

    if (uploadPanel) {
      me.getNuGetUploadRepositoryHostedStore().load();
    }
  },

  onNuGetUploadStateChanged: function (newState, oldState, avoidMenuRefresh) {
    var me = this,
        features = me.getApplication().getFeaturesController(),
        shouldRefreshMenu = false;

    if (oldState && oldState.enabled) {
      features.unregisterFeature({
        mode: 'browse',
        path: '/Upload/NuGet'
      });
      shouldRefreshMenu = true;
    }
    if (newState && newState.enabled) {
      features.registerFeature({
        mode: 'browse',
        path: '/Upload/NuGet',
        description: 'Upload packages to Nuget Hosted Repositories',
        view: { xtype: 'nx-coreui-nuget-upload' },
        // use preloaded icon to avoid a grey icon
        iconName: 'feature-nuget-upload',
        visible: function () {
          return NX.Permissions.check('nexus:artifact', 'create');
        }
      });
      shouldRefreshMenu = true;
    }
    if (!avoidMenuRefresh && shouldRefreshMenu) {
      me.getController('Menu').refreshMenu();
    }
  }

});
