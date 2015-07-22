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
 * Tests system bundles view.
 */
StartTest(function(t) {
  t.describe('System Bundles', function(t) {
    t.it('Bundles view is populated', function(t) {
      t.chain(
          t.openPageAsAdmin('admin/system/bundles'),
          {waitForRowsVisible: 'nx-coreui-system-bundlelist'}
      );
    });
    t.it('Can filter bundles by name', function(t) {
      t.chain(
        { type: 'nexus-bootstrap', target: '>>nx-coreui-system-bundlelist nx-searchbox' },
        function(next) {
          var store = t.cq1('nx-coreui-system-bundlelist').getStore();
          t.waitFor(function() {
            return store.isFiltered();
          }, next)
        },
        function(next) {
          var store = t.cq1('nx-coreui-system-bundlelist').getStore();
          t.expect(store.count()).toBe(1);
          next();
        }
      )
    });
    t.it('Can drill-down by bundle', function(t) {
      t.chain(
        function(next) {
          var grid = t.cq1('nx-coreui-system-bundlelist'),
              name = 'org.sonatype.nexus:nexus-bootstrap',
              bundleRow = t.getRow(grid, grid.getStore().find('name', name));
          t.click(bundleRow, next);
        },
        { waitFor: 'selector', args: '.nx-info-entry-name:contains(Bundle-Activator)' }
      )
    });
  });
});
