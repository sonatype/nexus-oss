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
 * Form styles.
 *
 * @since 3.0
 */
Ext.define('NX.view.dev.styles.Forms', {
  extend: 'NX.view.dev.styles.StyleSection',

  title: 'Forms',
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
        xtype: 'form',
        items: [
          { xtype: 'textfield', value: 'Text Input', allowBlank: false, fieldLabel: '[Label]', helpText: '[Optional description text]', width: 200 },
          { xtype: 'textarea', value: 'Text Input', allowBlank: false, fieldLabel: '[Label]', helpText: '[Optional description text]', width: 200 },
          { xtype: 'checkbox', boxLabel: 'Checkbox', checked: true, fieldLabel: null, helpText: null },
          { xtype: 'radio', boxLabel: 'Radio Button', checked: true, fieldLabel: null, helpText: null }
        ],
        buttons: [
          { text: 'Submit', ui: 'primary' },
          { text: 'Discard' }
        ]
      }
    ];

    me.callParent();
  }
});