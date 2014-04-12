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
 * Notification System Settings controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.NotificationSettings', {
  extend: 'Ext.app.Controller',

  stores: [
    'Role'
  ],
  views: [
    'system.NotificationSettings'
  ],

  refs: [
    {
      ref: 'panel',
      selector: 'nx-coreui-system-notification-settings'
    }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getFeaturesController().registerFeature({
      mode: 'admin',
      path: '/System/Notifications',
      description: 'Manage notification configuration',
      view: { xtype: 'nx-coreui-system-notification-settings' },
      iconConfig: {
        file: 'emails.png',
        variants: ['x16', 'x32']
      },
      visible: function () {
        return NX.Permissions.check('nexus:settings', 'read');
      }
    });

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.loadRoles
        }
      },
      component: {
        'nx-coreui-system-notification-settings': {
          beforerender: me.loadRoles
        }
      }
    });
  },

  /**
   * @private
   * Loads roles store.
   */
  loadRoles: function () {
    var me = this,
        panel = me.getPanel();

    if (panel) {
      me.getRoleStore().load();
    }
  }

});