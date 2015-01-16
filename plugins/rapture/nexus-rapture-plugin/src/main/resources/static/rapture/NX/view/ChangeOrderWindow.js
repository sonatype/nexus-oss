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
/*global Ext*/

/**
 * Abstract change order window.
 *
 * @since 3.0
 */
Ext.define('NX.view.ChangeOrderWindow', {
  extend: 'Ext.window.Window',
  alias: 'widget.nx-changeorderwindow',
  requires: [
    'NX.ext.form.field.ItemOrderer',
    'NX.I18n'
  ],

  layout: 'fit',
  autoShow: true,
  modal: true,
  constrain: true,
  width: 460,

  displayField: 'name',
  valueField: 'id',

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = {
      xtype: 'form',
      bodyPadding: 10,
      items: {
        xtype: 'nx-itemorderer',
        store: me.store,
        displayField: me.displayField,
        valueField: me.valueField,
        delimiter: null,
        height: 400,
        width: 400
      },
      buttonAlign: 'left',
      buttons: [
        { text: NX.I18n.get('GLOBAL_DIALOG_ORDER_SUBMIT_BUTTON'), action: 'save', formBind: true, ui: 'nx-primary' },
        { text: NX.I18n.get('GLOBAL_DIALOG_ORDER_CANCEL_BUTTON'), handler: function () {
          this.up('window').close();
        }}
      ]
    };

    me.callParent(arguments);
  }

});
