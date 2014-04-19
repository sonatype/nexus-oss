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
 * Metrics panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.support.Metrics', {
  extend: 'Ext.Panel',
  alias: 'widget.nx-coreui-support-metrics',

  requires: [
    'Ext.chart.Chart'
  ],

  layout: {
    type: 'table',
    columns: 2
  },

  bodyPadding: 20,
  defaults: {
    style: {
      margin: '0px 20px 20px 0px'
    }
  },

  items: [
    {
      xtype: 'panel',
      title: 'Memory Usage',
      frame: true,
      height: 240,
      width: 300,
      layout: 'fit',

      items: [
        {
          xtype: 'chart',
          itemId: 'memoryUsage',

          animate: false,
          insetPadding: 40,

          store: Ext.create('Ext.data.ArrayStore', {
            fields: ['value']
          }),

          axes: [
            {
              type: 'gauge',
              position: 'gauge',
              minimum: 0,
              maximum: 100,
              steps: 10
            }
          ],

          series: [
            {
              type: 'gauge',
              field: 'value',
              donut: 30,
              colorSet: ['#F49D10', '#ddd']
            }
          ]
        }
      ]
    },

    {
      xtype: 'panel',
      title: 'Heap Usage',
      frame: true,
      height: 240,
      width: 300,
      layout: 'fit',

      items: [
        {
          xtype: 'chart',
          itemId: 'heapUsage',
          animate: false,
          insetPadding: 20,
          theme: 'Green',

          store: Ext.create('Ext.data.ArrayStore', {
            fields: ['name', 'data']
          }),

          series: [
            {
              type: 'pie',
              angleField: 'data',
              showInLegend: true,

              tips: {
                trackMouse: true,
                renderer: function (storeItem, item) {
                  this.setTitle(storeItem.get('name') + ': ' + storeItem.get('data') + ' bytes');
                }
              }
            }
          ],

          legend: {
            position: 'right'
          }
        }
      ]
    },

    {
      xtype: 'panel',
      title: 'Threads',
      frame: true,
      height: 340,
      width: 400,

      layout: 'fit',
      colspan: 2,

      items: [
        {
          xtype: 'chart',
          itemId: 'threads',
          animate: false,
          insetPadding: 20,
          //theme: 'Blue',

          store: Ext.create('Ext.data.ArrayStore', {
            fields: ['name', 'data']
          }),

          series: [
            {
              type: 'pie',
              angleField: 'data',
              showInLegend: true
            }
          ],

          legend: {
            position: 'right'
          }
        }
      ]
    }
  ],

  /**
   * @public
   */
  setTotalData: function (data) {
    this.down('panel #memoryUsage').getStore().loadData(data);
  },

  /**
   * @public
   */
  setHeapData: function (data) {
    this.down('panel #heapUsage').getStore().loadData(data);
  },

  /**
   * @public
   */
  setThreadsData: function (data) {
    this.down('panel #threads').getStore().loadData(data);
  }

});
