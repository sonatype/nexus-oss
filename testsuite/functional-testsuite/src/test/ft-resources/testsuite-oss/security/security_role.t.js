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
 * Tests security role CRUD.
 */
StartTest(function(t) {

  var // generate a unique pattern
      id = t.uniqueId(),
  // a reusable function to wait for grid to load
      waitForStoreToLoad = { waitFor: 'storesToLoad', args: function() {
        return t.cq1('nx-coreui-role-list').getStore();
      }};

  t.chain(
      t.openPageAsAdmin('admin/security/roles'),
      // press the "New" button
      { click: '>>nx-coreui-role-list button[action=new]' },
      { click: '>>nx-coreui-role-list menuitem[action=newrole]' },
      // and wait for add dialog
      // set id
      { type: id, target: '>>nx-coreui-role-add field[name=id]' },
      // set name
      { type: 'name of ' + id, target: '>>nx-coreui-role-add field[name=name]' },
      // set a description
      { type: 'Description of ' + id, target: '>>nx-coreui-role-add field[name=description]' },
      // add privileges
      { dblclick: 'nx-coreui-role-add nx-itemselector[name=privileges] multiselectfield[title=Available] boundlist => .x-boundlist-item:contains(apikey-all)'},
      { dblclick: 'nx-coreui-role-add nx-itemselector[name=privileges] multiselectfield[title=Available] boundlist => .x-boundlist-item:contains(metrics-all)'},
      // add roles
      { dblclick: 'nx-coreui-role-add nx-itemselector[name=roles] multiselectfield[title=Available] boundlist => .x-boundlist-item:contains(admin)'},
      // we are ready to go, so press the "Add" button and wait for grid to refresh
      Ext.apply(Ext.clone(waitForStoreToLoad),
          { trigger: { click: '>>nx-coreui-role-add button[action=add]' } }
      ),
      // verify that the rule was added
      function(next) {
        t.waitForAnimations(function() {
          var grid = t.cq1('nx-coreui-role-list'),
            store = grid.getStore(),
            model = store.getById(id);

          // we can find the created record in store
          t.ok(model, 'Role "' + id + '" exists');
          // and values are the one we set
          if (model) {
            t.is(model.get('name'), 'name of ' + id);
            t.is(model.get('description'), 'Description of ' + id);
            t.is(model.get('privileges').sort().toString(), ['apikey-all', 'metrics-all'].toString());
            t.is(model.get('roles').toString(), ['admin'].toString());
          }

          // the entries are sorted in a case-insensitive manner
          var names = store.collect('name');
          var isSorted = Ext.Array.every(names, function (item, index, array) {
            return index === 0 || array[index - 1].toUpperCase().localeCompare(item.toUpperCase()) <= 0;
          });
          t.ok(isSorted, 'Role names should be sorted case-insensitively');
          // view the detail panel
          t.click(t.getRow(grid, grid.getView().indexOf(model)));

          next();
        });
      },

      
      function(next) {
        t.waitForAnimations(next);
      },
      function(next) {
        var roleStore = t.cq1('nx-coreui-role-settings-form').down('#roles').getStore();
        t.is(roleStore.isFiltered(), true, 'Should be filtered to remove self-referential choice');
        t.ok(roleStore.first(), 'Role store has at least one entry');
        t.ok(roleStore.find('id', id) === -1, 'Role store does not contain our new entry');
        t.ok(roleStore.find('id', 'admin') >= 0);
        t.ok(roleStore.find('id', 'anonymous') >= 0);
        next();
      },
      // update and check selected model to have correct values
      // change name
      { type: ' updated', target: '>>nx-coreui-role-settings field[name=name]' },
      // change description
      { type: ' updated', target: '>>nx-coreui-role-settings field[name=description]' },
      // change privileges
      { dblclick: 'nx-coreui-role-settings multiselectfield[title=Given] boundlist => .x-boundlist-item:contains(apikey-all)'},
      // change roles
      { dblclick: 'nx-coreui-role-settings multiselectfield[title=Contained] boundlist => .x-boundlist-item:contains(admin)'},
      { dblclick: 'nx-coreui-role-settings multiselectfield[title=Available] boundlist => .x-boundlist-item:contains(anonymous)'},
      // save changes and wait for grid to refresh
      Ext.apply(Ext.clone(waitForStoreToLoad),
          { trigger: { click: '>>nx-coreui-role-settings button[action=save]' } }
      ),
      // and verify that:
      function(next) {
        var grid = t.cq1('nx-coreui-role-list'),
            store = grid.getStore(),
            model = store.getById(id);

        // we can find the created record in store
        t.ok(model, 'Role "' + id + '" exists');
        // and values are the one we set
        if (model) {
          t.is(model.get('name'), 'name of ' + id + ' updated');
          t.is(model.get('description'), 'Description of ' + id + ' updated');
          t.is(model.get('privileges').toString(), ['metrics-all'].toString());
          t.is(model.get('roles').toString(), ['anonymous'].toString());
        }

        next();
      },

      // remove it and check is gone
      // press delete button
      { click: '>>nx-coreui-role-feature button[action=delete]' },
      // then agree with removal and wait for grid to refresh
      Ext.apply(Ext.clone(waitForStoreToLoad),
          { trigger: { click: '>>button[text=Yes]' } }
      ),
      // then check that deleted rule is no longer available in grid
      function(next) {
        var grid = t.cq1('nx-coreui-role-list'),
            store = grid.getStore(),
            model = store.getById(id);

        t.ok(!model, 'Role "' + id + '" has been removed');
        t.ok(!grid.getSelectionModel().getSelection().length, 'Grid has no selection');

        next();
      }
  );

});
