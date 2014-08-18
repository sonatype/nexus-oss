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
 * HTTP System Settings controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.HttpSettings', {
  extend: 'Ext.app.Controller',

  views: [
    'system.HttpSettings'
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getFeaturesController().registerFeature({
      mode: 'admin',
      path: '/System/HTTP',
      description: 'Manage outbound HTTP/HTTPS configuration',
      view: { xtype: 'nx-coreui-system-http-settings' },
      iconConfig: {
        file: 'lorry.png',
        variants: ['x16', 'x32']
      },
      visible: function () {
        return NX.Permissions.check('nexus:settings', 'read');
      }
    });

    me.listen({
      component: {
        'nx-coreui-system-http-settings checkbox[name=httpEnabled]': {
          change: me.onHttpEnabledChanged
        }
      }
    });
  },

  /**
   * @private
   * Enable HTTPS proxy settings only when HTTP proxy settings are enabled.
   */
  onHttpEnabledChanged: function (httpEnabled) {
    var httpsProxy = httpEnabled.up('form').down('#httpsProxy');

    if (!httpEnabled.getValue()) {
      httpsProxy.collapse();
      httpsProxy.disable();
    }
    else if (httpsProxy.isDisabled()) {
      httpsProxy.enable();
    }
  }

});