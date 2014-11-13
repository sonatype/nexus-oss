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
 * Button styles.
 *
 * @since 3.0
 */
Ext.define('NX.view.dev.styles.Buttons', {
  extend: 'NX.view.dev.styles.StyleSection',
  requires: [
    'Ext.XTemplate'
  ],

  title: 'Buttons',
  layout: {
    type: 'vbox',
    defaultMargins: {top: 4, right: 0, bottom: 0, left: 0}
  },

  /**
   * @protected
   */
  initComponent: function () {
    var me = this;

    var colorSampleTpl = Ext.create('Ext.XTemplate',
        '<table>',
        '<tpl for=".">',
        '<tr>',
        '<td><div class="color {.}"></div></td>',
        '<td><div style="padding: 0 10px 0 0">$color-{.}</div></td>',
        '</tr>',
        '</tpl>',
        '</table>'
    );

    function button(ui, text, disabled, pressed, menu) {
      var button = { xtype: 'button', text: text, ui: ui, margin: "0 10 10 0" };

      // Initialize optional button parameters
      if (disabled) {
        button['disabled'] = true;
      }
      if (pressed) {
        button['pressed'] = true;
        button['enableToggle'] = true;
      }
      if (menu) {
        button['menu'] = [
          { text: 'First' },
          '-',
          { text: 'Second' }
        ];
      }
      else {
        button['glyph'] = 'xf036@FontAwesome';
      }

      return button;
    }

    function group(/** items... */) {
      var items = Array.prototype.slice.call(arguments, 0);
      return {
        xtype: 'container',
        layout: {
          type: 'hbox',
          defaultMargins: {top: 0, right: 4, bottom: 0, left: 0}
        },
        items: items
      }
    }

    me.items = [
      // Default buttons
      group(
          { xtype: 'label', text: 'Default', cls: 'section-header' },
          button('default', 'Default', false, false, false),
          button('default', 'Default', true, false, false),
          button('default', 'Default', false, false, true),
          me.html(colorSampleTpl.apply(['white', 'light-gainsboro', 'light-gray', 'silver', 'suva-gray', 'gray']))
      ),

      // Plain buttons
      group(
          { xtype: 'label', text: 'Plain', cls: 'section-header' },
          button('plain', 'Plain', false, false, false),
          button('plain', 'Plain', true, false, false),
          button('plain', 'Plain', false, false, true),
          me.html(colorSampleTpl.apply(['white', 'light-gainsboro', 'light-gray', 'silver', 'suva-gray', 'gray']))
      ),

      // Primary buttons
      group(
          { xtype: 'label', text: 'Primary', cls: 'section-header' },
          button('primary', 'Primary', false, false, false),
          button('primary', 'Primary', true, false, false),
          button('primary', 'Primary', false, false, true),
          me.html(colorSampleTpl.apply([
            'denim', 'light-cobalt', 'dark-denim', 'smalt', 'dark-cerulean', 'prussian-blue'
          ]))
      ),

      // Danger buttons
      group(
          { xtype: 'label', text: 'Danger', cls: 'section-header' },
          button('danger', 'Danger', false, false, false),
          button('danger', 'Danger', true, false, false),
          button('danger', 'Danger', false, false, true),
          me.html(colorSampleTpl.apply(['light-cerise', 'brick-red', 'old-rose', 'fire-brick', 'shiraz', 'falu-red']))
      ),

      // Warning buttons
      group(
          { xtype: 'label', text: 'Warning', cls: 'section-header' },
          button('warning', 'Warning', false, false, false),
          button('warning', 'Warning', true, false, false),
          button('warning', 'Warning', false, false, true),
          me.html(colorSampleTpl.apply(['sea-buckthorn', 'tahiti-gold', 'zest', 'rich-gold', 'afghan-tan', 'russet']))
      ),

      // Success buttons
      group(
          { xtype: 'label', text: 'Success', cls: 'section-header' },
          button('success', 'Success', false, false, false),
          button('success', 'Success', true, false, false),
          button('success', 'Success', false, false, true),
          me.html(colorSampleTpl.apply([
            'elf-green', 'dark-pigment-green', 'salem', 'jewel', 'fun-green', 'dark-jewel'
          ]))
      )
    ];

    me.callParent();
  }
});