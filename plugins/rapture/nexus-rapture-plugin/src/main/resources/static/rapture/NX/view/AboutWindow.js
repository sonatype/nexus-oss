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
 * About window.
 *
 * @since 3.0
 */
Ext.define('NX.view.AboutWindow', {
  extend: 'Ext.window.Window',
  alias: 'widget.nx-aboutwindow',
  requires: [
    'NX.Icons',
    'NX.State'
  ],

  layout: {
    type: 'vbox',
    align: 'stretch',
    pack: 'center'
  },

  autoShow: true,
  modal: true,
  constrain: true,
  width: 640,
  height: 480,

  title: 'About Sonatype Nexus',

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    //
    // TODO: Make more eclipse-like, with details about product/edition/version
    // TODO: ... add license, etc.
    //

    me.items = [
      {
        xtype: 'image',
        cls: 'nx-icon-nexus-x100'
      },
      {
        xtype: 'label',
        html: '<code>' +
            'Nexus <em>' + NX.State.getValue('status')['edition'] + '</em> ' +
            NX.State.getValue('status')['version'] +
            '</code>',

        style: {
          'color': '#000000',
          'font-size': '20px',
          'font-weight': 'bold',
          'text-align': 'center',
          'padding': '20px'
        }
      }
    ];

    me.buttons = [
      { text: 'Close', action: 'close', ui: 'primary', handler: function() { me.close(); } }
    ];

    me.callParent(arguments);
  }

});
