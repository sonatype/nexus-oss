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
  requires: [
    'NX.I18n'
  ],

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

        title: NX.I18n.get('GLOBAL_SIGN_IN_TITLE'),

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
            anchor: '100%'
          },
          items: [
            {
              name: 'username',
              itemId: 'username',
              emptyText: NX.I18n.get('GLOBAL_SIGN_IN_USERNAME_PLACEHOLDER'),
              allowBlank: false,
              validateOnBlur: false // allow cancel to be clicked w/o validating this to be non-blank
            },
            {
              name: 'password',
              itemId: 'password',
              inputType: 'password',
              emptyText: NX.I18n.get('GLOBAL_SIGN_IN_PASSWORD_PLACEHOLDER'),
              allowBlank: false,
              validateOnBlur: false // allow cancel to be clicked w/o validating this to be non-blank
            },
            {
              xtype: 'checkbox',
              boxLabel: NX.I18n.get('GLOBAL_SIGN_IN_REMEMBER_ME'),
              name: 'rememberMe'
            }
          ],

          buttonAlign: 'left',
          buttons: [
            { text: NX.I18n.get('GLOBAL_SIGN_IN_SUBMIT'), formBind: true, bindToEnter: true, ui: 'nx-primary' },
            { text: NX.I18n.get('GLOBAL_SIGN_IN_CANCEL') }
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
