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
 * Tests support logging.
 */
StartTest(function(t) {
  var newLoggerName = t.uniqueId(),
      clickRow = function(t, query, column, value) {
        return function(next) {
          var grid = t.cq1(query),
              store = grid.getStore(),
              row = t.getRow(grid, store.find(column, value));
          t.pass(['click:', query, column, value].join(' '));
          t.click(row, next);
        };
      },
      waitForMessageDialogsToClose = function() {
        return {
          waitFor: 'CQNotFound',
          args: 'nx-message-notification',
          desc: 'Wait for message dialogs to close'
        }
      };

  Ext.merge(t.controller('Message').windowOptions, {
    slideInDuration: -1,
    slideBackDuration: -1,
    autoCloseDelay: -1,
  });

  t.describe('Support Logging', function(t) {
    t.it('Logging Smoke Test', function(t) {
      t.chain(
        t.openPageAsAdmin('admin/support/logging'),
        { waitFor: 'CQVisible', args: 'nx-coreui-logger-list' },
        { waitFor: 'CQVisible', args: 'nx-coreui-logger-list button[action=new]' },
        { waitFor: 'CQVisible', args: 'nx-coreui-logger-list button[action=delete]' },
        { waitFor: 'CQVisible', args: 'nx-coreui-logger-list button[action=reset]' },
        waitForMessageDialogsToClose(),
        { waitForRowsVisible: '>>nx-coreui-logger-list' },
        { waitFor: function() { return t.cq1('nx-coreui-logger-list'); } },
        { waitFor: function() { return t.cq1('nx-coreui-logger-list').getStore(); } },
        clickRow(t, 'nx-coreui-logger-list', 'name', 'ROOT'),
        { click: 'combobox[dataIndex=level] => .x-form-text' },
        { click: '.x-boundlist-item:contains(DEBUG)' },
        { click: '>>button#cancel' },
        // explore reset to default levels
        { click: '>>nx-coreui-logger-list button[action=reset]' },
        { waitForCQVisible: 'button#yes' },
        function(next, components) { t.click(components[0], next); },
        waitForMessageDialogsToClose()
      );
    });
    t.it('Create Logger', function(t) {
      t.chain(
        // create logger {newLoggerName}
        { waitForRowsVisible: '>>nx-coreui-logger-list' },
        { click : '>>nx-coreui-logger-list button[action=new]' },
        { type: newLoggerName, target: '>>nx-coreui-logger-add field#name' },
        { waitForCQVisible: 'nx-coreui-logger-add button[action=add]' },
        function(next, components) { t.click(components[0], next); },
        waitForMessageDialogsToClose(),
        // look for {newLoggerName} in the grid panel
        { waitForRowsVisible: '>>nx-coreui-logger-list' },
        function(next) {
          var grid = t.cq1('nx-coreui-logger-list'),
              store = grid.getStore(),
              model = store.findRecord('name', newLoggerName);
          t.ok(model, 'Logger "' + newLoggerName + '" exists');
          t.is(model.get('level'), 'INFO', 'Logger level is INFO');
          next();
        }
      );
    });
    // PhantomJS doesn't currently handle this scenario correctly
    if (!(/PhantomJS/.test(t.global.navigator.userAgent))) {
      t.it('Update Logger', function(t) {
        t.chain(
          // update logging level for {newLoggerName}
          { waitForRowsVisible: '>>nx-coreui-logger-list' },
          clickRow(t, 'nx-coreui-logger-list', 'name', newLoggerName),
          { click: 'combobox[dataIndex=level] => .x-form-text' },
          { click: '.x-boundlist-item:contains(ERROR)' },
          { waitForCQVisible: 'button#update' },
          function(next, components) { t.click(components[0], next); },
          waitForMessageDialogsToClose(),
          // check that logging level for {newLoggerName} is ERROR
          { waitForRowsVisible: '>>nx-coreui-logger-list' },
          function(next) {
            var grid = t.cq1('nx-coreui-logger-list'),
                store = grid.getStore(),
                model = store.findRecord('name', newLoggerName);
            t.ok(model, 'Logger "' + newLoggerName + '" exists');
            t.is(model.get('level'), 'ERROR', 'Logger level is ERROR');
            next();
          }
        );
      });
    }
    t.it('Delete Logger', function(t) {
      t.chain(
        // delete logger {newLoggerName}
        { waitForRowsVisible: '>>nx-coreui-logger-list' },
        clickRow(t, 'nx-coreui-logger-list', 'name', newLoggerName),
        { click: '>>nx-coreui-logger-list button[action=delete]' },
        { waitForCQVisible: 'button#yes' },
        function(next, components) { t.click(components[0], next); },
        waitForMessageDialogsToClose(),
        // check that {newLoggerName} no longer exists
        { waitForRowsVisible: '>>nx-coreui-logger-list' },
        function(next) {
          var grid = t.cq1('nx-coreui-logger-list'),
              store = grid.getStore(),
              model = store.findRecord('name', newLoggerName);
          t.notOk(model, 'Logger "' + newLoggerName + '" no longer exists');
          next();
        }
      );
    });
    t.it('Log Viewer Smoke Test', function(t) {
      t.chain(
        t.navigateTo('admin/support/logging/logviewer'),
        { waitFor: 'CQVisible', args: 'nx-coreui-log-viewer' },
        { waitFor: 'CQVisible', args: 'nx-coreui-log-viewer button[action=download]' },
        { waitFor: 'CQVisible', args: 'nx-coreui-log-viewer button[action=mark]' },
        { waitFor: 'CQVisible', args: 'nx-coreui-log-viewer combo#refreshPeriod' },
        { waitFor: 'CQVisible', args: 'nx-coreui-log-viewer combo#refreshSize' },
        { waitFor: 'CQVisible', args: 'nx-coreui-log-viewer textarea' }
      );
    });
  });
});
