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
 * Tests support zip.
 */
StartTest(function(t) {
  var expectedCheckboxes = [
    'systemInformation',
    'threadDump',
    'configuration',
    'security',
    'log',
    'metrics',
    'jmx',
    'limitFileSizes',
    'limitZipSize'
  ];

  Ext.merge(t.controller('Message').windowOptions, {
    slideInDuration: 200,
    slideBackDuration: 200,
    autoCloseDelay: 300,
  });

  t.describe('Support Zip', function(t) {
    t.it('System Zip Smoke Test', function(t) {
      t.chain(
        t.openPageAsAdmin('admin/support/supportzip'),
        { waitFor: 'CQNotFound', args: 'nx-message-notification' },
        { waitFor: 'CQVisible', args: 'nx-coreui-support-supportzip' },
        function(next) {
          // check for the existence of each expected checkbox
          expectedCheckboxes.forEach(function(c){
            t.waitForCQVisible('nx-coreui-support-supportzip checkbox[name=' + c + ']', function(){});
          });
          // get an array of checkbox names
          var checkboxNames = t.cq('nx-coreui-support-supportzip checkbox').map(function(c){ return c.getName(); });
          // check for unexpected checkboxes
          t.isDeeply(Ext.Array.difference(checkboxNames, expectedCheckboxes), [], "Check for unexpected checkboxes");
          next();
        },
        { waitFor: 'CQVisible', args: 'nx-coreui-support-supportzip button[action=save]' },
        { click: '>>nx-coreui-support-supportzip button[action=save]' },
        { waitFor: 'CQVisible', args: 'nx-coreui-support-supportzipcreated' },
        { waitFor: 'CQNotFound', args: 'nx-message-notification' },
        { waitFor: 'CQVisible', args: 'nx-coreui-support-supportzipcreated button[action=download]' },
        { waitFor: 'CQVisible', args: 'nx-coreui-support-supportzipcreated button[text=Cancel]' },
        { click: '>>nx-coreui-support-supportzipcreated button[text=Cancel]' }
      );
    });
  });
});
