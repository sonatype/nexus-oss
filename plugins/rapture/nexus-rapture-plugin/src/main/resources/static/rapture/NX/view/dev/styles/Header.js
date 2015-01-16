/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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
 * Header styles.
 *
 * @since 3.0
 */
Ext.define('NX.view.dev.styles.Header', {
  extend: 'NX.view.dev.styles.StyleSection',
  requires: [
    'Ext.XTemplate',
    'NX.State'
  ],

  title: 'Header',
  layout: {
    type: 'hbox',
    defaultMargins: {top: 0, right: 4, bottom: 0, left: 0}
  },

  /**
   * @protected
   */
  initComponent: function () {
    var me = this,
        items = [],
        logo = [];

    logo.push({ xtype: 'label', text: 'Sonatype Nexus', cls: 'nx-header-productname' });

    // HACK remove this
    var logoOnly = false;

    if (!logoOnly) {
      logo.push(
          {
            xtype: 'label',
            text: NX.State.getEdition() + ' ' + NX.State.getVersion(),
            cls: 'nx-header-productversion',
            style: {
              'padding-left': '8px'
            }
          }
      )
    }

    items.push({ xtype: 'nx-header-logo' });
    items.push({ xtype: 'container', items: logo });

    if (!logoOnly) {
      items.push(
          ' ', ' ', // 2x pad
          {
            xtype: 'button',
            ui: 'nx-header',
            cls: 'nx-modebutton',
            toggleGroup: 'examplemode',
            title: 'Browse',
            tooltip: 'Browse server contents',
            glyph: 'xf1b2@FontAwesome' /* fa-cube */
          },
          {
            xtype: 'button',
            ui: 'nx-header',
            cls: 'nx-modebutton',
            toggleGroup: 'examplemode',
            title: 'Administration',
            tooltip: 'Server administration and configuration',
            glyph: 'xf013@FontAwesome' /* fa-gear */
          },
          ' ',
          {
            xtype: 'nx-searchbox',
            cls: 'nx-quicksearch',
            width: 200,
            emptyText: 'Search…',
            inputAttrTpl: "data-qtip='Quick component keyword search'" // field tooltip
          },
          '->',
          //{
          //  xtype: 'button',
          //  ui: 'nx-header',
          //  glyph: 'xf0f3@FontAwesome',
          //  tooltip: 'Toggle messages display'
          //},
          {
            xtype: 'button',
            ui: 'nx-header',
            tooltip: 'Refresh current view and data',
            glyph: 'xf021@FontAwesome' // fa-refresh
          },
          {
            xtype: 'button',
            ui: 'nx-header',
            text: 'Sign In',
            tooltip: 'Have an account?',
            glyph: 'xf090@FontAwesome'
          },
          {
            xtype: 'nx-header-mode',
            ui: 'nx-header',
            hidden: true,
            mode: 'user',
            title: 'User',
            text: 'User',
            tooltip: 'User profile and options',
            glyph: 'xf007@FontAwesome',
            autoHide: false,
            collapseMenu: false
          },
          {
            xtype: 'button',
            ui: 'nx-header',
            tooltip: "Sign out",
            hidden: true,
            glyph: 'xf08b@FontAwesome'
          },
          {
            xtype: 'button',
            ui: 'nx-header',
            tooltip: 'Help',
            glyph: 'xf059@FontAwesome', // fa-question-circle
            arrowCls: '', // hide the menu button arrow
            menu: [
              {
                text: 'Menu item 1'
              },
              '-',
              {
                text: 'Menu item 2'
              },
              {
                text: 'Menu item 3'
              }
            ]
          }
      );
    }

    me.items = [
      {
        xtype: 'toolbar',

        // set height to ensure we have uniform size and not depend on what is in the toolbar
        height: 40,

        style: {
          backgroundColor: '#000000'
        },
        anchor: '100%',
        padding: "0 0 0 16px",

        defaults: {
          scale: 'medium'
        },

        items: items
      }
    ];

    me.callParent();
  }
});
