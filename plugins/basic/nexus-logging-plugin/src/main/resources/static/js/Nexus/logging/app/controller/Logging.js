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

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  requires: [
    'Nexus.logging.app.view.Panel',
    'Nexus.logging.app.view.Add'
  ],

  init: function () {
    this.control({
      '#nx-logging-button-refresh-loggers': {
        click: this.loadLoggers
      },
      '#nx-logging-button-add-logger': {
        click: this.addLogger
      },
      '#nx-logging-button-remove-loggers': {
        click: this.removeLoggers
      },
      '#nx-logging-button-save': {
        click: this.saveLogger
      }
    });
  },

  loadLoggers: function (button) {
    var loggersGrid = button.up('nx-logging-view-loggers'),
        store = loggersGrid.getStore();

    store.load();
  },

  addLogger: function (button) {
    var loggersGrid = button.up('nx-logging-view-loggers'),
        win = Ext.create({xtype: 'nx-logging-view-add'});

    win.grid = loggersGrid;
    win.show();
  },

  saveLogger: function (button) {
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

    if (sm.hasSelection()) {
      store.remove(sm.selection.record);
    }
  }

});