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

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.load
        }
      },
      component: {
        'nx-coreui-support-metrics': {
          afterrender: me.load
        }
      }
    });
  },

  /**
  * Load system information panel.
  *
  * @private
  */
  load: function () {
    var me = this,
        metrics = me.getMetrics();

    if (metrics) {
      metrics.getEl().mask('Loading...');

      NX.direct.coreui_Metrics.memory(function (response)
      {
        if (!Ext.isDefined(response) || !response.success) {
          return;
        }
        var memory = response.data;

        // update total memory data chart
        metrics.setTotalData([
          { name: 'Used', data: memory.totalUsed },
          { name: 'Available', data: memory.totalMax - memory.totalUsed }
        ]);

        //update heap memory data chart
        metrics.setHeapData([
          { name: 'Used', data: memory.heapUsed },
          { name: 'Available', data: memory.heapMax - memory.heapUsed }
        ]);

        metrics.getEl().unmask();
      });
    }
  }

});