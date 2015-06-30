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
StartTest(function(t) {

  var // generate a unique pattern
      name = 'Test-' + new Date().getTime(),
  // a reusable function to wait for grid to load
      waitForStoreToLoad = {
        waitFor: 'storesToLoad', args: function() {
          return t.cq1('nx-coreui-blobstore-list').getStore();
        }
      };

  t.describe('BlobStore administration', function(t) {
    t.it('BlobStore CRUD', function(t) {
      t.it('Can create a new BlobStore', function(t) {
        t.chain(
            t.openPageAsAdmin('admin/repository/blobstores'),
            // and wait for new button to be enabled
            {waitFor: 'CQ', args: 'nx-coreui-blobstore-list button[action=new][disabled=false]'},

            // press the "New" button
            {click: '>>nx-coreui-blobstore-list button[action=new]'},
            function(next) {
              t.waitForAnimations(next);
            },
            // select type
            {click: '>>nx-coreui-blobstore-add combobox[name=type] => .x-form-text'},
            {click: '[itemId=type].getPicker() => .x-boundlist-item:contains(File)'},
            // type a name
            {type: name, target: '>>nx-coreui-blobstore-add [itemId=name]'},
            {
              type: Ext.encode({file: {path: '/tmp/blobstore'}}), //TODO - property to get temp dir consistently on all platforms?
              target: '>>nx-coreui-blobstore-add textarea[name=attributes]'
            },
            // we are ready to go, so press the "Add" button and wait for grid to refresh
            Ext.apply(Ext.clone(waitForStoreToLoad),
                {trigger: {click: '>>nx-coreui-blobstore-add button[action=add]'}}
            ),
            // verify that the blobstore was added
            function(next) {
              var grid = t.cq1('nx-coreui-blobstore-list'),
                  store = grid.getStore(),
                  model = store.findRecord('name', name);

              // we can find the created record in store
              t.ok(model, 'BlobStore "' + name + '" exists');
              // and values are the one we set
              t.is(model.get('type'), 'File');
              next();
            }
        )
      }, 300000);
      t.it('Can delete an existing BlobStore', function(t) {
        t.chain(
            function(next) {
              var grid = t.cq1('nx-coreui-blobstore-list'),
                  store = grid.getStore(),
                  model = store.findRecord('name', name);
              t.click(t.getRow(grid, grid.getView().indexOf(model)));
              next();
            },
            function(next) {
              // Wait for the animation to finish
              t.waitForAnimations(function() {
                // FIXME: appears this will return more components than expected and produces warnings:
                //t.click('>>nx-coreui-blobstore-feature button[action=delete]');
                // FIXME: but this works, but is very obtuse:
                t.click('>>[itemId=feature-content] [itemId=nx-drilldown] nx-drilldown-item [itemId=browse1] nx-drilldown-details nx-actions button[action=delete]');
                next();
              });
            },
            // and wait for confirmation box
            Ext.apply(Ext.clone(waitForStoreToLoad),
                {trigger: {click: '>>button[text=Yes]'}}
            ),
            // then check that deleted task is no longer available in grid
            function(next) {
              var grid = t.cq1('nx-coreui-blobstore-list'),
                  store = grid.getStore(),
                  model = store.findRecord('name', name);

              t.ok(!model, 'Blobstore "' + name + '" has been removed');
              next();
            }
        );
      }, 300000);
    }, 300000);
  }, 300000);
});
