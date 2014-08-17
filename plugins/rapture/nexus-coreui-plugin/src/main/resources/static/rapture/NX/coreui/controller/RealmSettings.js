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
 * Security realms controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.RealmSettings', {
  extend: 'Ext.app.Controller',
  mixins: {
    logAware: 'NX.LogAware'
  },

  views: [
    'security.RealmSettings'
  ],

  stores: [
    'RealmType'
  ],

  refs: [
    {
      ref: 'panel',
      selector: 'nx-coreui-security-realm-settings'
    }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getFeaturesController().registerFeature({
      mode: 'admin',
      path: '/Security/Realms',
      view: { xtype: 'nx-coreui-security-realm-settings' },
      description: 'Manage security realm configuration',
      iconConfig: {
        file: 'shield.png',
        variants: ['x16', 'x32']
      },
      visible: function () {
        return NX.Permissions.check('nexus:settings', 'read');
      }
    });

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.loadRealmTypes
        }
      },
      component: {
        'nx-coreui-security-realm-settings': {
          beforerender: me.loadRealmTypes
        }
      }
    });
  },

  loadRealmTypes: function () {
    var me = this,
        panel = me.getPanel();

    if (panel) {
      me.getRealmTypeStore().load();
    }
  }

});