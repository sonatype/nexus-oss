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
 * Tests that change events are fired when a state value changes.
 */
StartTest(function (t) {

  t.ok(Ext, 'Ext is here');
  t.ok(NX, 'NX is here');

  t.requireOk('NX.controller.State', function() {
    var state = Ext.create('NX.controller.State');
    state.init();

    t.chain(
        function (next) {
          t.waitForEvent(state, 'foochanged', function () {
            t.pass('Setting a value will fire change event');
            next();
          });
          state.setValue('foo', 'bar');
        },
        function (next) {
          t.waitForEvent(state, 'foochanged', function () {
            t.pass('Setting a different value will fire change event');
            next();
          });
          state.setValue('foo', 'bar1');
        },
        function (next) {
          t.wontFire(state, 'foochanged', 'Setting same value will not fire change event');
          state.setValue('foo', 'bar1');
          next();
        }
    );
  });
});
