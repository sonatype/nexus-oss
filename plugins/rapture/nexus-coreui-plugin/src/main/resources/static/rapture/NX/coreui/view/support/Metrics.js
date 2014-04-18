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

  bodyPadding: 10,

  items: [
    {
      xtype: 'panel',

      title: 'Memory Usage',
      frame: true,
      height: 300,
      width: 400,
      layout: 'fit',

      items: [
        {
          xtype: 'chart',
          animate: false,
          insetPadding: 20,
          theme: 'Green',
          legend: {
            position: 'right'
          },

          store: Ext.create('Ext.data.ArrayStore', {
            fields: ['name', 'data']
          }),
          series: [
            {
              type: 'pie',
              angleField: 'data',
              showInLegend: true
            }
          ]
        }
      ]
    }
  ]

});
