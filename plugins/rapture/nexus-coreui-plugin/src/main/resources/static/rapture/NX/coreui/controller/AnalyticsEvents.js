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
 * Analytics Events controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.AnalyticsEvents', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.util.Url',
    'NX.util.DownloadHelper'
  ],

  stores: [
    'AnalyticsEvent'
  ],
  views: [
    'analytics.AnalyticsEventList',
    'analytics.EventsZipCreated'
  ],
  refs: [
    {
      ref: 'list',
      selector: 'nx-coreui-analytics-event-list'
    }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'analyticsevent-default': {
        file: 'transmit.png',
        variants: ['x16', 'x32']
      },
      'analyticsevent-rest': {
        file: 'transmit.png',
        variants: ['x16', 'x32']
      },
      'analyticsevent-ui': {
        file: 'transmit_blue.png',
        variants: ['x16', 'x32']
      },
      'analyticsevent-zip': {
        file: 'file_extension_zip.png',
        variants: ['x16', 'x32']
      }
    });

    me.getApplication().getFeaturesController().registerFeature({
      mode: 'admin',
      path: '/Support/Analytics/Events',
      description: 'View recorded analytics events',
      view: { xtype: 'nx-coreui-analytics-event-list' },
      iconConfig: {
        file: 'transmit.png',
        variants: ['x16', 'x32']
      },
      visible: function () {
        var analyticsState = NX.State.getValue('analytics');
        return NX.Permissions.check('nexus:analytics', 'read') && analyticsState && analyticsState.enabled;
      }
    });

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.load
        }
      },
      component: {
        'nx-coreui-analytics-event-list': {
          afterrender: me.load
        },
        'nx-coreui-analytics-event-list button[action=clear]': {
          click: me.clear,
          afterrender: me.bindClearButton
        },
        'nx-coreui-analytics-event-list button[action=export]': {
          click: me.exportEvents
        },
        'nx-coreui-analytics-event-list button[action=submit]': {
          click: me.submit,
          afterrender: me.bindSubmitButton
        },
        'nx-coreui-analytics-eventszipcreated button[action=download]': {
          click: me.download
        }
      }
    });
  },

  /**
   * @private
   * Load analytics event store.
   */
  load: function () {
    var me = this,
        list = me.getList();

    if (list) {
      list.getStore().load();
    }
  },

  /**
   * @private
   * Clear all events.
   */
  clear: function () {
    var me = this;

    NX.Dialogs.askConfirmation('Clear events', 'Clear analytics event data?', function () {
      me.getList().getEl().mask('Clearing event data...');
      NX.direct.analytics_Events.clear(function (response) {
        me.getList().getEl().unmask();
        me.load();
        if (Ext.isObject(response) && response.success) {
          NX.Messages.add({ text: 'Event data has been cleared', type: 'success' });
        }
      });
    });
  },

  /**
   * @private
   * Export and download events.
   */
  exportEvents: function () {
    var me = this;

    NX.Dialogs.askConfirmation('Export events',
        'Export and download analytics event data?<br/>No data will be sent to Sonatype.', function () {
          me.getList().getEl().mask('Exporting event data...');
          NX.direct.analytics_Events.export_(function (response) {
            me.getList().getEl().unmask();
            if (Ext.isObject(response) && response.success) {
              Ext.widget('nx-coreui-analytics-eventszipcreated').setValues(response.data);
            }
          });
        });
  },

  /**
   * @private
   * Download events file.
   */
  download: function (button) {
    var win = button.up('window'),
        fileName = win.down('form').getValues().name;

    NX.Security.doWithAuthenticationToken(
        'Downloading analytics events requires validation of your credentials.',
        {
          success: function (authToken) {
            NX.util.DownloadHelper.downloadUrl(NX.util.Url.urlOf(
                'service/siesta/wonderland/download/' + fileName + '?t=' + NX.util.Base64.encode(authToken)
            ));
            win.close();
          }
        }
    );
  },

  /**
   * @private
   * Submit events to Sonatype.
   */
  submit: function () {
    NX.Dialogs.askConfirmation('Submit events', 'Submit analytics event data to Sonatype?', function () {
      NX.Security.doWithAuthenticationToken(
          'Submit analytics event data to Sonatype validation of your credentials.',
          {
            success: function (authToken) {
              NX.direct.analytics_Events.submit(authToken, function (response) {
                if (Ext.isObject(response) && response.success) {
                  NX.Messages.add({ text: 'Event data submission in progress', type: 'success' });
                }
              });
            }
          }
      );
    });
  },

  /**
   * @private
   * Enable 'Clear' when user has 'delete' permission.
   */
  bindClearButton: function (button) {
    button.mon(
        NX.Conditions.isPermitted('nexus:analytics', 'delete'),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  /**
   * @private
   * Enable 'Submit' when user has 'create' permission.
   */
  bindSubmitButton: function (button) {
    button.mon(
        NX.Conditions.isPermitted('nexus:analytics', 'create'),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  }

});