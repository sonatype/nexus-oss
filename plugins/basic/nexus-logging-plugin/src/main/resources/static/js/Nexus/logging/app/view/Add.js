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
 * Add new logger window.
 *
 * @since 2.7
 */
NX.define('Nexus.logging.app.view.Add', {
  extend: 'Ext.Window',

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  title: 'Add logger',

  autoShow: true,
  constrain: true,
  resizable: false,
  width: 500,
  border: false,
  cls: 'nx-logging-view-add',

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
          cls: 'nx-logging-view-add-description',
          html: '<img src="images/instructions.png"/><span><br/>Add a logger with a specific level.</span>'
        },
        {
          xtype: 'form',
          border: false,
          monitorValid: true,
          items: [
            {
              xtype: 'textfield',
              fieldLabel: 'Logger',
              itemCls: 'required-field',
              helpText: 'Enter a logger name',
              name: 'name',
              allowBlank: false,
              anchor: '96%'
            },
            {
              xtype: 'nx-logging-combo-logger-level',
              fieldLabel: 'Level',
              itemCls: 'required-field',
              //helpText: "Select logger level",
              name: 'level',
              value: 'INFO',
              width: 80
            }
          ],

          buttonAlign: 'right',
          buttons: [
            { text: 'Discard', xtype: 'link-button', handler: this.close, scope: me },
            { text: 'Save', formBind: true, id: 'nx-logging-button-add-save' }
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
  Ext.reg('nx-logging-view-add', Nexus.logging.app.view.Add);
});