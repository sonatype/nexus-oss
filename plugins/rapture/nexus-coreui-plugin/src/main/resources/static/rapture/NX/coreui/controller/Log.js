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
/*global Ext, NX*/

/**
 * View log controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Log', {
  extend: 'Ext.app.Controller',
  mixins: {
    logAware: 'NX.LogAware'
  },
  requires: [
    'NX.util.Url',
    'NX.util.DownloadHelper'
  ],

  views: [
    'logging.LogViewer',
    'logging.LogMark'
  ],
  refs: [
    {
      ref: 'log',
      selector: 'nx-coreui-log-viewer'
    }
  ],

  init: function () {
    var me = this;

    me.getApplication().getFeaturesController().registerFeature({
      mode: 'admin',
      path: '/Support/Logging/Log Viewer',
      description: 'View the current log contents',
      view: { xtype: 'nx-coreui-log-viewer' },
      iconConfig: {
        file: 'script_text.png',
        variants: ['x16', 'x32']
      },
      visible: function () {
        return NX.Permissions.check('nexus:logs', 'read');
      }
    });

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.retrieveLog
        }
      },
      component: {
        'nx-coreui-log-viewer': {
          afterrender: me.retrieveLog,
          destroy: me.stopRetrieveLogTask
        },
        'nx-coreui-log-viewer button[action=download]': {
          click: me.downloadLog
        },
        'nx-coreui-log-viewer button[action=mark]': {
          afterrender: me.bindMarkButton,
          click: me.showMarkWindow
        },
        'nx-coreui-log-mark form': {
          submitted: me.onLogMarked
        },
        'nx-coreui-log-viewer #refreshPeriod': {
          change: me.changeRefreshPeriod
        },
        'nx-coreui-log-viewer #refreshSize': {
          change: me.changeRefreshSize
        }
      }
    });
  },

  /**
   * @private
   * Shows mark log window.
   */
  showMarkWindow: function () {
    Ext.widget({ xtype: 'nx-coreui-log-mark' });
  },

  /**
   * @private
   */
  onLogMarked: function (form) {
    this.retrieveLog();
    form.up('window').close();
  },

  /**
   * @private
   * Opens a new browser window pointing to GET /service/siesta/logging/log.
   */
  downloadLog: function () {
    NX.util.DownloadHelper.downloadUrl(NX.util.Url.urlOf('service/siesta/logging/log'));
  },

  /**
   * @private
   * Changes retrieving task recurrence interval when refresh period changed.
   * @param {Ext.form.ComboBox} combo refresh period combobox
   * @param {Number} value period in seconds
   */
  changeRefreshPeriod: function (combo, value) {
    var me = this;

    me.stopRetrieveLogTask();
    me.startRetrieveLogTask(value);
  },

  /**
   * @private
   * Triggers log retrieval when refresh size changed.
   */
  changeRefreshSize: function () {
    this.retrieveLog();
  },

  /**
   * @private
   * Starts log retrieval task, with a recurrence specified by period.
   * @param {Number} period in seconds
   */
  startRetrieveLogTask: function (period) {
    var me = this;

    if (period > 0) {
      me.retrieveLog();
      me.retrieveLogTask = Ext.util.TaskManager.newTask({
        run: function () {
          me.retrieveLog();
        },
        interval: period * 1000
      });
      me.retrieveLogTask.start();
      me.logDebug('Started refreshing log every ' + period + ' seconds');
    }
  },

  /**
   * @private
   * Stops log retrieval task, if active.
   */
  stopRetrieveLogTask: function () {
    var me = this;

    if (me.retrieveLogTask) {
      me.retrieveLogTask.destroy();
      delete me.retrieveLogTask;
      me.logDebug('Stopped refreshing log');
    }
  },

  /**
   * @private
   * Retrieves log from /service/internal/logs/nexus.log and shows it in log panel.
   */
  retrieveLog: function () {
    var me = this,
        logPanel = me.getLog(),
        size;

    if (logPanel) {
      size = logPanel.down('#refreshSize').getValue();

      logPanel.getEl().mask('Loading...');

      me.logDebug('Retrieving last ' + size + 'kb from log');

      Ext.Ajax.request({
        url: NX.util.Url.urlOf('service/siesta/logging/log'),
        method: 'GET',
        headers: {
          'accept': 'text/plain'
        },
        params: {
          bytesCount: -1024 * size
        },
        scope: me,
        suppressStatus: true,
        success: function (response) {
          logPanel.getEl().unmask();
          me.showLog(response.responseText);
        },
        failure: function (response) {
          logPanel.getEl().unmask();
          me.showLog('Failed to retrieve log due to "' + response.statusText + '".');
        }
      });
    }
  },

  /**
   * @private
   * Display log content.
   * @param {String} text log content
   */
  showLog: function (text) {
    var me = this,
        textarea = me.getLog().down('textarea');

    textarea.setValue(text);
    // scroll to the bottom
    textarea.getEl().down('textarea').dom.scrollTop = 1000000;
  },

  /**
   * @private
   * Enable 'Mark' when user has 'update' permission.
   */
  bindMarkButton: function (button) {
    button.mon(
        NX.Conditions.isPermitted('nexus:logs', 'update'),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  }

});