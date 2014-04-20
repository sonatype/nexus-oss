/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * Analytics controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.AnalyticsSettings', {
  extend: 'Ext.app.Controller',

  views: [
    'analytics.AnalyticsSettings'
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getFeaturesController().registerFeature({
      mode: 'admin',
      path: '/Support/Analytics',
      description: 'Manage analytics configuration',
      view: { xtype: 'nx-coreui-analytics-settings' },
      iconConfig: {
        file: 'system_monitor.png',
        variants: ['x16', 'x32']
      },
      visible: function () {
        return NX.Permissions.check('nexus:analytics', 'read');
      }
    });

    me.listen({
      component: {
        'nx-coreui-analytics-settings nx-settingsform': {
          submitted: me.onSubmitted
        }
      }
    });
  },

  /**
   * @private
   * Set "analytics" state on save.
   */
  onSubmitted: function (form, action) {
    NX.State.setValue('analytics', { enabled: action.result.data.collection });
  }

});