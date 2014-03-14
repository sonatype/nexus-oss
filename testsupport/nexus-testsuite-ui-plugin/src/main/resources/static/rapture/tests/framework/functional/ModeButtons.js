/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
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
 * Tests mode buttons are present and are working.
 *
 * @since 2.8
 */
StartTest(function (t) {

  var checkMode = function (mode, collapsed, next) {
    var menu = t.cq1('nx-feature-menu');
    t.diag('While in ' + mode + ' mode');
    t.is(menu.title, mode, 'Menu title is "' + mode + '"');
    t.is(menu.collapsed, collapsed ? 'left' : false, 'Menu is ' + (collapsed ? 'collapsed' : 'not collapsed'));
    next();
  };

  t.chain(
      { waitFor: 'stateReceived' },
      { waitFor: 'userToBeLoggedOut' },

      // before login we should have 3 visible and 2 hidden buttons
      { waitFor: 'CQ', args: 'nx-header-dashboard-mode[hidden=false]' },
      { waitFor: 'CQ', args: 'nx-header-search-mode[hidden=false]' },
      { waitFor: 'CQ', args: 'nx-header-browse-mode[hidden=false]' },
      { waitFor: 'CQ', args: 'nx-header-admin-mode[hidden=true]' },
      { waitFor: 'CQ', args: 'nx-header-user-mode[hidden=true]' },

      // after login we should have 5 visible
      { waitFor: 'userToBeLoggedIn' },
      { waitFor: 'CQ', args: 'nx-header-dashboard-mode[hidden=false]' },
      { waitFor: 'CQ', args: 'nx-header-search-mode[hidden=false]' },
      { waitFor: 'CQ', args: 'nx-header-browse-mode[hidden=false]' },
      { waitFor: 'CQ', args: 'nx-header-admin-mode[hidden=false]' },
      { waitFor: 'CQ', args: 'nx-header-user-mode[hidden=false]' },

      { click: '>>nx-header-dashboard-mode' },
      function (next) {
        checkMode('Dashboard', true, next);
      },

      { click: '>>nx-header-search-mode' },
      function (next) {
        checkMode('Search', true, next);
      },

      { click: '>>nx-header-browse-mode' },
      function (next) {
        checkMode('Browse', false, next);
      },

      { click: '>>nx-header-admin-mode' },
      function (next) {
        checkMode('Administration', false, next);
      },

      { click: '>>nx-header-user-mode' },
      function (next) {
        checkMode('User', false, next);
      }
  );

});
