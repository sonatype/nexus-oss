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
 * Support ZIP created window.
 *
 * @since 2.7
 */
NX.define('Nexus.atlas.view.SupportZipCreated', {
  extend: 'Ext.Window',

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  requires: [
    'Nexus.atlas.Icons'
  ],

  xtype: 'nx-atlas-view-supportzip-created',
  cls: 'nx-atlas-view-supportzip-created',

  title: 'Support ZIP Created',

  autoShow: true,
  constrain: true,
  resizable: false,
  width: 500,
  border: false,
  modal: true,

  /**
   * @override
   */
  initComponent: function() {
    var me = this,
        icons = Nexus.atlas.Icons;

    Ext.apply(me, {
      items: [
        {
          xtype: 'panel',
          border: false,
          cls: 'nx-atlas-view-supportzip-created-description',
          html: icons.get('zip').variant('x32').img + '<div>Support ZIP has been created.' +
              '<br/><br/>You can reference this file on the filesystem or download the file from your browser.</div>'
        },
        {
          xtype: 'form',
          itemId: 'form',
          border: false,
          monitorValid: true,
          layoutConfig: {
            labelSeparator: '',
            labelWidth: 40
          },
          items: [
            {
              xtype: 'textfield',
              fieldLabel: 'File',
              helpText: 'Support ZIP file path',
              name: 'file',
              readOnly: true,
              selectOnFocus: true,
              anchor: '96%'
            }
          ],

          buttonAlign: 'right',
          buttons: [
            { text: 'Close', xtype: 'link-button', handler: me.close, scope: me },
            { text: 'Download', formBind: true, id: 'nx-atlas-button-supportzip-download' }
          ]
        }
      ],

      keys: [
        {
          // Download on ENTER
          key: Ext.EventObject.ENTER,
          scope: me,
          fn: function() {
            var btn = Ext.getCmp('nx-atlas-button-supportzip-download');
            btn.fireEvent('click', btn);
          }
        },
        {
          // Close on ESC
          key: Ext.EventObject.ESC,
          scope: me,
          fn: me.close
        }
      ]
    });

    me.constructor.superclass.initComponent.apply(me, arguments);
  },

  /**
   * Set form values.
   *
   * @public
   */
  setValues: function(values) {
    this.down('form').getForm().setValues(values);
  }
});