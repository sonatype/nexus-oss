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
 * Ssl Certificate grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ssl.SslCertificateList', {
  extend: 'Ext.grid.Panel',
  alias: 'widget.nx-coreui-sslcertificate-list',
  requires: [
    'NX.Icons'
  ],

  store: 'SslCertificate',

  columns: [
    {
      xtype: 'nx-iconcolumn',
      width: 36,
      iconVariant: 'x16',
      iconName: function () {
        return 'sslcertificate-default';
      }
    },
    { header: 'Name', dataIndex: 'subjectCommonName', flex: 1 },
    { header: 'Issued To', dataIndex: 'issuerCommonName', flex: 1 },
    { header: 'Issued By', dataIndex: 'issuerCommonName', flex: 1 },
    { header: 'Fingerprint', dataIndex: 'fingerprint', flex: 1 }
  ],

  emptyText: 'No SSL certificates',

  plugins: [
    { ptype: 'gridfilterbox', emptyText: 'No SSL certificate matched criteria "$filter"' }
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.tbar = [
      {
        xtype: 'button',
        text: 'New',
        glyph: 'xf055@FontAwesome' /* fa-plus-circle */,
        action: 'new',
        disabled: true,
        menu: [
          {
            text: 'Load from server',
            action: 'newfromserver',
            iconCls: NX.Icons.cls('sslcertificate-add-by-server', 'x16')
          },
          {
            text: 'Paste PEM',
            action: 'newfrompem',
            iconCls: NX.Icons.cls('sslcertificate-add-by-pem', 'x16')
          }
        ]
      },
      {
        xtype: 'button',
        text: 'Delete',
        glyph: 'xf056@FontAwesome' /* fa-minus-circle */,
        action: 'delete',
        disabled: true
      }
    ];

    me.callParent();
  }
});
