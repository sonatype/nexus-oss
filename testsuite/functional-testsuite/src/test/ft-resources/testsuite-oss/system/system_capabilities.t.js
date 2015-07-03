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
                }
            );
        });
    });
});