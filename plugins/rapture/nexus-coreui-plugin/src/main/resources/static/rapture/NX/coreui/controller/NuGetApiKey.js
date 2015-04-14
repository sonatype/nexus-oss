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
/*global Ext, NX*/

/**
 * NuGet repository settings controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.NuGetApiKey', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.Conditions',
    'NX.Permissions',
    'NX.Security'
  ],

  views: [
    'nuget.NuGetApiKeyDetails',
    'nuget.NuGetApiKey'
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

    me.getApplication().getFeaturesController().registerFeature([
      {
        mode: 'user',
        path: '/NuGet API Token',
        text: NX.I18n.get('NUGET_APIKEY_TITLE'),
        description: NX.I18n.get('NUGET_APIKEY_SUB_TITLE'),
        view: { xtype: 'nx-coreui-nuget-apikey' },
        iconConfig: {
          file: 'key.png',
          variants: ['x16', 'x32']
        },
        visible: function() {
          return NX.Security.hasUser();
        }
      }
    ], me);

    me.listen({
      component: {
        'nx-coreui-nuget-apikey button[action=access]': {
          click: me.accessApiKey,
          afterrender: me.bindAccessApiKeyButton
        },
        'nx-coreui-nuget-apikey button[action=reset]': {
          click: me.resetApiKey,
          afterrender: me.bindResetApiKeyButton
        }
      }
    });
  },

  /**
   * @private
   * Authenticate & show API Key.
   */
  accessApiKey: function() {
    var me = this;

    NX.Security.doWithAuthenticationToken(
        NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_NUGET_ACCESS'),
        {
          success: function(authToken) {
            NX.direct.nuget_NuGetApiKey.readKey(authToken, function(response) {
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
        NX.I18n.get('LEGACY_ADMIN_REPOSITORIES_NUGET_RESET'),
        {
          success: function(authToken) {
            NX.direct.nuget_NuGetApiKey.resetKey(authToken, function(response) {
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
    var me = this;

    Ext.widget('nx-coreui-nuget-apikeydetails', { apiKey: apiKey });
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
