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
 * Dialog to display a NuGet API Key details.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.nuget.NuGetApiKeyDetails', {
  extend: 'Ext.window.Window',
  alias: 'widget.nx-coreui-nuget-apikeydetails',
  requires: [
    'NX.Icons',
    'NX.Messages'
  ],

  title: 'NuGet API Key',
  autoShow: true,
  modal: true,
  constrain: true,
  width: 640,
  resizable: false,

  /**
   * @cfg {String} repository id
   */
  repositoryId: undefined,

  /**
   * @cfg {String} NuGet API Key
   */
  apiKey: undefined,

  /**
   * @override
   */
  initComponent: function() {
    var me = this;

    me.items = {
      xtype: 'form',
      bodyPadding: 10,
      items: [
        {
          xtype: 'panel',
          layout: 'hbox',
          items: [
            { xtype: 'component', html: NX.Icons.img('nuget-default', 'x32') },
            { xtype: 'label', margin: '0 0 0 5',
              html: 'NuGet API Key enables pushing packages using NuGet.exe.<br/>' +
                  '<span style="font-weight: bold;">Keep this key secret!</span>'
            }
          ]
        },
        {
          xtype: 'label',
          html: '<p>Your NuGet API Key is:</p>'
        },
        {
          xtype: 'textfield',
          value: me.apiKey,
          readOnly: true,
          selectOnFocus: true,
          fieldStyle: {
            padding: '2px',
            'font-family': 'monospace'
          }
        },
        {
          xtype: 'label',
          html: '<p>You can register this key with the following command:</p>'
        },
        {
          xtype: 'textfield',
          value: 'nuget setapikey ' + me.apiKey + ' -source ' + me.repositoryId,
          readOnly: true,
          selectOnFocus: true,
          fieldStyle: {
            padding: '2px',
            'font-family': 'monospace'
          }
        },
        {
          xtype: 'label',
          html: '<p>This window will automatically close after one minute.</p>'
        }
      ],
      buttonAlign: 'left',
      buttons: [
        { text: 'Cancel', handler: function() {
          this.up('window').close();
        }}
      ]
    };

    me.callParent(arguments);

    // Automatically close the window
    Ext.defer(function() {
      if (me.isVisible()) { // ignore if already closed
        NX.Messages.add({ text: 'Automatically closing NuGet API Key details due to timeout' });
        me.close();
      }
    }, 1 * 60 * 1000); // 1 minute
  }

});
