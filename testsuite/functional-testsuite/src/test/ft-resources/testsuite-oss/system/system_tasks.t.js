/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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
 * Tests system tasks CRUD.
 */
StartTest(function(t) {

  var // generate a unique pattern
      name = 'Test-' + new Date().getTime(),
  // a reusable function to wait for grid to load
      waitForStoreToLoad = {
        waitFor: 'storesToLoad', args: function() {
          return t.cq1('nx-coreui-task-list').getStore();
        }
      };

  t.describe('Task administration', function(t) {
    t.it('Task CRUD testing', function(t) {
      t.chain(
          t.openPageAsAdmin('admin/system/tasks'),
          // and wait for new button to be enabled
          {waitFor: 'CQ', args: 'nx-coreui-task-list button[action=new][disabled=false]'},

          // press the "New" button
          {click: '>>nx-coreui-task-list button[action=new]'},
          // and wait for add dialog
          function(next) {
            t.waitForAnimations(next);
          },
          // select type
          function(next) {
            var grid = t.cq1('nx-coreui-task-selecttype'),
                purgeTimelineRow = t.getRow(grid, grid.getStore().find('name', 'Purge Timeline'));
            t.click(purgeTimelineRow, next);
          },
          function(next) {
            t.waitForAnimations(next);
          },
          // type a name
          {type: name, target: '>>nx-coreui-task-add field[name=name]'},
          {type: '0', target: '>>nx-coreui-task-add field[name=property_purgeOlderThan]'},
          // set schedule
          //{ click: '>>tab[text=Schedule]' },
          {click: 'combobox[name=schedule] => .x-form-text'},
          {click: '.x-boundlist-item:contains(Manual)'},
          // we are ready to go, so press the "Add" button and wait for grid to refresh
          Ext.apply(Ext.clone(waitForStoreToLoad),
              {trigger: {click: '>>nx-coreui-task-add button[action=add]'}}
          ),
          // verify that the task was added
          function(next) {
            var grid = t.cq1('nx-coreui-task-list'),
                store = grid.getStore(),
                model = store.findRecord('name', name);

            // we can find the created record in store
            t.ok(model, 'Task "' + name + '" exists');
            // and values are the one we set
            if (model) {
              t.is(model.get('typeId'), 'PurgeTimelineTask');
              t.is(model.get('properties')['purgeOlderThan'], '0');
              t.is(model.get('schedule'), 'manual');
            }

            // view the detail panel
            t.click(t.getRow(grid, grid.getView().indexOf(model)));

            next();
          },

          // update and check selected model to have correct values
          function(next) {
            t.waitForAnimations(function() {
              t.click('>>nx-drilldown-details tab[text=Settings]');
              next();
            });
          },
          {type: '[UP]', target: '>>nx-coreui-task-settings field[name=property_purgeOlderThan]'},
          // save changes and wait for grid to refresh
          Ext.apply(Ext.clone(waitForStoreToLoad),
              {trigger: {click: '>>nx-coreui-task-settings button[action=save]'}}
          ),
          // and verify that:
          function(next) {
            var grid = t.cq1('nx-coreui-task-list'),
                store = grid.getStore(),
                model = store.findRecord('name', name),
                selected = grid.getSelectionModel().getSelection()[0];

            // we can find the updated record in store
            t.ok(model, 'Task "' + name + '" exists');
            // and values are the one we set
            if (model) {
              t.is(model.get('typeId'), 'PurgeTimelineTask');
              t.is(model.get('properties')['purgeOlderThan'], '1');
              t.is(model.get('schedule'), 'manual');
            }

            next();
          },

          // remove it and check is gone
          // press delete button
          {click: '>>nx-coreui-task-feature button[action=delete]'},
          // then agree with removal and wait for grid to refresh
          Ext.apply(Ext.clone(waitForStoreToLoad),
              {trigger: {click: '>>button[text=Yes]'}}
          ),
          // then check that deleted task is no longer available in grid
          function(next) {
            var grid = t.cq1('nx-coreui-task-list'),
                store = grid.getStore(),
                model = store.findRecord('name', name);

            t.ok(!model, 'Task "' + name + '" has been removed');
            t.ok(!grid.getSelectionModel().getSelection().length, 'Grid has no selection');

            next();
          })
    });
    t.it('By default the list of tasks to choose from should not be filtered', function(t) {
      t.chain(
          // press the "New" button
          {click: '>>nx-coreui-task-list button[action=new]'},
          // and wait for add dialog
          function(next) {
            var store = t.cq1('nx-coreui-task-selecttype').getStore();
            t.expect(store.isFiltered()).toBe(false);
            t.expect(store.count()).toBeGreaterThan(0);
            next();
          }
      );
    });
    t.it('When filling out the filter, only matching entries should be shown', function(t) {
      t.chain(
          function(next) {
            t.waitForAnimations(next)
          },
          {type: 'zzz', target: '>>nx-coreui-task-selecttype nx-searchbox'},
          function(next) {
            var store = t.cq1('nx-coreui-task-selecttype').getStore();
            t.waitFor(function() {
              return store.isFiltered();
            }, next)
          },
          function(next) {
            var store = t.cq1('nx-coreui-task-selecttype').getStore();
            t.expect(store.count()).toBe(0);
            next();
          }
      )
    });
  });

});
