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
    var waitForStoreToLoad = {
        waitFor: 'storesToLoad', args: function() {
            return t.cq1('nx-coreui-capability-list').getStore();
        }
    };

    t.describe('Given an admin when the admin is administering Capabilities', function(t) {
        t.it('then CRUD should work', function(t) {
            t.chain(
                t.openPageAsAdmin('admin/system/capabilities'),
                // Wait for create capability button to be enabled
                {waitFor: 'CQ', args: 'nx-coreui-capability-list button[action=new][disabled=false]'},

                // Press the create capability button
                {click: '>>nx-coreui-capability-list button[action=new]'},
                // and wait for add dialog
                function(next) {
                    t.waitForAnimations(next);
                },
                function(next) {
                    var grid = t.cq1('nx-coreui-capability-selecttype'),
                        baseURLRow = t.getRow(grid, grid.getStore().find('name', 'Base URL: Force'));
                    t.click(baseURLRow, next);
                },
                function(next) {
                    t.waitForAnimations(next);
                },
                // we are ready to go, so press the "Add" button and wait for grid to refresh
                Ext.apply(Ext.clone(waitForStoreToLoad),
                    {trigger: {click: '>>nx-coreui-capability-add button[action=add]'}}
                ),
                // verify that the capability was added
                function(next) {
                    var grid = t.cq1('nx-coreui-capability-list'),
                        store = grid.getStore(),
                        model = store.findRecord('typeName', 'Base URL: Force');

                    // we can find the created record in store
                    t.ok(model, 'Capability "Base URL: Force" exists');
                    // and values are the one we set
                    if (model) {
                        t.is(model.get('typeId'), 'baseurl.force');
                    }

                    // view the detail panel
                    t.click(t.getRow(grid, grid.getView().indexOf(model)));

                    next();
                }
            );
        });
    });
});