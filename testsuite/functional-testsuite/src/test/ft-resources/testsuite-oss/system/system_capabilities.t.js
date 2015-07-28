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
 * Tests system capabilities CRUD.
 */
StartTest(function(t) {

  // Name/type-id of Capability tested
  var capabilityTypeName = 'UI: Branding',
      capabilityTypeId = 'rapture.branding';

  t.describe('Given an admin administering Capabilities', function(t) {
    t.it('Should be able to create a new Capability', function(t) {
      t.chain(
          t.openPageAsAdmin('admin/system/capabilities'),
          // CREATE: Wait for create capability button to be enabled
          {waitFor: 'CQ', args: 'nx-coreui-capability-list button[action=new][disabled=false]'},

          // Press the create capability button
          {click: '>>nx-coreui-capability-list button[action=new]'},
          // and wait for list
          function(next) {
            t.waitForAnimations(next);
          },
          // Find the capability and click it
          function(next) {
            var grid = t.cq1('nx-coreui-capability-selecttype'),
                baseURLRow = t.getRow(grid, grid.getStore().find('name', capabilityTypeName));

            t.click(baseURLRow, next);
          },
          // and wait for details to load
          function(next) {
            t.waitForAnimations(next);
          },
          // Press the "Add" button and wait for grid to refresh
          Ext.apply(t.waitForStore('Capability'),
              {trigger: {click: '>>nx-coreui-capability-add button[action=add]'}}
          ),
          // READ: Verify that our record is saved
          function(next) {
            var grid = t.cq1('nx-coreui-capability-list'),
                store = grid.getStore(),
                model = store.findRecord('typeName', capabilityTypeName);

            // we can find the created capability
            t.ok(model, 'Capability ' + capabilityTypeName + ' exists');
            // and values correspond
            t.is(model.get('typeId'), capabilityTypeId);

            // Back to the detail panel view
            t.click(t.getRow(grid, grid.getView().indexOf(model)));

            next();
          },
          //and wait for view to load
          function(next) {
            t.waitForAnimations(next);
          }
      );
    });
    t.it('Should be able to update the new Capability', function(t) {
      t.chain(
          // UPDATE: Select Settings pill and wait for transition
          function(next) {
            t.waitForAnimations(function() {
              t.click('>>nx-drilldown-details tab[text=Settings]');
              next();
            });
          },
          // then set capability to disabled
          {click: '>>nx-coreui-capability-settings field[name=enabled]'},
          // Save changes
          Ext.apply(t.waitForStore('Capability'),
              {trigger: {click: '>>nx-coreui-capability-settings button[action=save]'}}
          ),

          // Verify that
          function(next) {
            var grid = t.cq1('nx-coreui-capability-list'),
                store = grid.getStore(),
                model = store.findRecord('typeName', capabilityTypeName);

            // we can find the updated record in store
            t.ok(model, 'Capability ' + capabilityTypeName + ' exists');
            // and is it disabled
            t.is(model.get('enabled'), false);

            next();
          }
      );
    });
    t.it('Should be able to delete the new Capability', function(t) {
      t.chain(
          // DELETE: Press delete button
          {click: '>>nx-coreui-capability-feature button[action=delete]'},
          // then agree with removal and wait for grid to refresh
          Ext.apply(t.waitForStore('Capability'),
              {trigger: {click: '>>button[text=Yes]'}}
          ),
          // Check that deleted capability is no longer available in grid
          function(next) {
            var grid = t.cq1('nx-coreui-capability-list'),
                store = grid.getStore(),
                model = store.findRecord('typeName', capabilityTypeName);

            t.ok(!model, 'Capability ' + capabilityTypeName + ' has been removed');
            t.ok(!grid.getSelectionModel().getSelection().length, 'Grid has no selection');

            next();
          }
      );
    });
  });
});
