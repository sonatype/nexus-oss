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
 * Analytics controller.
 *
 * @since 2.8
 */
NX.define('Nexus.analytics.controller.Analytics', {
  extend: 'Nexus.controller.Controller',

  requires: [
    'Nexus.siesta',
    'Nexus.analytics.Icons',
    'Nexus.analytics.view.EventsZipCreated',
    'Nexus.analytics.view.Panel',
    'Nexus.util.DownloadHelper'
  ],

  init: function() {
    var me = this;

    me.control({
      '#nx-analytics-view-settings': {
        activate: me.loadSettings
      },
      '#nx-analytics-view-settings-button-save': {
        click: me.saveSettings
      },
      '#nx-analytics-view-events': {
        activate: me.loadEvents
      },
      '#nx-analytics-view-events-button-refresh': {
        click: me.refreshEvents
      },
      '#nx-analytics-view-events-button-clear': {
        click: me.clearEvents
      },
      '#nx-analytics-view-events-button-export': {
        click: me.exportEvents
      },
      '#nx-analytics-view-events-button-submit': {
        click: me.submitEvents
      },
      '#nx-analytics-button-eventszip-download': {
        'authenticated': me.downloadEventsZip
      }
    });

    me.addNavigationMenu();
  },

  /**
   * Install panel into main navigation menu.
   *
   * @private
   */
  addNavigationMenu: function() {
    Sonatype.Events.on('nexusNavigationInit', function(panel) {
      var sp = Sonatype.lib.Permissions;

      panel.add({
        enabled: sp.checkPermission('nexus:analytics', sp.READ),
        sectionId: 'st-nexus-config',
        title: 'Analytics',
        tabId: 'analytics',
        tabCode: function() {
          return Ext.create({ xtype: 'nx-analytics-view-panel', id: 'analytics' });
        }
      });
    });
  },

  /**
   * Helper to show Analytics message.
   *
   * @private
   */
  showMessage: function(message) {
    Nexus.messages.show('Analytics', message);
  },

  /**
   * @private
   */
  loadSettings: function() {
    var me = this;

    me.logDebug('Loading settings');

    Ext.Ajax.request({
      url: Nexus.siesta.basePath + '/analytics/settings',
      method: 'GET',

      scope: me,
      success: function (response, opts) {
        me.logDebug('Settings: ' + response.responseText);
        var values = Ext.decode(response.responseText);

        Ext.getCmp('nx-analytics-view-settings').setValues(values);

        // show/hide the events tab if collection is enabled/disabled
        var tabpanel = Ext.getCmp('analytics').down('tabpanel');
        var eventsview = Ext.getCmp('nx-analytics-view-events');
        if (values.collection === true) {
          tabpanel.unhideTabStripItem(eventsview);
        }
        else {
          tabpanel.hideTabStripItem(eventsview);
        }
      }
    });
  },

  /**
   * @private
   */
  saveSettings: function(button) {
    var me = this,
        values = Ext.getCmp('nx-analytics-view-settings').getValues();

    me.logDebug('Saving settings: ' + Ext.util.JSON.encode(values));

    Ext.Ajax.request({
      url: Nexus.siesta.basePath + '/analytics/settings',
      method: 'PUT',
      jsonData: values,

      scope: me,
      success: function () {
        me.showMessage('Settings saved');

        // reload settings to apply view customiztions
        me.loadSettings();
      }
    });
  },

  /**
   * Load events.
   *
   * @private
   */
  loadEvents: function(panel) {
    var store = panel.getGrid().getStore();
    store.load({
      params: {
        start: 0,
        limit: Nexus.analytics.store.Events.PAGE_SIZE
      }
    });
  },

  /**
   * Refresh events panel.
   *
   * @private
   */
  refreshEvents: function(button) {
    var me = this,
        panel = button.up('nx-analytics-view-events');
    me.loadEvents(panel);
  },

  /**
   * Clear all events.
   *
   * @private
   */
  clearEvents: function(button) {
    var me = this,
        icons = Nexus.analytics.Icons,
        store = button.up('nx-analytics-view-events').getGrid().getStore();

    Ext.Msg.show({
      title: 'Clear events',
      msg: 'Clear analytics event data?',
      buttons: Ext.Msg.OKCANCEL,
      icon: icons.get('clear').variant('x32').cls,
      fn: function (btn) {
        if (btn === 'ok') {
          Ext.Ajax.request({
            url: Nexus.siesta.basePath + '/analytics/events',
            method: 'DELETE',
            suppressStatus: true,
            success: function () {
              me.showMessage('Event data has been cleared');
              store.load();
            },
            failure: function (response) {
              me.showMessage('Failed to clear event data: ' + me.parseExceptionMessage(response));
            }
          });
        }
      }
    });
  },

  /**
   * Export and download events.
   *
   * @private
   */
  exportEvents: function(button) {
    var me = this,
        icons = Nexus.analytics.Icons,
        viewport = button.up('viewport'),
        mask = NX.create('Ext.LoadMask', viewport.getEl(), { msg: 'Exporting event data...' });

    Ext.Msg.show({
      title: 'Export events',
      msg: 'Export and download analytics event data?<br/>No data will be sent to Sonatype.',
      buttons: Ext.Msg.OKCANCEL,
      icon: icons.get('_export').variant('x32').cls,
      fn: function (btn) {
        if (btn === 'ok') {
          mask.show();

          Ext.Ajax.request({
            url: Nexus.siesta.basePath + '/analytics/events/export',
            method: 'POST',
            suppressStatus: true,

            scope: me,
            callback: function() {
              mask.hide()
            },
            success: function(response) {
              var obj = Ext.decode(response.responseText),
                  win = NX.create('Nexus.analytics.view.EventsZipCreated');

              win.setValues(obj);
              win.show();
            },
            failure: function (response) {
              me.showMessage('Failed to export event data: ' + me.parseExceptionMessage(response));
            }
          });
        }
      }
    });
  },

  /**
   * Submit events to Sonatype.
   *
   * @private
   */
  submitEvents: function(button) {
    var me = this,
        icons = Nexus.analytics.Icons;

    Ext.Msg.show({
      title: 'Submit events',
      msg: 'Submit analytics event data to Sonatype?',
      buttons: Ext.Msg.OKCANCEL,
      icon: icons.get('submit').variant('x32').cls,
      fn: function (btn) {
        if (btn === 'ok') {
          Ext.Ajax.request({
            url: Nexus.siesta.basePath + '/analytics/events/submit',
            method: 'POST',
            suppressStatus: true,
            success: function () {
              me.showMessage('Event data submission in progress');
            },
            failure: function (response) {
              me.showMessage('Failed to submit event data: ' + me.parseExceptionMessage(response));
            }
          });
        }
      }
    });
  },

  /**
   * Download events ZIP file.
   *
   * @private
   */
  downloadEventsZip: function(button, authTicket) {
    var win = button.up('nx-analytics-view-eventszip-created'),
        fileName = win.getValues().name;

    // encode ticket for query-parameter
    authTicket = Sonatype.utils.base64.encode(authTicket);

    // FIXME: Expose download stuff in wonderland to avoid direct dep on atlas plugin
    if (Nexus.util.DownloadHelper.downloadUrl(
        Nexus.siesta.basePath + '/atlas/support-zip/' + fileName + '?t=' + authTicket))
    {
      // if download was initiated close the window
      win.close();
    }
  }

});