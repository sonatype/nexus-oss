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
 * Modal dialogs styles.
 *
 * @since 3.0
 */
Ext.define('NX.view.dev.styles.Modals', {
  extend: 'NX.view.dev.styles.StyleSection',

  title: 'Modals',

  layout: {
    type: 'hbox',
    defaultMargins: {top: 0, right: 4, bottom: 0, left: 0}
  },

  /**
   * @protected
   */
  initComponent: function () {
    var me = this;

    me.items = [
      {
        xtype: 'window',

        title: 'Sign In',

        hidden: false,
        collapsible: false,
        floating: false,
        closable: false,
        draggable: false,
        resizable: false,
        width: 320,
        cls: 'fixed-modal',

        items: {
          xtype: 'form',
          bodyPadding: 10,
          defaultType: 'textfield',
          defaults: {
            labelAlign: 'left',
            labelWidth: 100,
            anchor: '100%'
          },
          items: [
            {
              name: 'username',
              itemId: 'username',
              fieldLabel: 'Username',
              emptyText: 'enter your username',
              allowBlank: false,
              validateOnBlur: false // allow cancel to be clicked w/o validating this to be non-blank
            },
            {
              name: 'password',
              itemId: 'password',
              fieldLabel: 'Password',
              inputType: 'password',
              emptyText: 'enter your password',
              allowBlank: false,
              validateOnBlur: false // allow cancel to be clicked w/o validating this to be non-blank
            },
            {
              xtype: 'checkbox',
              fieldLabel: 'Remember me',
              name: 'remember'
            }
          ],

          buttonAlign: 'left',
          buttons: [
            { text: 'Sign In', formBind: true, bindToEnter: true, ui: 'nx-primary' },
            { text: 'Cancel', scope: me }
          ]
        }
      },
      {
        xtype: 'window',

        title: 'Session',

        hidden: false,
        collapsible: false,
        floating: false,
        closable: false,
        draggable: false,
        resizable: false,
        width: 320,
        cls: 'fixed-modal',

        items: [
          {
            xtype: 'label',
            id: 'expire',
            text: 'Session is about to expire',
            style: {
              'color': 'red',
              'font-size': '20px',
              'margin': '10px'
            }
          }
        ],
        buttons: [
          { text: 'Cancel' }
        ]
      }
    ];

    me.callParent();
  }
});
