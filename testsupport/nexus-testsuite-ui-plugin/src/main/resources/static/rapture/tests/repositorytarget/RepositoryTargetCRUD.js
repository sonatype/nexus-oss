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

  var name = 'Test-' + new Date().getTime(),
      waitForStoreToLoad = { waitFor: 'storesToLoad', args: function () {
        return t.cq1('nx-coreui-repositorytarget-list').getStore();
      }};

  t.chain(
      { waitFor: 'stateReceived' },
      { waitFor: 'userToBeLoggedIn' },
      function (next) {
        t.navigateTo('admin/repository/targets');
        next();
      },
      { waitFor: 'rowsVisible', args: 'nx-coreui-repositorytarget-list' },

      // create and check selected model to have correct values
      { click: '>>nx-coreui-repositorytarget-list button[action=new]' },
      { waitFor: 'CQVisible', args: 'nx-coreui-repositorytarget-add' },
      { type: name + '[TAB]' },
      { type: 'maven2[ENTER][TAB]' },
      { type: '.*maven-metadata\\.xml.*[ENTER]' },
      { type: '.*-sources.*[ENTER]' },
      { click: '>>nx-coreui-repositorytarget-add button[action=add]' },
      { waitFor: 'CQNotVisible', args: 'nx-coreui-repositorytarget-add' },
      waitForStoreToLoad,
      function (next) {
        var grid = t.cq1('nx-coreui-repositorytarget-list'),
            store = grid.getStore(),
            model = store.findRecord('name', name),
            selected = grid.getSelectionModel().getSelection()[0];

        t.ok(model, 'Target "' + name + '" exists');
        if (model) {
          t.is(model.get('format'), 'maven2');
        }
        t.ok(selected, 'Grid has selection');
        if (selected && model) {
          t.ok(selected.getId() === model.getId(), 'Target "' + name + '" is selected in grid');
        }

        next();
      },

      // update and check selected model to have correct values
      { waitFor: 'CQVisible', args: 'nx-coreui-repositorytarget-settings' },
      function (next) {
        t.cq1('nx-coreui-repositorytarget-settings field[name=format]').setValue('maven1');
        next();
      },
      { click: '>>nx-coreui-repositorytarget-settings button[action=save]' },
      waitForStoreToLoad,
      function (next) {
        var grid = t.cq1('nx-coreui-repositorytarget-list'),
            store = grid.getStore(),
            model = store.findRecord('name', name),
            selected = grid.getSelectionModel().getSelection()[0];

        t.ok(model, 'Target "' + name + '" exists');
        if (model) {
          t.is(model.get('format'), 'maven1');
        }
        t.ok(selected, 'Grid has selection');
        if (selected && model) {
          t.ok(selected.getId() === model.getId(), 'Target "' + name + '" is selected in grid');
        }

        next();
      },

      // remove it and check is gone
      { click: '>>nx-coreui-repositorytarget-list button[action=delete]' },
      { waitFor: 'CQVisible', args: 'messagebox' },
      { click: '>>button[text=Yes]' },
      waitForStoreToLoad,
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
