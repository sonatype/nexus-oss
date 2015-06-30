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
 * Tests security user CRUD.
 */
StartTest(function(t) {

  var // generate a unique pattern
      id = 'Test-' + new Date().getTime(),
  // a reusable function to wait for grid to load
      waitForStoreToLoad = { waitFor: 'storesToLoad', args: function() {
        return t.cq1('nx-coreui-user-list').getStore();
      }};

  t.chain(
      t.openPageAsAdmin('admin/security/users'),
      // press the "New" button
      { click: '>>nx-coreui-user-list button[action=new]' },
      // set id
      { type: id, target: '>>nx-coreui-user-add field[name=userId]' },
      // set first name
      { type: 'First name of ' + id, target: '>>nx-coreui-user-add field[name=firstName]' },
      // set last name
      { type: 'Last name of ' + id, target: '>>nx-coreui-user-add field[name=lastName]' },
      // set email
      { type: id + '@sonatype.org', target: '>>nx-coreui-user-add field[name=email]' },
      // set password
      { type: id + '[TAB]', target: '>>nx-coreui-user-add field[name=password]' },
      // and confirm password
      { type: id },
      // set status to active
      { click: 'nx-coreui-user-add combobox[name=status] => .x-form-text' },
      { click: '.x-boundlist-item:contains(Active)' },
      // add roles
      { dblclick: 'nx-coreui-user-add nx-itemselector[name=roles] multiselectfield[title=Available] boundlist => .x-boundlist-item:contains(admin)'},
      // we are ready to go, so press the "Add" button and wait for grid to refresh
      Ext.apply(Ext.clone(waitForStoreToLoad),
          { trigger: { click: '>>nx-coreui-user-add button[action=add]' } }
      ),
      // verify that the user was added
      function(next) {
        t.waitForAnimations(function() {
          var grid = t.cq1('nx-coreui-user-list'),
            store = grid.getStore(),
            model = store.getById(id),
            selected = grid.getSelectionModel().getSelection()[0];

          // we can find the created record in store
          t.ok(model, 'User "' + id + '" exists');
          // and values are the one we set
          if (model) {
            t.is(model.get('firstName'), 'First name of ' + id);
            t.is(model.get('lastName'), 'Last name of ' + id);
            t.is(model.get('email'), id + '@sonatype.org');
            t.is(model.get('status'), 'active');
            t.is(model.get('roles').toString(), ['admin'].toString());
          }
          // view the detail panel
          t.click(t.getRow(grid, grid.getView().indexOf(model)));

          next();
        });
      },

      // update and check selected model to have correct values
      function(next) {
        t.waitForAnimations(function() {
          next();
        });
      },
      // change name
      { type: ' updated', target: '>>nx-coreui-user-settings field[name=firstName]' },
      // change description
      { type: ' updated', target: '>>nx-coreui-user-settings field[name=lastName]' },
      // change email
      { type: '.com', target: '>>nx-coreui-user-settings field[name=email]' },
      // change status to Disabled
      { click: 'nx-coreui-user-settings combobox[name=status] => .x-form-text' },
      { click: 'combobox[name=status].getPicker() => .x-boundlist-item:contains(Disabled)' },
      // change roles
      { dblclick: 'nx-coreui-user-settings multiselectfield[title=Granted] boundlist => .x-boundlist-item:contains(admin)'},
      { dblclick: 'nx-coreui-user-settings multiselectfield[title=Available] boundlist => .x-boundlist-item:contains(anonymous)'},
      
      // save changes and wait for grid to refresh
      Ext.apply(Ext.clone(waitForStoreToLoad),
          { trigger: { click: '>>nx-coreui-user-settings button[action=save]' } }
      ),
      // and verify that:
      function(next) {
        var grid = t.cq1('nx-coreui-user-list'),
            store = grid.getStore(),
            model = store.getById(id),
            selected = grid.getSelectionModel().getSelection()[0];

        // we can find the created record in store
        t.ok(model, 'User "' + id + '" exists');
        // and values are the one we set
        if (model) {
          t.is(model.get('firstName'), 'First name of ' + id + ' updated');
          t.is(model.get('lastName'), 'Last name of ' + id + ' updated');
          t.is(model.get('email'), id + '@sonatype.org' + '.com');
          t.is(model.get('status'), 'disabled');
          t.is(model.get('roles').toString(), ['anonymous'].toString());
        }
        // the updated rule is selected in grid
        t.ok(selected, 'Grid has selection');
        if (selected && model) {
          t.ok(selected.getId() === model.getId(), 'User "' + id + '" is selected in grid');
        }

        next();
      },

      // remove it and check is gone
      // press delete button
      { click: '>>nx-coreui-user-feature button[action=delete]' },
      // and wait for confirmation box
      // then agree with removal and wait for grid to refresh
      Ext.apply(Ext.clone(waitForStoreToLoad),
          { trigger: { click: '>>button[text=Yes]' } }
      ),
      // then check that deleted rule is no longer available in grid
      function(next) {
        var grid = t.cq1('nx-coreui-user-list'),
            store = grid.getStore(),
            model = store.getById(id);

        t.ok(!model, 'User "' + id + '" has been removed');
        t.ok(!grid.getSelectionModel().getSelection().length, 'Grid has no selection');

        next();
      }
  );

});
