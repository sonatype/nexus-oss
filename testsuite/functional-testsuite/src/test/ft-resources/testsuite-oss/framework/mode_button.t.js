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
 * Tests mode buttons are present and are working.
 */
StartTest(function(t) {

  var checkMode = function(mode, collapsed, next, t) {
    var menu = t.cq1('nx-feature-menu');
    t.diag('While in ' + mode + ' mode');
    t.is(menu.title, mode, 'Menu title is "' + mode + '"');
    t.is(menu.collapsed, collapsed ? 'left' : false, 'Menu is ' + (collapsed ? 'collapsed' : 'not collapsed'));
    next();
  };

  t.describe('The "mode" button should navigate between modes of the application', function(t) {
    
    t.it('Before signin we should have 1 visible and 2 hidden buttons', function(t) {
      t.chain(
          {waitFor: 'userToBeSignedOut'},
          {waitFor: 'CQ', args: 'nx-header-browse-mode ^ nx-header-mode[hidden=false]', desc: 'browse mode visible'},
          {waitFor: 'CQ', args: 'nx-header-admin-mode ^ nx-header-mode[hidden=true]', desc: 'admin mode hidden'},
          {waitFor: 'CQ', args: 'nx-header-user-mode ^ nx-header-mode[hidden=true]', desc: 'user mode hidden'}
      )
    });

    t.it('After signin we should have 3 buttons visible', function(t) {
      t.chain(
          t.openPageAsAdmin('browse/welcome'),
          {waitFor: 'CQ', args: 'nx-header-browse-mode ^ nx-header-mode[hidden=false]', desc: 'browse mode visible'},
          {waitFor: 'CQ', args: 'nx-header-admin-mode ^ nx-header-mode[hidden=false]', desc: 'admin mode visible'},
          {waitFor: 'CQ', args: 'nx-header-user-mode ^ nx-header-mode[hidden=false]', desc: 'user mode visible'}
      )
    });

    t.it('We can enter "browse" mode', function(t) {
      t.chain(
          {click: '>>nx-header-browse-mode'},
          function(next) {
            checkMode('Browse', false, next, t);
          }
      );
    });

    t.it('We can enter "admin" mode', function(t) {
      t.chain(
          {click: '>>nx-header-admin-mode'},
          function(next) {
            checkMode('Administration', false, next, t);
          }
      );
    });

    t.it('We can enter "user" mode', function(t) {
      t.chain(
          {click: '>>nx-header-user-mode'},
          function(next) {
            checkMode('User', false, next, t);
          }
      );
    });

  });
});
