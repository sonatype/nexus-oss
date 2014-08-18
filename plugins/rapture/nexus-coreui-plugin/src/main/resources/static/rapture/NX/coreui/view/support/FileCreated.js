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
 * File created window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.support.FileCreated', {
  extend: 'Ext.window.Window',
  alias: 'widget.nx-coreui-support-filecreated',

  /**
   * @cfg Icon to show (img)
   */
  fileIcon: undefined,

  /**
   * @cfg Type of file shown
   */
  fileType: undefined,

  layout: 'fit',
  autoShow: true,
  constrain: true,
  resizable: false,
  width: 630,
  border: false,
  modal: true,

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    Ext.apply(me, {
      title: me.title || me.fileType + ' Created',
      items: [
        {
          xtype: 'form',
          bodyPadding: 10,
          defaults: {
            anchor: '100%'
          },
          items: [
            {
              xtype: 'panel',
              layout: 'hbox',
              style: {
                marginBottom: '10px'
              },
              // TODO Style
              items: [
                { xtype: 'component', html: me.fileIcon },
                { xtype: 'component', html: me.fileType + ' has been created.' +
                    '<br/>You can reference this file on the filesystem or download the file from your browser.',
                  margin: '0 0 0 5'
                }
              ]
            },
            {
              // TODO Style
              xtype: 'textfield',
              name: 'name',
              fieldLabel: 'Name',
              helpText: me.fileType + ' file name',
              readOnly: true
            },
            {
              // TODO Style
              xtype: 'textfield',
              name: 'size',
              fieldLabel: 'Size',
              helpText: 'Size of ' + me.fileType + ' file in bytes',  // FIXME: Would like to render in bytes/kilobytes/megabytes
              readOnly: true
            },
            {
              xtype: 'textfield',
              name: 'file',
              fieldLabel: 'Path',
              helpText: me.fileType + ' file location',
              readOnly: true,
              selectOnFocus: true
            },
            {
              xtype: 'hidden',
              name: 'truncated'
            }
          ],

          buttonAlign: 'left',
          buttons: [
            {
              text: 'Download',
              action: 'download',
              formBind: true,
              bindToEnter: true,
              ui: 'primary',
              glyph: 'xf023@FontAwesome' /* fa-lock */
            },
            {
              text: 'Cancel',
              handler: me.close,
              scope: me
            }
          ]
        }
      ]
    });

    me.callParent(arguments);
  },

  /**
   * Set form values.
   *
   * @public
   */
  setValues: function (values) {
    this.down('form').getForm().setValues(values);
  },

  /**
   * Get form values.
   *
   * @public
   */
  getValues: function () {
    return this.down('form').getForm().getValues();
  }

});