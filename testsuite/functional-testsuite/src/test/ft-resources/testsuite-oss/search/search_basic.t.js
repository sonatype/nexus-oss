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

  var waitForStoreToLoad = {
    waitFor: 'storesToLoad', args: function() {
      return t.cq1('nx-coreui-search-result-list').getStore();
    }
  };

  t.describe('Basic Indexing and Searching', function(t) {
    t.it('Keyword search finds maven components', function(t) {
      t.chain(
          t.openPageAsAdmin('browse/search'),
          {waitFor: 'CQ', args: 'nx-coreui-search-result-list'},
          {type: 'name:aopalliance', target: '>>nx-coreui-searchfeature field[criteriaId=keyword]'},
          Ext.clone(waitForStoreToLoad),
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
    }, 300000);
    t.it('Maven search finds maven components', function(t) {
      t.chain(
          t.openPageAsAdmin('browse/search/maven'),
          {waitFor: 'CQ', args: 'nx-coreui-search-result-list'},
          {type: 'aopalliance', target: '>>nx-coreui-searchfeature field[criteriaId=attributes.maven2.artifactId]'},
          Ext.clone(waitForStoreToLoad),
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
    }, 300000);
    t.it('Keyword search finds nuget components', function(t) {
      t.chain(
          t.openPageAsAdmin('browse/search'),
          {waitFor: 'CQ', args: 'nx-coreui-search-result-list'},
          {type: 'name:SONATYPE.TEST', target: '>>nx-coreui-searchfeature field[criteriaId=keyword]'},
          Ext.clone(waitForStoreToLoad),
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
    }, 300000);
    t.it('Nuget search finds nuget components', function(t) {
      t.chain(
          t.openPageAsAdmin('browse/search/nuget'),
          {waitFor: 'CQ', args: 'nx-coreui-search-result-list'},
          {type: 'SONATYPE.TEST', target: '>>nx-coreui-searchfeature field[criteriaId=attributes.nuget.id]'},
          Ext.clone(waitForStoreToLoad),
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
    }, 300000);
    t.it('Keyword search finds raw components', function(t) {
      t.chain(
          t.openPageAsAdmin('browse/search'),
          {waitFor: 'CQ', args: 'nx-coreui-search-result-list'},
          {type: 'name:alphabet.txt', target: '>>nx-coreui-searchfeature field[criteriaId=keyword]'},
          Ext.clone(waitForStoreToLoad),
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
    }, 300000);
    t.it('Raw search finds raw components', function(t) {
      t.chain(
          t.openPageAsAdmin('browse/search/raw'),
          {waitFor: 'CQ', args: 'nx-coreui-search-result-list'},
          {type: 'alphabet.txt', target: '>>nx-coreui-searchfeature field[criteriaId=attributes.raw.path.tree]'},
          Ext.clone(waitForStoreToLoad),
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
    }, 300000);
  }, 300000);
});
