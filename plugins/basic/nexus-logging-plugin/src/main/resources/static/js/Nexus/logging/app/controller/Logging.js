/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global NX, Ext, Sonatype, Nexus*/

/**
 * Logging controller.
 *
 * @since 2.7
 */
NX.define('Nexus.logging.app.controller.Logging', {
  extend: 'Nexus.app.Controller',

  requires: [
    'Nexus.siesta',
    'Nexus.logging.app.view.Panel',
    'Nexus.logging.app.view.Add',
    'Nexus.logging.app.view.Mark'
  ],

  init: function () {
    var me = this;

    me.control({
      '#nx-logging-button-refresh-loggers': {
        click: this.loadLoggers
      },
      '#nx-logging-button-add-logger': {
        click: this.showAddLogger
      },
      '#nx-logging-button-remove-loggers': {
        click: this.removeLoggers
      },
      '#nx-logging-button-mark': {
        click: this.showMarkLog
      },
      '#nx-logging-button-add-save': {
        click: this.addLogger
      },
      '#nx-logging-button-mark-save': {
        click: this.markLog
      },
      '#nx-logging-button-refresh-log': {
        click: this.refreshLog
      }
    });

    me.getLogTask = {
      run: function () {
        this.loadTail();
      },
      interval: 10000,
      scope: me,
      started: false
    };
  },

  loadLoggers: function (button) {
    var loggersGrid = button.up('nx-logging-view-loggers'),
        store = loggersGrid.getStore();

    store.load();
  },

  showAddLogger: function (button) {
    var loggersGrid = button.up('nx-logging-view-loggers'),
        win = Ext.create({xtype: 'nx-logging-view-add'});

    win.grid = loggersGrid;
    win.show();
  },

  addLogger: function (button) {
    var win = button.up('nx-logging-view-add'),
        store = win.grid.getStore(),
        form = win.down('form')[0].getForm(),
        values = form.getFieldValues(),
        record = store.getById(values.name);

    if (Ext.isDefined(record)) {
      Ext.Msg.show({
        title: 'Confirm',
        msg: 'Logger "' + values.name
            + '" is already configured. Would you like to update its level to "' + values.level + '"?',
        buttons: Ext.Msg.YESNO,
        icon: Ext.MessageBox.QUESTION,
        closeable: false,
        scope: self,
        fn: function (buttonName) {
          if (buttonName == 'yes' || buttonName == 'ok') {
            record.set('level', values.level);
            store.save();
            win.grid.getSelectionModel().select(store.indexOf(record), 0);
            win.close();
          }
        }
      });
    }
    else {
      record = new store.recordType(values);
      store.add(record);
      win.grid.getSelectionModel().select(store.indexOf(record), 0);
      win.close();
    }
  },

  removeLoggers: function (button) {
    var loggersGrid = button.up('nx-logging-view-loggers'),
        sm = loggersGrid.getSelectionModel(),
        store = loggersGrid.getStore();

    // FIXME: Show confirmation before removing

    if (sm.hasSelection()) {
      store.remove(sm.selection.record);
    }
  },

  showMarkLog: function () {
    Ext.create({xtype: 'nx-logging-view-mark'}).show();
  },

  markLog: function (button) {
    var win = button.up('nx-logging-view-mark'),
        form = win.down('form')[0].getForm(),
        values = form.getFieldValues();

    win.close();

    Ext.Ajax.request({
      url: Nexus.siesta.basePath + '/logging/log/mark',
      method: 'PUT',
      suppressStatus: true,
      jsonData: values,
      success: function () {
        Nexus.messages.show('Confirmation', 'Log has been marked with "' + values.message + '"');
      },
      failure: function (response) {
        // TODO shall we show a message and ask user to retry?
        var message;
        if (response.siestaError) {
          message = response.siestaError.message;
        }
        if (!message && response.responseText) {
          message = Sonatype.utils.parseHTMLErrorMessage(response.responseText);
        }
        if (!message) {
          message = response.statusText;
        }
        Nexus.messages.show('Failed to mark log file', message);
      }
    });
  },

  refreshLog: function (button) {
    this.retrieveLog(button.up('nx-logging-view-log'));
  },

  retrieveLog: function (logPanel) {
    var me = this;

    Ext.Ajax.request({
      url: Sonatype.config.repos.urls.logs + '/nexus.log',
      method: 'GET',
      headers: {
        'accept': 'text/plain'
      },
      params: {
        count: -10240
      },
      scope: me,
      suppressStatus: true,
      success: function (response) {
        logPanel.showLog(response.responseText);
      },
      failure: function (response) {
        logPanel.showLog(response.responseText);
      }
    });
  }

});