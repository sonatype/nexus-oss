Class('NX.Test', {
  isa: Siesta.Test.ExtJS,

  methods: {

    do: function (callback) {
      var me = this,
          params = Array.prototype.slice.call(arguments, 1);

      return function (next) {
        callback.apply(me, params);
        next();
      }
    },

    waitForStateReceived: function (callback, scope, timeout) {
      var stateController = this.stateController();

      return this.waitFor({
        method: function () {
          return stateController.isReceiving();
        },
        callback: callback,
        scope: scope,
        timeout: timeout,
        assertionName: 'waitForStateReceived',
        description: ' receiving state from server'
      });
    },

    waitForUserToBeLoggedIn: function (shouldLogin, callback, scope, timeout) {
      var userController = this.userController();

      if (shouldLogin) {
        this.login();
      }

      return this.waitFor({
        method: function () {
          return userController.hasUser() && this.global.NX.Permissions.available();
        },
        callback: callback,
        scope: scope,
        timeout: timeout,
        assertionName: 'waitForUserToBeLoggedIn',
        description: ' user to be logged in'
      });
    },

    waitForUserToBeLoggedOut: function (shouldLogout, callback, scope, timeout) {
      var userController = this.userController();

      if (shouldLogout) {
        this.logout();
      }

      return this.waitFor({
        method: function () {
          return !userController.hasUser();
        },
        callback: callback,
        scope: scope,
        timeout: timeout,
        assertionName: 'waitForUserToBeLoggedOut',
        description: ' user to not be logged in'
      });
    },

    waitForControllerToExist: function (controller, callback, scope, timeout) {
      var me = this;

      return this.waitFor({
        method: function () {
          return Ext.isDefined(me.controllerUnguarded(controller));
        },
        callback: callback,
        scope: scope,
        timeout: timeout,
        assertionName: 'waitForControllerToExist',
        description: ' controller "' + controller + '" to exist'
      });
    },

    waitForControllerToNotExist: function (controller, callback, scope, timeout) {
      var me = this;

      return this.waitFor({
        method: function () {
          return !Ext.isDefined(me.controllerUnguarded(controller));
        },
        callback: callback,
        scope: scope,
        timeout: timeout,
        assertionName: 'waitForControllerToNotExist',
        description: ' controller "' + controller + '" to not exist'
      });
    },

    waitForBookmark: function (bookmark, callback, scope, timeout) {
      var bookmarks = this.bookmarks();
      return this.waitFor({
        method: function () {
          return bookmarks.getBookmark().getToken() === bookmark;
        },
        callback: callback,
        scope: scope,
        timeout: timeout,
        assertionName: 'waitForBookmark',
        description: ' bookmark to be set to "' + bookmark + '"'
      });
    },

    navigateTo: function (bookmark) {
      var NX = this.global.NX;

      NX.Bookmarks.navigateTo(NX.Bookmarks.fromToken(bookmark));
      this.diag('Navigate to "' + bookmark + '"');
    },

    setState: function (key, value) {
      var NX = this.global.NX;

      NX.State.setValue(key, value);
      this.diag('State "' + key + '" set to "' + value + '"');
    },

    login: function () {
      var me = this,
          NX = this.global.NX,
          loginAssertion = me.startWaiting('Logging in as admin ...', me.getSourceLine());

      NX.direct.rapture_Security.login(
          NX.util.Base64.encode('admin'),
          NX.util.Base64.encode('admin123'),
          false,
          function (response) {
            if (Ext.isDefined(response) && response.success) {
              NX.State.setUser(response.data);
              me.finalizeWaiting(loginAssertion, true, 'Logged in as admin');
            }
            else {
              me.finalizeWaiting(loginAssertion, false, 'Could not login as admin');
            }
          }
      );
    },

    logout: function () {
      var me = this,
          NX = this.global.NX,
          logoutAssertion = me.startWaiting('Logging out ...', me.getSourceLine());

      NX.direct.rapture_Security.logout(
          function (response) {
            if (Ext.isDefined(response) && response.success) {
              NX.State.setUser(undefined);
              me.finalizeWaiting(logoutAssertion, true, 'Logged out');
            }
            else {
              me.finalizeWaiting(logoutAssertion, false, 'Could not logout');
            }
          }
      );
    },

    controller: function (name) {
      var controller = this.controllerUnguarded(name);
      if (!controller) {
        t.fail('Controller "' + name + '" does not exist');
      }
      return controller
    },

    controllerUnguarded: function (name) {
      return this.global.NX.getApplication().controllers.get(name);
    },

    stateController: function () {
      return this.controller('State');
    },

    userController: function () {
      return this.controller('User');
    },

    bookmarks: function () {
      return this.global.NX.Bookmarks;
    }

  }

});