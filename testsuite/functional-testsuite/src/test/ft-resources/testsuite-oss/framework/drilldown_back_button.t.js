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
 * Test the ability to drill into a feature, then go back by clicking the feature menu
 */
StartTest(function(t) {

  t.describe('When in a drilldown feature, go back to the first panel', function(t) {
    t.it('Go back using the feature menu', function(t) {
      t.chain(
          t.openPageAsAdmin('admin/repository/repositories'),
          {waitForRowsVisible: 'nx-coreui-repository-list'},
          function(next) {
            var grid = t.cq1('nx-coreui-repository-list'),
                testRepoRow = t.getRow(grid, grid.getStore().find('name', 'maven-public'));
            t.click(testRepoRow, next);
          },
          function(next) {
            t.waitForAnimations(next);
          },
          {waitFor: 'CQ', args: ">>nx-feature-content button[text=Repositories]"},
          {click : "nx-feature-menu => .x-tree-node-text:contains('Repositories')"},
          {waitForRowsVisible: 'nx-coreui-repository-list'}
      )
    });
  });

});
