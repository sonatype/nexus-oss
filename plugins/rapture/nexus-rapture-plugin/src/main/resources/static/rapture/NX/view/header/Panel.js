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
 * Header panel.
 *
 * @since 3.0
 */
Ext.define('NX.view.header.Panel', {
  extend: 'Ext.container.Container',
  alias: 'widget.nx-header-panel',
  requires: [
    'NX.State'
  ],

  layout: {
    type: 'vbox',
    align: 'stretch',
    pack: 'start'
  },

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = [
      { xtype: 'nx-header-branding', hidden: true },
      {
        xtype: 'toolbar',

        // TODO replace style with UI
        style: {
          backgroundColor: '#000000'
        },
        anchor: '100%',

        defaults: {
          scale: 'medium'
        },

        items: [
          { xtype: 'nx-header-logo' },
          {
            xtype: 'label',
            text: 'Sonatype Nexus',
            style: {
              'color': '#FFFFFF',
              'font-size': '20px'
            }
          },
          {
            xtype: 'label',
            text: NX.State.getValue('status')['edition'] + ' ' +
                NX.State.getValue('status')['version'],
            style: {
              'color': '#CCCCCC',
              'font-size': '10px'
            }
          }
        ]
      }
    ];

    me.callParent();
  }
});
