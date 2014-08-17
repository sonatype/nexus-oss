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
 * Ssl Certificate detail panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ssl.SslCertificateDetails', {
  extend: 'NX.view.SettingsForm',
  alias: 'widget.nx-coreui-sslcertificate-details',
  requires: [
    'NX.util.DateFormat'
  ],

  buttons: undefined,

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = [
      {
        xtype: 'fieldset',
        title: 'Subject',
        items: [
          { xtype: 'displayfield', name: 'subjectCommonName', fieldLabel: 'Common Name' },
          { xtype: 'displayfield', name: 'subjectOrganization', fieldLabel: 'Organisation' },
          { xtype: 'displayfield', name: 'subjectOrganizationalUnit', fieldLabel: 'Unit' }
        ]
      },
      {
        xtype: 'fieldset',
        title: 'Issuer',
        items: [
          { xtype: 'displayfield', name: 'issuerCommonName', fieldLabel: 'Common Name' },
          { xtype: 'displayfield', name: 'issuerOrganization', fieldLabel: 'Organisation' },
          { xtype: 'displayfield', name: 'issuerOrganizationalUnit', fieldLabel: 'Unit' }
        ]
      },
      {
        xtype: 'fieldset',
        title: 'Certificate',
        items: [
          { xtype: 'displayfield', name: 'issuedOn', fieldLabel: 'Issued On', renderer: NX.util.DateFormat.timestampRenderer() },
          { xtype: 'displayfield', name: 'expiresOn', fieldLabel: 'Valid Until', renderer: NX.util.DateFormat.timestampRenderer() },
          { xtype: 'displayfield', name: 'fingerprint', fieldLabel: 'Fingerprint' }
        ]
      }
    ];

    me.callParent();
  }
});
