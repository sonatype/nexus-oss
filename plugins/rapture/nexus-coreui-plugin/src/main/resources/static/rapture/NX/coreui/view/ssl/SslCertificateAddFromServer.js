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
 * Add Ssl Certificate from Server window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ssl.SslCertificateAddFromServer', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-sslcertificate-add-from-server',
  requires: [
    'NX.Icons'
  ],

  title: 'Load certificate from server',
  defaultFocus: 'server',

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = {
      xtype: 'nx-settingsform',
      settingsFormSubmitOnEnter: true,

      items: [
        {
          xtype: 'panel',
          margin: '0 0 10 0',
          layout: {
            type: 'hbox'
          },
          items: [
            { xtype: 'container', html: NX.Icons.img('sslcertificate-add-by-server', 'x32') },
            { xtype: 'container', html: 'Please enter a hostname, hostname:port or a URL to fetch a SSL certificate from.' }
          ]
        },
        {
          xtype: 'textfield',
          anchor: '100%',
          name: 'server',
          itemId: 'server'
        }
      ],

      buttons: [
        { text: 'Load Certificate', action: 'load', formBind: true, bindToEnter: true, ui: 'primary' },
        { text: 'Cancel', handler: function () {
          this.up('window').close();
        }}
      ]
    };

    me.callParent();
  }
});
