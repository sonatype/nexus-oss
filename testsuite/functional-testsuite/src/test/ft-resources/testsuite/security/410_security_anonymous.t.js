/*
 * Copyright (c) 2008-2015 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * Tests security anonymous.
 */
StartTest(function(t) {

  t.describe('Anonymous user administration', function(t) {
    t.it('Can navigate to the admin screen', function(t) {
      t.chain(
          t.openPageAsAdmin('admin/security/anonymous'),
          {
            waitFor: 'Event', args: ['>>nx-coreui-security-anonymous-settings nx-settingsform', 'loaded']
            , trigger: {click: '>>nx-header-refresh'}
          },

          // bring settings to a known state (enabled checked)
          function(next) {
            var enabled = t.cq1('nx-coreui-security-anonymous-settings checkbox[name=enabled]');
            if (!enabled.getValue()) {
              t.click('>>nx-coreui-security-anonymous-settings checkbox[name=enabled]', next);
            }
            else {
              next();
            }
          },
          {
            waitFor: 'Event', args: ['>>nx-coreui-security-anonymous-settings nx-settingsform', 'loaded'],
            trigger: {click: '>>nx-coreui-security-anonymous-settings button[action=save]'}
          },
          function(next) {
            t.waitForAnimations(next);
          },
          function(next) {
            t.fieldHasValue('nx-coreui-security-anonymous-settings combo[name=realmName]', 'NexusAuthorizingRealm',
                'The only available realm is selected');
            next();
          }
      );
    }, 300000);
    t.it('Can disable anonymous user', function(t) {
      t.chain(
          {click: '>>nx-coreui-security-anonymous-settings checkbox[name=enabled]'},
          // and we should be able to save the form
          {
            waitFor: 'Event', args: ['>>nx-coreui-security-anonymous-settings nx-settingsform', 'loaded'],
            trigger: {click: '>>nx-coreui-security-anonymous-settings button[action=save]'}
          },
          function(next) {
            t.fieldHasValue('nx-coreui-security-anonymous-settings checkbox[name=enabled]', false,
                'Anonymous is disabled');
            next();
          }
      );
    }, 300000);
    t.it('Can enable anonymous user', function(t) {
      t.chain(
          // enable anonymous user
          {click: '>>nx-coreui-security-anonymous-settings checkbox[name=enabled]'},
          // and we should be able to save the form
          {
            waitFor: 'Event', args: ['>>nx-coreui-security-anonymous-settings nx-settingsform', 'loaded'],
            trigger: {click: '>>nx-coreui-security-anonymous-settings button[action=save]'}
          },
          function(next) {
            t.fieldHasValue('nx-coreui-security-anonymous-settings checkbox[name=enabled]', true,
                'Anonymous is enabled');
            next();
          }
      );
    }, 300000);
  }, 300000);
});

