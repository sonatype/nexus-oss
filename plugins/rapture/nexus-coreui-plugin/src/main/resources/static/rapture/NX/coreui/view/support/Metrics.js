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
 * Metrics panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.support.Metrics', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-support-metrics',
  requires: [
    'Ext.chart.Chart',
    'Ext.data.ArrayStore'
  ],

  autoScroll: true,
  layout: {
    type: 'table',
    columns: 2
  },

  tbar: [
    {
      xtype: 'button',
      text: 'Download',
      tooltip: 'Download metrics data',
      glyph: 'xf019@FontAwesome' /* fa-download */,
      action: 'download'
    },
    '-',
    {
      xtype: 'button',
      text: 'Thread Dump',
      tooltip: 'Download thread dump',
      glyph: 'xf019@FontAwesome' /* fa-download */,
      action: 'threads'
    }
  ],

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
              colorSet: ['#F49D10', '#ddd'],

              tips: {
                trackMouse: true,
                renderer: function (storeItem, item) {
                  this.setTitle('Memory used: ' + storeItem.get('value') + '%');
                }
              }
            }
          ]
        }
      ]
    },

    {
      xtype: 'panel',
      title: 'Memory Distribution',
      frame: true,
      height: 240,
      width: 300,
      layout: 'fit',

      items: [
        {
          xtype: 'chart',
          itemId: 'memoryDist',
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
      title: 'Thread States',
      frame: true,
      height: 340,
      width: 420,

      layout: 'fit',
      colspan: 2,

      items: [
        {
          xtype: 'chart',
          itemId: 'threadStates',
          animate: false,
          insetPadding: 20,
          theme: 'Base',

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
                  // name: count
                  this.setTitle(storeItem.get('name') + ': ' + storeItem.get('data'));
                }
              }
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
  setMemoryDistData: function (data) {
    this.down('panel #memoryDist').getStore().loadData(data);
  },

  /**
   * @public
   */
  setThreadStatesData: function (data) {
    this.down('panel #threadStates').getStore().loadData(data);
  }
});
