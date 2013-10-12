/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global NX, Ext, Nexus, Sonatype*/

/**
 * Mark Nexus log window.
 *
 * @since 2.7
 */
NX.define('Nexus.logging.app.view.Mark', {
  extend: 'Ext.Window',

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  title: 'Mark log',

  autoShow: true,
  constrain: true,
  resizable: false,
  width: 500,
  border: false,
  cls: 'nx-logging-view-mark',

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    Ext.apply(me, {
      items: [
        {
          xtype: 'panel',
          border: false,
          cls: 'nx-logging-view-mark-description',
          html: '<img src="images/instructions.png"/><span><br/>Mark the log with a unique message for reference.</span>'
        },
        {
          xtype: 'form',
          border: false,
          monitorValid: true,
          items: [
            {
              xtype: 'textfield',
              fieldLabel: 'Message',
              itemCls: 'required-field',
              helpText: "Message to be included in the log",
              name: 'message',
              allowBlank: false,
              anchor: '96%'
            }
          ],

          buttonAlign: 'right',
          buttons: [
            { text: 'Discard', xtype: 'link-button', handler: this.close, scope: me },
            { text: 'Save', formBind: true, id: 'nx-logging-button-mark-save' }
          ]
        }
      ],

      keys: [
        { key: Ext.EventObject.ESC, fn: this.close, scope: me }
      ]
    });

    me.constructor.superclass.initComponent.apply(me, arguments);
  }

}, function () {
  Ext.reg('nx-logging-view-mark', Nexus.logging.app.view.Mark);
});