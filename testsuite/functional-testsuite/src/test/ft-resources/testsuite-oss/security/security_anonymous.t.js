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
 * Tests security anonymous.
 */
StartTest(function(t) {

  var disabled = null;

  t.describe('Anonymous user administration', function(t) {
    t.it('Can navigate to the admin screen', function(t) {
      t.chain(
        t.openPageAsAdmin('admin/security/anonymous'),
        function(next) {
          disabled = !t.cq1('nx-coreui-security-anonymous-settings checkbox[name=enabled]').getValue();
          next();
        },
        // First, enable the anonymous user, if it’s not already enabled
        function(next) {
          if (disabled) {
            t.click('>>nx-coreui-security-anonymous-settings checkbox[name=enabled]', function() {
              t.click('>>nx-coreui-security-anonymous-settings button[action=save]', function() {
                t.waitForEvent('>>nx-coreui-security-anonymous-settings nx-settingsform', 'loaded', function() {
                  t.fieldHasValue('nx-coreui-security-anonymous-settings checkbox[name=enabled]', true,
                    'Anonymous is enabled');
                  next();
                });
              });
            });
          }
          else {
            next();
          }
        }
      )
    });
    t.it('Can disable anonymous user', function(t) {
      t.chain(
        t.click('>>nx-coreui-security-anonymous-settings checkbox[name=enabled]'),
        {
          waitFor: 'Event', args: ['>>nx-coreui-security-anonymous-settings nx-settingsform', 'loaded'],
          trigger: {click: '>>nx-coreui-security-anonymous-settings button[action=save]'}
        },
        function(next) {
          t.fieldHasValue('nx-coreui-security-anonymous-settings checkbox[name=enabled]', false,
              'Anonymous is disabled');
          next();
        }
      )
    });
    t.it('Can enable anonymous user', function(t) {
      t.chain(
        t.click('>>nx-coreui-security-anonymous-settings checkbox[name=enabled]'),
        {
          waitFor: 'Event', args: ['>>nx-coreui-security-anonymous-settings nx-settingsform', 'loaded'],
          trigger: {click: '>>nx-coreui-security-anonymous-settings button[action=save]'}
        },
        function(next) {
          t.fieldHasValue('nx-coreui-security-anonymous-settings checkbox[name=enabled]', true,
            'Anonymous is enabled');
          next();
        }
      )
    });
  });
});

