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
/*global Ext, NX*/

/**
 * NuGet repository settings controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.NuGetRepositorySettings', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.Conditions',
    'NX.Permissions',
    'NX.Security'
  ],

  views: [
    'nuget.NuGetApiKeyDetails',
    'nuget.NuGetRepositorySettings'
  ],
  refs: [
    { ref: 'feature', selector: 'nx-coreui-repository-feature' },
    { ref: 'panel', selector: 'nx-coreui-nuget-repository-settings' }
  ],

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'nuget-default': {
        file: 'key.png',
        variants: ['x16', 'x32']
      }
    });

    me.listen({
      component: {
        'nx-coreui-repository-list': {
          selection: me.onSelection
        },
        'nx-coreui-nuget-repository-settings button[action=access]': {
          click: me.accessApiKey,
          afterrender: me.bindAccessApiKeyButton
        },
        'nx-coreui-nuget-repository-settings button[action=reset]': {
          click: me.resetApiKey,
          afterrender: me.bindResetApiKeyButton
        }
      }
    });
  },

  /**
   * @private
   * Add "NuGet" panel to repository tabs, if not already present and/or load NuGet settings into the panel.
   * @param {NX.coreui.view.repository.RepositoryList} grid repository grid
   * @param {NX.coreui.model.Repository} model selected repository
   */
  onSelection: function(grid, model) {
    var me = this,
        panel = me.getPanel();

    if (model && NX.Permissions.check('apikey:access', 'read') && model.get('format') === 'nuget') {
      if (!panel) {
        me.getFeature().addTab({ xtype: 'nx-coreui-nuget-repository-settings', title: 'NuGet' });
        panel = me.getPanel();
      }
      panel.setRepository(model);
    }
    else {
      if (panel) {
        me.getFeature().removeTab(panel);
      }
    }
  },

  /**
   * @private
   * Authenticate & show API Key.
   */
  accessApiKey: function() {
    var me = this;

    NX.Security.doWithAuthenticationToken(
        'Accessing NuGet API Key requires validation of your credentials.',
        {
          success: function(authToken) {
            NX.direct.nuget_NuGet.readKey(authToken, function(response) {
              if (Ext.isDefined(response) && response.success) {
                me.showApiKey(response.data);
              }
            });
          }
        }
    );
  },

  /**
   * @private
   * Authenticate & reset API Key.
   */
  resetApiKey: function() {
    var me = this;

    NX.Security.doWithAuthenticationToken(
        'Resetting NuGet API Key requires validation of your credentials.',
        {
          success: function(authToken) {
            NX.direct.nuget_NuGet.resetKey(authToken, function(response) {
              if (Ext.isDefined(response) && response.success) {
                me.showApiKey(response.data);
              }
            });
          }
        }
    );
  },

  /**
   * @private
   * Show API Key details window.
   * @param {String} apiKey to show
   */
  showApiKey: function(apiKey) {
    var me = this,
        repositoryId = me.getPanel().getRepository().getId();

    Ext.widget('nx-coreui-nuget-apikeydetails', { repositoryId: repositoryId, apiKey: apiKey });
  },

  /**
   * @private
   * Enable 'Access API Key' when user has 'apikey:access:read' permission.
   * @param {Ext.Button} button access API Key button
   */
  bindAccessApiKeyButton: function(button) {
    button.mon(
        NX.Conditions.isPermitted('apikey:access', 'read'),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  /**
   * @private
   * Enable 'Reset API Key' when user has 'apikey:access:delete' permission.
   * @param {Ext.Button} button reset API Key button
   */
  bindResetApiKeyButton: function(button) {
    button.mon(
        NX.Conditions.isPermitted('apikey:access', 'delete'),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  }

});
