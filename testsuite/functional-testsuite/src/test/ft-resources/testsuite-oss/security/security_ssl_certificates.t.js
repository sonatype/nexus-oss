/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * Tests SSL Certificate CRUD.
 */
StartTest(function(t) {

  var sslCertificateListCQ = '>>nx-coreui-sslcertificate-list',
      pemLoadCQ = '>>nx-coreui-sslcertificate-add-from-pem button[action=load]',
      pemFieldCQ = '>>nx-coreui-sslcertificate-add-from-pem #pem',
      addToTrustStoreButtonCQ = '>>nx-coreui-sslcertificate-details-form button[action=add][disabled=false]';

  t.describe('SSL Certificates', function(t) {
    t.it('An admin can load a certificate by pasting a PEM', function(t) {

      t.chain(
          t.openPageAsAdmin('admin/security/sslcertificates'),
          {waitForCQVisible: sslCertificateListCQ},
          {click: '>>nx-coreui-sslcertificate-list button[text=Load certificate] => .x-btn-button'},
          {click: '>>menuitem[action=newfrompem]'},
          {waitForCQVisible: pemLoadCQ + '[disabled=true]', desc: 'initially load certificate button is disabled'},
          function(next) {
            var pemField = t.cq1(pemFieldCQ);
            pemField.setValue([
              '-----BEGIN CERTIFICATE-----',
              'MIIEBzCCAu+gAwIBAgIJAKfcFT5TCwrIMA0GCSqGSIb3DQEBCwUAMIGZMQswCQYD',
              'VQQGEwJVUzERMA8GA1UECAwITWFyeWxhbmQxDzANBgNVBAcMBkZ1bHRvbjERMA8G',
              'A1UECgwIU29uYXR5cGUxDDAKBgNVBAsMA0RldjEOMAwGA1UEAwwFbngtZnQxNTAz',
              'BgkqhkiG9w0BCQEWJglzb25hdHlwZS1uZXh1cy1kZXYtZ3JvdXBAc29uYXR5cGUu',
              'Y29tMB4XDTE1MDcwNzA0NTA1NloXDTI1MDcwNDA0NTA1NlowgZkxCzAJBgNVBAYT',
              'AlVTMREwDwYDVQQIDAhNYXJ5bGFuZDEPMA0GA1UEBwwGRnVsdG9uMREwDwYDVQQK',
              'DAhTb25hdHlwZTEMMAoGA1UECwwDRGV2MQ4wDAYDVQQDDAVueC1mdDE1MDMGCSqG',
              'SIb3DQEJARYmCXNvbmF0eXBlLW5leHVzLWRldi1ncm91cEBzb25hdHlwZS5jb20w',
              'ggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQClJk6Tcv8c2jQgsqIMns5c',
              '5X+ZJWzVe5o+TTwDXOtdupK7MUt/vh7Q7AeThytXzjaWZDuZDf6m85OJvRTnmmDI',
              'hMshU/TCek4GMXgcnDfRJFMXqlOd8fArn4lR+rHr6CsKt/P6S8A5B6hx24a6SL+p',
              'a/W2MXInoerk67/syxecdDRew3K56qJ0yonDPNXrhWr5jsu0FERXGVYTreTi++Oz',
              'qV/Of/mV/1IxefoZPsTLmkcrmekiS2jNcoZnwr312gySKl/v/jqAybjPsj7AYBEl',
              '0/yjEnWLFjfTZCCixbQsOLGJBGraQnxpnO+G1OtSAuv7A0kd/MTypMkHNGlNXN09',
              'AgMBAAGjUDBOMB0GA1UdDgQWBBQ3yInDrwNCBKN+Pn41+1M9o2YufDAfBgNVHSME',
              'GDAWgBQ3yInDrwNCBKN+Pn41+1M9o2YufDAMBgNVHRMEBTADAQH/MA0GCSqGSIb3',
              'DQEBCwUAA4IBAQBZa4WqQr74VTysIlZlaHw833mNQw238sgaOSG/RsTew3zR9d8t',
              'jsOGgSCAGM4iTyAtLaEweMkDAPvXEL5B06KwULNDjoFrtTnN9rA2xzGzlDreLZfh',
              'VHznzXgtzAXGF5CeG4q9p7U99m4bYpn9eYBfyDSo8ZKYvA79PNqAkVPfVlaLt46h',
              'zWUSiCU8C0Uilam7UJHLI3I3hNIduKFrOSABDlWO4ZjHQ/XxMBKCPqDadeTTGY3P',
              '/uW7JeOnXxWD4VVAuvHJFOGmaPaMoepQh1W4ueAy2GT1D45+PFeXQ8Yd0OO10c+n',
              'wkOCAIGTk0wExqk/I/ytX9QrXdklowikn5se',
              '-----END CERTIFICATE-----'
            ].join('\n'));
            next()
          },
          {click: pemLoadCQ + '[disabled=false]', desc: 'load certificate button enables once PEM is pasted'},
          function(next) {
            t.waitForAnimations(next);
          },
          {waitForCQVisible: addToTrustStoreButtonCQ, desc: 'button is enabled to add to trust store'},
          {waitForCQVisible: '>>nx-coreui-sslcertificate-details-panel', desc: 'details are loaded for inspection'},
          function(next, detailsPanel) {
            var form = detailsPanel[0].down('form').getForm(),
                values = form.getFieldValues();

            t.expect(values.subjectCommonName).toBe('nx-ft');
            t.expect(values.subjectOrganization).toBe('Sonatype');
            t.expect(values.subjectOrganizationalUnit).toBe('Dev');
            t.expect(values.issuerCommonName).toBe('nx-ft');
            t.expect(values.issuerOrganization).toBe('Sonatype');
            t.expect(values.issuerOrganizationalUnit).toBe('Dev');
            t.expect(values.fingerprint).toBe('A9:28:E6:E1:62:66:5C:69:79:60:3A:A1:69:53:75:06:0A:C0:DE:FA');

            next()
          },
          {click: '>>nx-coreui-sslcertificate-details-form button[action=back]', desc: 'cancel before accepting'},
          {waitForCQVisible: sslCertificateListCQ, desc: 'returning to the list view'},
          function(next){
            t.waitForAnimations(next);
          }
      )
    }, 60000);

    var serverFieldCQ = '>>nx-coreui-sslcertificate-add-from-server #server';

    //disabled as we don't want CI to connect to external servers
    t.xit('An admin can load a certificate from a server', function(t) {

      t.chain(
          {click: '>>nx-coreui-sslcertificate-list button[text=Load certificate] => .x-btn-button'},
          {click: '>>menuitem[action=newfromserver]'},
          {waitForCQVisible: serverFieldCQ},
          {type: 'https://repo1.maven.org/maven2/', target: serverFieldCQ},
          {click: '>>nx-coreui-sslcertificate-add-from-server button[action=load]'},
          {waitForCQVisible: '>>nx-coreui-sslcertificate-details-panel', desc: 'details are loaded for inspection'},
          function(next, detailsPanel) {
            var form = detailsPanel[0].down('form').getForm(),
                values = form.getFieldValues();

            t.expect(values.subjectCommonName).toBe('repo1.maven.org');
            t.expect(values.subjectOrganization).toBe('Sonatype, Inc');

            next()
          },
          function(next){
            t.waitForAnimations(next);
          },
          {click: '>>nx-coreui-sslcertificate-details-form button[action=back]', desc: 'cancel before accepting'},
          {waitForCQVisible: sslCertificateListCQ, desc: 'returning to the list view'}
      )
    });
  });
});
