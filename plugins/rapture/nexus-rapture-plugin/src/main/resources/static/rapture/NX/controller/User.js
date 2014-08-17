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
 * Logged in user controller.
 *
 * @since 3.0
 */
Ext.define('NX.controller.User', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.util.Base64',
    'NX.Messages',
    'NX.State'
  ],
  mixins: {
    logAware: 'NX.LogAware'
  },

  views: [
    'header.Login',
    'header.Logout',
    'header.User',
    'Authenticate',
    'Login'
  ],

  refs: [
    {
      ref: 'loginButton',
      selector: 'nx-header-login'
    },
    {
      ref: 'logoutButton',
      selector: 'nx-header-logout'
    },
    {
      ref: 'userButton',
      selector: 'nx-header-user-mode'
    },
    {
      ref: 'login',
      selector: 'nx-login'
    },
    {
      ref: 'authenticate',
      selector: 'nx-authenticate'
    }
  ],

  init: function () {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'authenticate': {
        file: 'lock.png',
        variants: ['x16', 'x32']
      }
    });

    me.listen({
      controller: {
        '#State': {
          userchanged: me.onUserChanged
        }
      },
      component: {
        'nx-header-panel': {
          afterrender: me.manageButtons
        },
        'nx-header-login': {
          click: me.showLoginWindow
        },
        'nx-header-logout': {
          click: me.logout
        },
        'nx-login button[action=login]': {
          click: me.login
        },
        'nx-authenticate button[action=authenticate]': {
          click: me.doAuthenticateAction
        }
      }
    });

    me.addEvents(
        /**
         * @event login
         * Fires when a user had been successfully logged in.
         * @param {Object} user
         */
        'login',

        /**
         * @event logout
         * Fires when a user had been successfully logged out.
         */
        'logout'
    );
  },

  /**
   * @private
   */
  onUserChanged: function (user, oldUser) {
    var me = this;

    if (user && !oldUser) {
      NX.Messages.add({text: 'User signed in: ' + user.id, type: 'default' });
      me.fireEvent('login', user);
    }
    else if (!user && oldUser) {
      NX.Messages.add({text: 'User signed out', type: 'default' });
      me.fireEvent('logout');
    }

    me.manageButtons();
  },

  /**
   * Returns true if there is a logged in user.
   *
   * @public
   * @return {boolean}
   */
  hasUser: function () {
    return Ext.isDefined(NX.State.getUser());
  },

  /**
   * @public
   * Shows login or authentication window based on the fact that we have an user or not.
   * @param {String} [message] Message to be shown in authentication window
   * @param {Object} [options] TODO
   */
  askToAuthenticate: function (message, options) {
    var me = this;

    if (me.hasUser()) {
      me.showAuthenticateWindow(message, Ext.apply(options || {}, { authenticateAction: me.authenticate }));
    }
    else {
      me.showLoginWindow(options);
    }
  },

  /**
   * @public
   * Shows authentication window in order to retrieve an authentication token.
   * @param {String} [message] Message to be shown in authentication window
   * @param {Object} [options] TODO
   */
  doWithAuthenticationToken: function (message, options) {
    var me = this;

    me.showAuthenticateWindow(message,
        Ext.apply(options || {}, { authenticateAction: me.retrieveAuthenticationToken })
    );
  },

  /**
   * @private
   * Shows login window.
   * @param {Object} [options] TODO
   */
  showLoginWindow: function (options) {
    var me = this;

    if (!me.getLogin()) {
      me.getLoginView().create({ options: options });
    }
  },

  /**
   * @private
   * Shows authenticate window.
   * @param {String} [message] Message to be shown in authentication window
   * @param {Object} [options] TODO
   */
  showAuthenticateWindow: function (message, options) {
    var me = this,
        user = NX.State.getUser(),
        win;

    if (!me.getAuthenticate()) {
      win = me.getAuthenticateView().create({ message: message, options: options });
      if (me.hasUser()) {
        win.down('form').getForm().setValues({ username: user.id });
        win.down('#password').focus();
      }
    }
  },

  /**
   * @private
   */
  login: function (button) {
    var me = this,
        win = button.up('window'),
        form = button.up('form'),
        values = form.getValues(),
        userName = NX.util.Base64.encode(values.username),
        userPass = NX.util.Base64.encode(values.password);

    win.getEl().mask('Logging you in...');

    me.logDebug('Logging you in...');

    NX.direct.rapture_Security.login(userName, userPass, values.remember === 'on', function (response) {
      win.getEl().unmask();
      if (Ext.isObject(response) && response.success) {
        NX.State.setUser(response.data);
        win.close();
        if (win.options && Ext.isFunction(win.options.success)) {
          win.options.success.call(win.options.scope, win.options);
        }
      }
    });
  },

  /**
   * @private
   */
  doAuthenticateAction: function (button) {
    var me = this,
        win = button.up('window');

    if (win.options && Ext.isFunction(win.options.authenticateAction)) {
      win.options.authenticateAction.call(me, button);
    }
  },

  /**
   * @private
   */
  authenticate: function (button) {
    var me = this,
        win = button.up('window'),
        form = button.up('form'),
        user = NX.State.getUser(),
        values = Ext.applyIf(form.getValues(), { username: user ? user.id : undefined }),
        userName = NX.util.Base64.encode(values.username),
        userPass = NX.util.Base64.encode(values.password);

    win.getEl().mask('Authenticate...');

    me.logDebug('Authenticate...');

    NX.direct.rapture_Security.authenticate(userName, userPass, function (response) {
      win.getEl().unmask();
      if (Ext.isObject(response) && response.success) {
        NX.State.setUser(response.data);
        win.close();
        if (win.options && Ext.isFunction(win.options.success)) {
          win.options.success.call(win.options.scope, win.options);
        }
      }
    });
  },

  /**
   * @private
   */
  retrieveAuthenticationToken: function (button) {
    var me = this,
        win = button.up('window'),
        form = button.up('form'),
        user = NX.State.getUser(),
        values = Ext.applyIf(form.getValues(), { username: user ? user.id : undefined }),
        userName = NX.util.Base64.encode(values.username),
        userPass = NX.util.Base64.encode(values.password);

    win.getEl().mask('Retrieving authentication token...');

    me.logDebug('Retrieving authentication token...');

    NX.direct.rapture_Security.authenticationToken(userName, userPass, function (response) {
      win.getEl().unmask();
      if (Ext.isObject(response) && response.success) {
        win.close();
        if (win.options && Ext.isFunction(win.options.success)) {
          win.options.success.call(win.options.scope, response.data, win.options);
        }
      }
    });
  },

  /**
   * @public
   */
  logout: function () {
    var me = this;

    me.logDebug('Logout...');

    NX.direct.rapture_Security.logout(function (response) {
      if (Ext.isObject(response) && response.success) {
        NX.State.setUser(undefined);
      }
    });
  },

  manageButtons: function () {
    var me = this,
        user = NX.State.getUser(),
        loginButton = me.getLoginButton(),
        logoutButton = me.getLogoutButton(),
        userButton = me.getUserButton();

    if (loginButton) {
      if (user) {
        loginButton.hide();
        userButton.setText(user.id);
        userButton.show();
        logoutButton.show();
      }
      else {
        loginButton.show();
        userButton.setText('Not logged in');
        userButton.hide();
        logoutButton.hide();
      }
    }
  }

});