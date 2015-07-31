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
 * Test indexing & basic searching of indexed components.
 */
StartTest(function(t) {

  var assetListToken = null;
  var assetDetailToken = null;

  t.describe('Basic Indexing and Searching', function(t) {
    t.it('Keyword search finds maven components', function(t) {
      t.chain(
          t.openPageAsAdmin('browse/search'),
          {waitForCQVisible: '>>nx-coreui-searchfeature field[criteriaId=keyword]'},
          { action : 'click', target : '>>nx-coreui-searchfeature field[criteriaId=keyword]' },
          function (next, searchBox) {
            searchBox.focus();
            t.type(searchBox.inputEl, 'name:aopalliance', next);
          },
          {waitForRowsVisible: 'nx-coreui-search-result-list'},
          function(next) {
            var grid = t.cq1('nx-coreui-search-result-list'),
                store = grid.getStore(),
                model = store.getAt(0);

            t.expect(model).toBeDefined();
            t.expect(model.get('format')).toEqual('maven2');
            t.expect(model.get('name')).toBe(t.anyStringLike('aopalliance'));
            next();
          }
      )
    }, 60000);
    t.it('Can view asset details', function(t) {
      t.chain(
        function(next) {
          var grid = t.cq1('nx-coreui-search-result-list'),
              store = grid.getStore(),
              model = store.getAt(0),
              row = t.getRow(grid, grid.getView().indexOf(model));

          t.click(row, next);
        },
        // and wait for the asset list to load
        function(next) {
          t.waitForAnimations(next);
        },
        // and save the asset list URL for future use
        function(next) {
          assetListToken = t.getBookmark();
          next();
        },
        function(next) {
          var grid = t.cq1('nx-coreui-component-asset-list'),
              store = grid.getStore(),
              model = store.getAt(0),
              row = t.getRow(grid, grid.getView().indexOf(model));

          t.click(row, next);
        },
        // and wait for the asset details to load
        function(next) {
          t.waitForAnimations(next);
        },
        {waitFor: 'CQ', args: 'nx-coreui-component-assetcontainer'},
        // and save the asset URL for future use
        function(next) {
          assetDetailToken = t.getBookmark();
          next();
        }
      )
    });
    t.it('Navigate to search feature via the URL', function(t) {
      t.chain(
        t.navigateTo('browse/search'),
        {waitFor: 'CQVisible', args: 'nx-coreui-search-result-list'}
      );
    });
    t.it('Navigate to asset list via the URL', function(t) {
      t.chain(
        t.navigateTo(assetListToken),
        {waitFor: 'CQVisible', args: 'nx-coreui-component-asset-list'}
      );
    });
    t.it('Navigate to asset details via the URL', function(t) {
      t.chain(
        t.navigateTo(assetDetailToken),
        {waitFor: 'CQVisible', args: 'nx-coreui-component-assetcontainer'}
      );
    });
    t.it('Maven search finds maven components', function(t) {
      t.chain(
          t.navigateTo('browse/search/maven'),
          {waitFor: 'CQ', args: 'nx-coreui-search-result-list'},
          {type: 'aopalliance', target: '>>nx-coreui-searchfeature field[criteriaId=attributes.maven2.artifactId]'},
          t.waitForStore('SearchResult'),
          function(next) {
            var grid = t.cq1('nx-coreui-search-result-list'),
                store = grid.getStore(),
                model = store.getAt(0);

            t.expect(model).toBeDefined();
            t.expect(model.get('format')).toEqual('maven2');
            t.expect(model.get('name')).toBe(t.anyStringLike('aopalliance'));
            next();
          }
      )
    });
    t.it('Keyword search finds nuget components', function(t) {
      t.chain(
          t.navigateTo('browse/search'),
          {waitFor: 'CQ', args: 'nx-coreui-search-result-list'},
          {type: 'name:SONATYPE.TEST', target: '>>nx-coreui-searchfeature field[criteriaId=keyword]'},
          t.waitForStore('SearchResult'),
          function(next) {
            var grid = t.cq1('nx-coreui-search-result-list'),
                store = grid.getStore(),
                model = store.getAt(0);

            t.expect(model).toBeDefined();
            t.expect(model.get('format')).toEqual('nuget');
            t.expect(model.get('name')).toBe(t.anyStringLike('SONATYPE.TEST'));
            next();
          }
      )
    });
    t.it('Nuget search finds nuget components', function(t) {
      t.chain(
          t.navigateTo('browse/search/nuget'),
          {waitFor: 'CQ', args: 'nx-coreui-search-result-list'},
          {type: 'SONATYPE.TEST', target: '>>nx-coreui-searchfeature field[criteriaId=attributes.nuget.id]'},
          t.waitForStore('SearchResult'),
          function(next) {
            var grid = t.cq1('nx-coreui-search-result-list'),
                store = grid.getStore(),
                model = store.getAt(0);

            t.expect(model).toBeDefined();
            t.expect(model.get('format')).toEqual('nuget');
            t.expect(model.get('name')).toBe(t.anyStringLike('SONATYPE.TEST'));
            next();
          }
      )
    });
    t.it('Keyword search finds raw components', function(t) {
      t.chain(
          t.navigateTo('browse/search'),
          {waitFor: 'CQ', args: 'nx-coreui-search-result-list'},
          {type: 'name:alphabet.txt', target: '>>nx-coreui-searchfeature field[criteriaId=keyword]'},
          t.waitForStore('SearchResult'),
          function(next) {
            var grid = t.cq1('nx-coreui-search-result-list'),
                store = grid.getStore(),
                model = store.getAt(0);

            t.expect(model).toBeDefined();
            t.expect(model.get('format')).toEqual('raw');
            t.expect(model.get('name')).toBe(t.anyStringLike('alphabet.txt'));
            next();
          }
      )
    });
    t.it('Raw search finds raw components', function(t) {
      t.chain(
          t.navigateTo('browse/search/raw'),
          {waitFor: 'CQ', args: 'nx-coreui-search-result-list'},
          {type: 'alphabet.txt', target: '>>nx-coreui-searchfeature field[criteriaId=attributes.raw.path.tree]'},
          t.waitForStore('SearchResult'),
          function(next) {
            var grid = t.cq1('nx-coreui-search-result-list'),
                store = grid.getStore(),
                model = store.getAt(0);

            t.expect(model).toBeDefined();
            t.expect(model.get('format')).toEqual('raw');
            t.expect(model.get('name')).toBe(t.anyStringLike('alphabet.txt'));
            next();
          }
      )
    });
  });
});
