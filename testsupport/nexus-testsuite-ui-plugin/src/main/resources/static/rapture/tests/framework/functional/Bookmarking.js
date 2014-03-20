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
/**
 * Tests bookmarking (navigate to divers parts and navigating back).
 *
 * @since 2.8
 */
StartTest(function (t) {

  var goBack = function () {
        t.getExt().History.back();
        t.diag('history:back');
      },
      selected0Id, selected2Id;

  t.chain(
      { waitFor: 'stateReceived' },
      { waitFor: 'userToBeLoggedIn' },

      { click: '>>nx-header-browse-mode' },
      { waitFor: 'bookmark', args: 'browse/components' },

      { click: '>>nx-header-search-mode' },
      { waitFor: 'CQVisible', args: 'nx-search' },
      { waitFor: 'bookmark', args: 'search/search'},

      { click: '>>nx-header-admin-mode' },
      function (next) {
        t.navigateTo('admin/repository/repositories');
        next();
      },
      { waitFor: 'rowsVisible', args: 'nx-coreui-repository-list' },
      { waitFor: 'bookmark', args: 'admin/repository/repositories' },

      function (next) {
        var grid = t.cq1('nx-coreui-repository-list');

        grid.getSelectionModel().select(0);
        selected0Id = grid.getSelectionModel().getSelection()[0].getId();
        t.diag('Selected repository: ' + selected0Id);
        next();
      },
      function (next) {
        t.waitForBookmark('admin/repository/repositories:' + selected0Id + ':summary', next);
      },

      function (next) {
        var grid = t.cq1('nx-coreui-repository-list');

        grid.getSelectionModel().select(2);
        selected2Id = grid.getSelectionModel().getSelection()[0].getId();
        t.diag('Selected repository: ' + selected2Id);
        next();
      },
      function (next) {
        t.waitForBookmark('admin/repository/repositories:' + selected2Id + ':summary', next);
      },

      function (next) {
        t.navigateTo('admin/repository/targets');
        next();
      },
      { waitFor: 'rowsVisible', args: 'nx-coreui-repositorytarget-list' },
      { waitFor: 'bookmark', args: 'admin/repository/targets' },

      t.do(goBack),
      { waitFor: 'rowsVisible', args: 'nx-coreui-repository-list' },
      function (next) {
        t.waitForBookmark('admin/repository/repositories:' + selected2Id + ':summary', next);
      },
      function (next) {
        var grid = t.cq1('nx-coreui-repository-list'),
            selected;

        selected = grid.getSelectionModel().getSelection();
        t.is(selected[0].getId(), selected2Id, 'Repository "' + selected2Id + '" is selected');
        next();
      },

      t.do(goBack),
      function (next) {
        t.waitForBookmark('admin/repository/repositories:' + selected0Id + ':summary', next);
      },
      function (next) {
        var grid = t.cq1('nx-coreui-repository-list'),
            selected;

        selected = grid.getSelectionModel().getSelection();
        t.is(selected[0].getId(), selected0Id, 'Repository "' + selected0Id + '" is selected');
        next();
      },

      t.do(goBack),
      { waitFor: 'rowsVisible', args: 'nx-coreui-repository-list' },
      { waitFor: 'bookmark', args: 'admin/repository/repositories' },

      t.do(goBack),
      { waitFor: 'bookmark', args: 'admin/repository' },

      t.do(goBack),
      { waitFor: 'CQVisible', args: 'nx-search' },
      { waitFor: 'bookmark', args: 'search/search' },

      t.do(goBack),
      { waitFor: 'bookmark', args: 'browse/components' }
  );

});
