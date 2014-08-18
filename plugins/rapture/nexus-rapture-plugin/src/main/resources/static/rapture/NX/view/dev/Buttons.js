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
/*global Ext*/

/**
 * Shows examples of buttons in various styles.
 *
 * @since 3.0
 */
Ext.define('NX.view.dev.Buttons', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-dev-buttons',

  title: 'Buttons',

  layout: {
    type: 'vbox',
    padding: 4,
    defaultMargins: {top: 0, right: 0, bottom: 4, left: 0}
  },

  defaults: {
    width: '100%'
  },

  /**
   * @protected
   */
  initComponent: function () {
    var me = this, i = 0;

    // Add a panel for each button theme and render some example button states
    me.items = [];
    Ext.each(['default', 'primary', 'danger', 'warning', 'success', 'plain'], function (ui) {
      // pick a different glyph for each style of button
      var glyph = 'xf00' + i + '@FontAwesome';
      i = i + 1;

      me.items.push({
        xtype: 'container',
        layout: {
          type: 'hbox',
          padding: 4,
          defaultMargins: {top: 0, right: 4, bottom: 0, left: 0}
        },
        defaults: {
          width: 100
        },
        items: [
          { xtype: 'label', text: "ui: '" + ui + "'"},
          { xtype: 'button', text: 'normal', glyph: glyph, ui: ui },
          { xtype: 'button', text: 'disabled', glyph: glyph,  ui: ui, disabled: true },
          {
            xtype: 'button',
            text: 'menu',
            glyph: glyph,
            ui: ui,
            menu: [
              { text: 'Hello' },
              '-',
              { text: 'Goodbye' }
            ]
          }
        ]
      });
    });

    me.callParent();
  }
});