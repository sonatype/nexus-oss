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
 * UI Session Timeout controller.
 *
 * @since 3.0
 */
Ext.define('NX.controller.UiSessionTimeout', {
  extend: 'Ext.app.Controller',
  requires: [
    'Ext.ux.ActivityMonitor',
    'NX.Messages',
    'NX.Security',
    'NX.State'
  ],
  mixins: {
    logAware: 'NX.LogAware'
  },

  views: [
    'ExpireSession'
  ],

  SECONDS_TO_EXPIRE: 30,

  activityMonitor: undefined,

  expirationTicker: undefined,

  init: function () {
    var me = this;

    me.listen({
      controller: {
        '#State': {
          userchanged: me.onUserChanged,
          uisettingschanged: me.onUiSettingsChanged
        }
      },
      component: {
        'nx-expire-session': {
          afterrender: me.startTicking
        },
        'nx-expire-session button[action=cancel]': {
          click: me.stopTicking
        }
      }
    });
  },

  onLaunch: function () {
    var me = this,
        user = NX.State.getUser(),
        uiSettings = NX.State.getValue('uiSettings') || {};

    me.setupTimeout(user ? uiSettings['sessionTimeout'] : undefined);
  },

  /**
   * @private
   */
  onUserChanged: function (user) {
    var me = this,
        uiSettings = NX.State.getValue('uiSettings') || {};

    me.setupTimeout(user ? uiSettings['sessionTimeout'] : undefined);
  },

  /**
   * @private
   * Reset UI session timeout when uiSettings.sessionTimeout changes.
   * @param {Object} uiSettings
   * @param {Number} uiSettings.sessionTimeout
   * @param {Object} oldUiSettings
   * @param {Number} oldUiSettings.sessionTimeout
   */
  onUiSettingsChanged: function (uiSettings, oldUiSettings) {
    var me = this,
        user = NX.State.getUser();

    uiSettings = uiSettings || {};
    oldUiSettings = oldUiSettings || {};

    if (uiSettings.sessionTimeout !== oldUiSettings.sessionTimeout) {
      me.setupTimeout(user ? uiSettings.sessionTimeout : undefined);
    }
  },

  /**
   * @private
   */
  setupTimeout: function (sessionTimeout) {
    var me = this;

    me.cancelTimeout();

    if (sessionTimeout > 0) {
      me.logDebug('Session expiration enabled for ' + sessionTimeout + ' minutes');
      me.activityMonitor = Ext.create('Ext.ux.ActivityMonitor', {
        interval: 1000, // check every second,
        maxInactive: ((sessionTimeout * 60) - me.SECONDS_TO_EXPIRE) * 1000,
        isInactive: Ext.bind(me.showExpirationWindow, me)
      });
      me.activityMonitor.start();
    }
  },

  /**
   * @private
   */
  cancelTimeout: function () {
    var me = this;

    if (me.activityMonitor) {
      me.activityMonitor.stop();
      delete me.activityMonitor;
      me.logDebug('Session expiration disabled');
    }

    if (me.expirationTicker) {
      me.expirationTicker.destroy();
      delete me.expirationTicker;
    }
  },

  /**
   * @private
   */
  showExpirationWindow: function () {
    var me = this;

    NX.Messages.add({text: 'Session is about to expire', type: 'warning' });
    me.getExpireSessionView().create();
  },

  /**
   * @private
   */
  startTicking: function (win) {
    var me = this;

    me.expirationTicker = Ext.util.TaskManager.newTask({
      run: function (count) {
        win.down('label').setText('Session will expire in ' + (me.SECONDS_TO_EXPIRE - count) + ' seconds');
        if (count === me.SECONDS_TO_EXPIRE) {
          win.close();
          NX.Messages.add({
            text: 'Session expired after being inactive for '
                + NX.State.getValue('uiSettings')['sessionTimeout']
                + ' minutes',
            type: 'warning'
          });
          NX.Security.logout();
        }
      },
      interval: 1000,
      repeat: me.SECONDS_TO_EXPIRE
    });
    me.expirationTicker.start();
  },

  /**
   * @private
   */
  stopTicking: function (button) {
    var me = this,
        win = button.up('window');

    if (me.expirationTicker) {
      me.expirationTicker.destroy();
      delete me.expirationTicker;
    }
    if (win) {
      win.close();
    }
    me.activityMonitor.start();
  }

});