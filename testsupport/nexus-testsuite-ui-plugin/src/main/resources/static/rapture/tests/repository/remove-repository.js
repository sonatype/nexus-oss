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
startTest({

  waitForAppReady: false

}, function (t) {
  var numberOfRecs;
  t.chain(
      function (next) {
        t.waitForMs(1500, function () {
          next();
        });
      },
      { waitFor: 'CQVisible', args: 'nx-featurebrowser' },
      function (next, comps) {
        var fb = comps[0];

        fb.setActiveTab(2);

        next();
      },
      { waitFor: 'rowsVisible', args: 'nx-coreui-repository-list' },
      function (next) {
        var repoGrid = t.cq1('nx-coreui-repository-list'),
            deleteButton = t.cq1('nx-coreui-repository-list button[action=delete]');

        t.ok(deleteButton.isDisabled(), 'Delete button is disabled');
        repoGrid.getSelectionModel().select(0);
        t.ok(!deleteButton.isDisabled(), 'Delete button is enabled');
        numberOfRecs = repoGrid.getStore().getCount();
        next();
      },
      { click: '>>nx-coreui-repository-list button[action=delete]' },
      { waitFor: 'CQVisible', args: 'messagebox' },
      { click: '>>button[text=No]' },
      function (next) {
        var repoGrid = t.cq1('nx-coreui-repository-list');

        t.is(repoGrid.getStore().getCount(), numberOfRecs, "Selected record was not deleted");

        next();
      },
      { click: '>>nx-coreui-repository-list button[action=delete]' },
      { waitFor: 'CQVisible', args: 'messagebox' },
      { click: '>>button[text=Yes]' },
      function (next) {
        t.waitForMs(1500, function () {
          next();
        });
      },
      function (next) {
        var repoGrid = t.cq1('nx-coreui-repository-list');

        t.isLess(repoGrid.getStore().getCount(), numberOfRecs, "Selected record was deleted");

        next();
      }
  );
});
