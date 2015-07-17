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
 * Test browse assets.
 */
StartTest(function(t) {

  t.describe('Browse assets', function(t) {
    t.it('Browse assets feature is available', function(t) {
      t.chain(
          t.openPageAsAdmin('browse/browse/assets'),
          {waitFor: 'CQVisible', args: 'nx-coreui-browseassetfeature'}
      );
    });
    t.it('Repositories are shown and can be clicked', function(t) {
      t.chain(
          {waitFor: 'RowsVisible', args: 'nx-coreui-browse-repository-list'},
          function(next) {
            var grid = t.cq1('>>nx-coreui-browse-repository-list'),
                store = grid.getStore(),
                model = store.findRecord('name', 'browse-test-maven');
            t.click(t.getRow(grid, grid.getView().indexOf(model)));
            next();
          }
      );
    });
    t.it('Assets are shown and can be clicked', function(t) {
      t.chain(
          {waitFor: 'RowsVisible', args: 'nx-coreui-browse-asset-list'},
          function(next) {
            var grid = t.cq1('>>nx-coreui-browse-asset-list'),
                store = grid.getStore(),
                model = store.findRecord('name', '/aopalliance/aopalliance/1.0/aopalliance-1.0.jar');
            t.click(t.getRow(grid, grid.getView().indexOf(model)));
            next();
          }
      );
    });
    t.it('Asset details are shown', function(t) {
      t.chain(
          {waitFor: 'CQVisible', args: 'nx-coreui-component-assetinfo'},
          {waitFor: 'CQVisible', args: 'nx-coreui-component-assetattributes'}
      );
    });
  });
});
