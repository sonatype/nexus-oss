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
 * Tests security realms.
 */
StartTest(function(t) {
  t.describe('Security Realms', function(t) {
    t.it('Realms view is populated', function(t) {
      t.chain(
        t.openPageAsAdmin('admin/security/realms'),
        { waitFor: 'CQVisible', args: 'nx-coreui-security-realm-settings' },
        function(next) {
          var store = t.cq1('>>nx-coreui-security-realm-settings nx-itemselector').toField.getStore();
          t.waitFor(function() {
            return store.count() > 0;
          }, next)
        },
        function(next) {
          var store = t.cq1('>>nx-coreui-security-realm-settings nx-itemselector').toField.getStore();
          var authcIndex = store.find('name', 'Nexus Authenticating Realm');
          var authzIndex = store.find('name', 'Nexus Authorizing Realm');
          t.ok(authcIndex >= 0, 'Active realm store does not contain the authc realm');
          t.ok(authzIndex >= 0, 'Active realm store does not contain the authz realm');
          t.ok(authcIndex < authzIndex, 'Authc realm is not ordered before authz realm');
          next();
        }
      );
    });
    t.it('Can filter realms by name', function(t) {
      t.chain(
        {type: 'LDAP', target: '>>nx-coreui-security-realm-settings nx-searchbox'},
        function(next) {
          var store = t.cq1('>>nx-coreui-security-realm-settings nx-itemselector').fromField.getStore();
          t.waitFor(function() {
            return store.isFiltered();
          }, next)
        },
        function(next) {
          var store = t.cq1('>>nx-coreui-security-realm-settings nx-itemselector').fromField.getStore();
          t.expect(store.count()).toBe(1);
          next();
        }
      )
    });
    t.it('Can add a realm', function(t) {
      t.chain(
        { dblclick: 'nx-coreui-security-realm-settings nx-itemselector multiselectfield[title=Available] boundlist => .x-boundlist-item:contains(LDAP Realm)'},
        {
          waitFor: 'Event', args: ['>>nx-coreui-security-realm-settings nx-settingsform', 'loaded'],
          trigger: {click: '>>nx-coreui-security-realm-settings button[action=save]'}
        },
        function(next) {
          t.waitForAnimations(next);
        },
        function(next) {
          var store = t.cq1('>>nx-coreui-security-realm-settings nx-itemselector').toField.getStore();
          t.ok(store.find('name', 'LDAP Realm') >= 0, 'Active realm store contains the LDAP realm');
          next();
        }
      )
    });
    t.it('Can remove a realm', function(t) {
      t.chain(
        { dblclick: 'nx-coreui-security-realm-settings nx-itemselector multiselectfield[title=Active] boundlist => .x-boundlist-item:contains(LDAP Realm)'},
        {
          waitFor: 'Event', args: ['>>nx-coreui-security-realm-settings nx-settingsform', 'loaded'],
          trigger: {click: '>>nx-coreui-security-realm-settings button[action=save]'}
        },
        function(next) {
          t.waitForAnimations(next);
        },
        function(next) {
          var store = t.cq1('>>nx-coreui-security-realm-settings nx-itemselector').toField.getStore();
          t.ok(store.find('name', 'LDAP Realm') === -1, 'Active realm store does not contain the LDAP realm');
          next();
        }
      )
    });
  });
});
