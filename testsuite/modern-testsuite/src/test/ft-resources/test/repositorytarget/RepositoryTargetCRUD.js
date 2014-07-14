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
/**
 * Tests repository target CRUD.
 *
 * @since 2.8
 */
StartTest(function (t) {

  var // generate a unique name
      name = 'Test-' + new Date().getTime(),
      // a reusable function to wait for grid to load
      waitForStoreToLoad = { waitFor: 'storesToLoad', args: function () {
        return t.cq1('nx-coreui-repositorytarget-list').getStore();
      }};

  t.chain(
      // wait for receiving state from server
      { waitFor: 'stateReceived' },
      // automatically logon using admin/admin123
      { waitFor: 'userToBeLoggedIn' },
      // go to Repository Targets
      function (next) {
        t.navigateTo('admin/repository/targets');
        next();
      },
      // and wait for repository targets to be loaded in grid
      { waitFor: 'rowsVisible', args: 'nx-coreui-repositorytarget-list' },

      // press the "New" button
      { click: '>>nx-coreui-repositorytarget-list button[action=new]' },
      // and wait for add dialog
      { waitFor: 'CQVisible', args: 'nx-coreui-repositorytarget-add' },
      // focus whould be already in the name field so simulate the name input
      { type: name },
      // select "maven2" as format
      function (next) {
        t.cq1('nx-coreui-repositorytarget-add field[name=format]').setValue('maven2');
        next();
      },
      // focus the patterns value set
      { click: '>>nx-coreui-repositorytarget-add field[inputFor=patterns]' },
      // and add a bunch of them
      { type: '.*maven-metadata\\.xml.*[ENTER]' },
      { type: '.*-sources.*[ENTER]' },
      // we are ready to go, so press the "Add" button
      { click: '>>nx-coreui-repositorytarget-add button[action=add]' },
      // wait for add dialog to disappear (this should mean that target was added)
      { waitFor: 'CQNotVisible', args: 'nx-coreui-repositorytarget-add' },
      // and wait for grid to refresh
      waitForStoreToLoad,
      // and verify that:
      function (next) {
        var grid = t.cq1('nx-coreui-repositorytarget-list'),
            store = grid.getStore(),
            model = store.findRecord('name', name),
            selected = grid.getSelectionModel().getSelection()[0];

        // we can find the created record in store
        t.ok(model, 'Target "' + name + '" exists');
        // and format is the one we set
        if (model) {
          t.is(model.get('format'), 'maven2');
        }
        // the new created target is selected in grid
        t.ok(selected, 'Grid has selection');
        if (selected && model) {
          t.ok(selected.getId() === model.getId(), 'Target "' + name + '" is selected in grid');
        }

        next();
      },

      // update and check selected model to have correct values
      { waitFor: 'CQVisible', args: 'nx-coreui-repositorytarget-settings' },
      function (next) {
        // change format to "maven1"
        t.cq1('nx-coreui-repositorytarget-settings field[name=format]').setValue('maven1');
        next();
      },
      // save changes
      { click: '>>nx-coreui-repositorytarget-settings button[action=save]' },
      // and wait for grid to refresh
      waitForStoreToLoad,
      // and verify that:
      function (next) {
        var grid = t.cq1('nx-coreui-repositorytarget-list'),
            store = grid.getStore(),
            model = store.findRecord('name', name),
            selected = grid.getSelectionModel().getSelection()[0];

        // we can find the created record in store
        t.ok(model, 'Target "' + name + '" exists');
        // and format is the one we set
        if (model) {
          t.is(model.get('format'), 'maven1');
        }
        // the updated target is selected in grid
        t.ok(selected, 'Grid has selection');
        if (selected && model) {
          t.ok(selected.getId() === model.getId(), 'Target "' + name + '" is selected in grid');
        }

        next();
      },

      // remove it and check is gone
      // press delete button
      { click: '>>nx-coreui-repositorytarget-list button[action=delete]' },
      // and wait for confirmation box
      { waitFor: 'CQVisible', args: 'messagebox' },
      // then agree with removal
      { click: '>>button[text=Yes]' },
      // and wait for grid to refresh
      waitForStoreToLoad,
      // then check that deleted target is no longer available in grid
      function (next) {
        var grid = t.cq1('nx-coreui-repositorytarget-list'),
            store = grid.getStore(),
            model = store.findRecord('name', name);

        t.ok(!model, 'Target "' + name + '" has been removed');
        t.ok(!grid.getSelectionModel().getSelection().length, 'Grid has no selection');

        next();
      }
  );

});
