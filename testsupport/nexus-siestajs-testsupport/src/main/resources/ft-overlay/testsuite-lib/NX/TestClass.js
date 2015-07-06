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
 * NX specific Siesta test-class extensions.
 */
Class('NX.TestClass', {
  isa: Siesta.Test.ExtJS,

  has: {
    /**
     * Copied from {@link NX.TestHarness#waitForNxAppReady}.
     */
    waitForNxAppReady: undefined,

    /**
     * Copied from {@link NX.TestHarness#describeTimeout}.
     */
    describeTimeout: undefined,

    /**
     * Copied from {@link NX.TestHarness#itTimeout}.
     */
    itTimeout: undefined
  },

  override: {
    /**
     * Custom isReady() to cope with rapture application bootstrap.
     *
     * @override
     */
    isReady: function () {
      var result = this.SUPERARG(arguments);

      if (!result.ready) {
        // super test framework is not ready
        return result;
      }

      // maybe wait for NX.application to become ready
      if (this.waitForNxAppReady) {
        if (this.global.NX === undefined || this.global.NX.application === undefined) {
          return {
            ready: false,
            reason: 'NX application namespace missing'
          };
        }

        console.error('NX application ready:', this.global.NX.application.ready);
        if (!this.global.NX.application.ready) {
          return {
            ready: false,
            reason: 'NX application is not ready'
          };
        }
      }

      return {
        ready: true
      };
    },

    /**
     * Customized describe() to configure saner default timeouts.
     *
     * @override
     */
    describe: function(name, code, timeout) {
      this.SUPER(name, code, timeout === undefined ? this.describeTimeout : timeout);
    },

    /**
     * Customized it() to configure saner default timeouts.
     *
     * @override
     */
    it: function(name, code, timeout) {
      this.SUPER(name, code, timeout === undefined ? this.itTimeout : timeout);
    },

    /**
     * @override
     */
    processSubTestConfig: function() {
      var me = this,
          config = this.SUPERARG(arguments);

      // sub-tests should not wait for NX app ready
      config.waitForNxAppReady = false;

      // Propagate configuration customization to sub-tests
      Joose.A.each([
        'describeTimeout',
        'itTimeout'
      ], function (name) {
        config[name] = me[name];
      });

      return config;
    }
  },

  methods: {
    do: function (callback) {
      var me = this,
          params = Array.prototype.slice.call(arguments, 1);

      return function (next) {
        callback.apply(me, params);
        next();
      }
    },

    waitForSessionToBeInvalidated: function (callback, scope, timeout) {
      var me = this, invalidated = false, invalidateAndRefresh = function() {
        invalidated = true;
        me.stateController().refreshNow();  
      };
      Ext.Ajax.request({
        url: '/service/rapture/session',
        method: 'DELETE',
        success: invalidateAndRefresh,
        failure: function(response) {
          // Unfortunately with phantomjs we can't be entirely certain about the status due to https://github.com/ariya/phantomjs/issues/11195
          if (response.status === 403 || response.status === 0) {
            Ext.util.Cookies.clear('NXSESSIONID'); //weren't logged in in the first place, clear any remaining session cookie just in case
            invalidateAndRefresh();
          }
        },
        scope: this
      });
      return me.waitFor({
        method: function() {
          return invalidated;
        },
        callback: callback,
        scope: scope,
        timeout: timeout,
        assertionName: 'waitForSessionToBeInvalidated',
        description: ' session to be invalidated'
      });
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

    waitForUserToBeSignedIn: function(shouldSignIn, callback, scope, timeout) {
      var btn = this.cq1('nx-header-signin');
      if (btn && !btn.isHidden()) {
        this.clickComponentQuery('nx-header-signin', Ext.emptyFn);
        this.chain(
            { type: 'admin[TAB]' },
            { type: 'admin123' },
            { type: '[ENTER]' }
        );
      }
    },

    waitForUserToBeSignedOut: function(shouldSignOut, callback, scope, timeout) {
      var btn = this.cq1('nx-header-signout');
      if (btn && !btn.isHidden()) {
        this.click(btn);
      }
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

    selectAndType: function(text, target) {
      return [
        function(next) {
          this.selectText(target);
          next();
        },
        { type: text, target: target },
      ];
    },

    signIn: function () {
      return [
        { click: '>>nx-header-signin' },
        function(next) {
          this.waitForAnimations(next);
        },
        { type: 'admin[TAB]', target: this.cq1('#username') },
        { type: 'admin123', target: this.cq1('#password') },
        { type: '[ENTER]' }
      ];
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
    },
    
    openPageAsAdmin: function(bookmark) {
      return [
        // invalidate session
        {waitFor: 'sessionToBeInvalidated'},
        // and sign-in as admin
        this.signIn(),
        // go to bookmark
        function (next) {
          this.navigateTo(bookmark);
          this.waitForAnimations(next);
        }
      ]   
    },

    /**
     * Generate an id based on the present time 
     * @param {String} prefix optional prefix to apply to the generated id
     */
    uniqueId: function(prefix) {
      if(prefix) {
        return prefix + new Date().getTime();
      }  
      return 'Test-' + new Date().getTime();
    },

    /**
     * Convenience method for waiting on the load of a store.
     * @param {String} cq component query for the component whose store should be loaded
     */
    waitForStore: function(cq) {
      return {
        waitFor: 'storesToLoad', args: function() {
          return this.cq1(cq).getStore();
        }
      }
    }
  }
});
