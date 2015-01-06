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
    'NX.util.DateFormat',
    'NX.I18n'
  ],

  title: 'Summary',

  buttons: undefined,

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = [
      {
        xtype: 'fieldset',
        title: NX.I18n.get('ADMIN_SSL_SUMMARY_SUBJECT_SECTION'),
        items: [
          { xtype: 'displayfield', name: 'subjectCommonName', fieldLabel: NX.I18n.get('ADMIN_SSL_SUMMARY_SUBJECT_NAME') },
          { xtype: 'displayfield', name: 'subjectOrganization', fieldLabel: NX.I18n.get('ADMIN_SSL_SUMMARY_SUBJECT_ORGANIZATION') },
          { xtype: 'displayfield', name: 'subjectOrganizationalUnit', fieldLabel: NX.I18n.get('ADMIN_SSL_SUMMARY_SUBJECT_UNIT') }
        ]
      },
      {
        xtype: 'fieldset',
        title: NX.I18n.get('ADMIN_SSL_SUMMARY_ISSUER_SECTION'),
        items: [
          { xtype: 'displayfield', name: 'issuerCommonName', fieldLabel: NX.I18n.get('ADMIN_SSL_SUMMARY_ISSUER_NAME') },
          { xtype: 'displayfield', name: 'issuerOrganization', fieldLabel: NX.I18n.get('ADMIN_SSL_SUMMARY_ISSUER_ORGANIZATION') },
          { xtype: 'displayfield', name: 'issuerOrganizationalUnit', fieldLabel: NX.I18n.get('ADMIN_SSL_SUMMARY_ISSUER_UNIT') }
        ]
      },
      {
        xtype: 'fieldset',
        title: NX.I18n.get('ADMIN_SSL_SUMMARY_CERTIFICATE_SECTION'),
        items: [
          { xtype: 'displayfield', name: 'issuedOn', fieldLabel: NX.I18n.get('ADMIN_SSL_SUMMARY_CERTIFICATE_ISSUED'), renderer: NX.util.DateFormat.timestampRenderer() },
          { xtype: 'displayfield', name: 'expiresOn', fieldLabel: NX.I18n.get('ADMIN_SSL_SUMMARY_CERTIFICATE_VALID'), renderer: NX.util.DateFormat.timestampRenderer() },
          { xtype: 'displayfield', name: 'fingerprint', fieldLabel: NX.I18n.get('ADMIN_SSL_SUMMARY_CERTIFICATE_FINGERPRINT') }
        ]
      }
    ];

    me.callParent();
  }
});
