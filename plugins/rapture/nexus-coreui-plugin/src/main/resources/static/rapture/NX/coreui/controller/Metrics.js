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
 * Metrics controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Metrics', {
  extend: 'Ext.app.Controller',
  mixins: {
    logAware: 'NX.LogAware'
  },

  stores: [
    'MetricsMemory'
  ],
  models: [
    'MetricsMemory'
  ],
  views: [
    'support.Metrics'
  ],
  refs: [
    {
      ref: 'metrics',
      selector: 'nx-coreui-support-metrics'
    }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getFeaturesController().registerFeature({
      mode: 'admin',
      path: '/Support/Metrics',
      view: { xtype: 'nx-coreui-support-metrics' },
      description: 'Provides server metrics',
      iconConfig: {
        file: 'chart_pie.png',
        variants: ['x16', 'x32']
      },
      visible: function () {
        return NX.Permissions.check('nexus:metrics-endpoints');
      }
    });

    /*
    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.load
        }
      },
      component: {
        'nx-coreui-support-sysinfo': {
          afterrender: me.load
        },
        'nx-coreui-support-sysinfo button[action=download]': {
          'click': me.download
        },
        'nx-coreui-support-sysinfo button[action=print]': {
          'click': me.print
        }
      }
    });
    */
  },

  ///**
  // * Load system information panel.
  // *
  // * @private
  // */
  //load: function () {
  //  var me = this,
  //      panel = me.getSysInfo();
  //
  //  if (panel) {
  //    me.logDebug('Refreshing sysinfo');
  //
  //    panel.getEl().mask('Loading...');
  //    NX.direct.atlas_SystemInformation.read(function (response) {
  //      panel.getEl().unmask();
  //      if (Ext.isDefined(response) && response.success) {
  //        panel.setInfo(response.data);
  //      }
  //    });
  //  }
  //}

});