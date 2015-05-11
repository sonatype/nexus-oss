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
    'NX.State',
    'NX.I18n',
    'NX.State'
  ],
  mixins: {
    logAware: 'NX.LogAware'
  },

  views: [
    'ExpireSession'
  ],
  
  refs: [
    {
      ref: 'expireSessionWindow',
      selector: 'nx-expire-session'
    }  
  ],

  SECONDS_TO_EXPIRE: 30,

  activityMonitor: undefined,

  expirationTicker: undefined,

  init: function () {
    var me = this;

    me.listen({
      controller: {
        '#State': {
          userchanged: me.setupTimeout,
          uisettingschanged: me.onUiSettingsChanged,
          receivingchanged: me.setupTimeout
        }
      },
      component: {
        'nx-expire-session': {
          afterrender: me.startTicking
        },
        'nx-expire-session button[action=cancel]': {
          click: me.setupTimeout
        }
      }
    });
  },

  onLaunch: function () {
    this.setupTimeout();
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
    var me = this;

    uiSettings = uiSettings || {};
    oldUiSettings = oldUiSettings || {};

    if (uiSettings.sessionTimeout !== oldUiSettings.sessionTimeout) {
      me.setupTimeout();
    }
  },

  /**
   * @private
   */
  setupTimeout: function () {
    var me = this,
        user = NX.State.getUser(),
        uiSettings = NX.State.getValue('uiSettings') || {},
        sessionTimeout = user ? uiSettings['sessionTimeout'] : undefined;

    me.cancelTimeout();
    if ((user &&  NX.State.isReceiving()) && sessionTimeout > 0) {
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
    var me = this, 
        expireSessionView = me.getExpireSessionWindow();
    
    //close the window if the session has not yet expired or if the server is disconnected
    if(expireSessionView  && (!expireSessionView.sessionExpired() || !NX.State.isReceiving())) {
      expireSessionView.close();
    }
    
    if (me.activityMonitor) {
      me.activityMonitor.stop();
      delete me.activityMonitor;
      me.logDebug('Activity monitor disabled');
    }

    if (me.expirationTicker) {
      me.expirationTicker.destroy();
      delete me.expirationTicker;
      me.logDebug('Session expiration disabled');
    }
  },

  /**
   * @private
   */
  showExpirationWindow: function () {
    var me = this;

    NX.Messages.add({text: NX.I18n.get('GLOBAL_SERVER_EXPIRE_WARNING'), type: 'warning' });
    me.getExpireSessionView().create();
  },

  /**
   * @private
   */
  startTicking: function (win) {
    var me = this;

    me.expirationTicker = Ext.util.TaskManager.newTask({
      run: function (count) {
        win.down('label').setText(NX.I18n.format('GLOBAL_EXPIRE_SECONDS', me.SECONDS_TO_EXPIRE - count));
        if (count === me.SECONDS_TO_EXPIRE) {
          win.down('label').setText(NX.I18n.get('GLOBAL_EXPIRE_SIGNED_OUT'));
          win.down('button[action=close]').show();
          win.down('button[action=signin]').show();
          win.down('button[action=cancel]').hide();
          NX.Messages.add({
            text: NX.I18n.format('GLOBAL_SERVER_EXPIRED_WARNING', NX.State.getValue('uiSettings')['sessionTimeout']),
            type: 'warning'
          });
          NX.Security.signOut();
        }
      },
      interval: 1000,
      repeat: me.SECONDS_TO_EXPIRE
    });
    me.expirationTicker.start();
  }

});
