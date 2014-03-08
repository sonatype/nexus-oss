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
Ext.define('NX.app.Application', {
  extend: 'Ext.app.Application',

  requires: [
    'Ext.Error',
    'Ext.Direct',
    'Ext.state.Manager',
    'Ext.state.CookieProvider',
    'Ext.state.LocalStorageProvider',
    'Ext.util.LocalStorage',
    'NX.view.Viewport',
    'NX.util.Url',
    'NX.State',

    // Ext overrides
    'NX.ext.form.action.DirectSubmit',
    'NX.ext.form.field.Base',
    'NX.ext.form.field.Checkbox',
    'NX.ext.form.field.Display',
    'NX.ext.form.FieldContainer',

    // require custom extensions so we don't need to requirement explicitly everywhere
    'NX.ext.grid.IconColumn',
    'NX.ext.SearchBox'
  ],

  uses: [
    'NX.ext.grid.plugin.FilterBox',
    'NX.ext.grid.plugin.Filtering',
    'NX.ext.form.OptionalFieldSet',
    'NX.ext.form.field.ClearableComboBox',
    'NX.ext.form.field.Email',
    'NX.ext.form.field.Password',
    'NX.ext.form.field.Url',
    'NX.ext.form.field.ValueSet',
    'NX.ext.grid.column.Timestamp',
    'NX.Conditions'
  ],

  mixins: {
    logAware: 'NX.LogAware'
  },

  name: 'NX',

  /**
   * Store application instance in "NX.application".
   */
  appProperty: 'application',

  /**
   * Relative to /rapture.html
   */
  appFolder: 'static/rapture/NX',

  paths: {
    'Ext.ux': 'static/rapture/Ext/ux'
  },

  /**
   * Always active controllers.
   */
  controllers: [
    'State',
    'Bookmarking',
    'ExtDirect',
    'Features',
    'Icon',
    'Message',
    'Permissions'
  ],

  /**
   * @private
   * {@link Ext.util.MixedCollection} containing managed controllers configurations
   */
  managedControllers: undefined,

  statics: {
    alwaysActive: function () {
      return true;
    },
    defaultActivation: function () {
      return NX.app.Application.supportedBrowser() && NX.app.Application.licensed();
    },
    supportedBrowser: function () {
      return NX.State.isBrowserSupported();
    },
    unsupportedBrowser: function () {
      return !NX.app.Application.supportedBrowser();
    },
    licensed: function () {
      return !NX.State.requiresLicense() || NX.State.isLicenseInstalled();
    },
    unlicensed: function () {
      return !NX.app.Application.licensed();
    },
    debugMode: function () {
      return NX.State.getValue('debug') === true;
    }
  },

  /**
   * @override
   * @param {NX.app.Application} app this class
   */
  init: function (app) {
    var me = this;

    me.logDebug('Initializing application ...');

    // Configure blank image URL
    Ext.BLANK_IMAGE_URL = NX.util.Url.baseUrl + '/static/rapture/resources/images/s.gif';

    app.initErrorHandler();
    app.initDirect();
    app.initState();
  },

  /**
   * @private
   * Hook into browser error handling (in order to log them).
   */
  initErrorHandler: function () {
    var me = this,
        originalOnError = window.onerror;

    // FIXME: This needs further refinement, seems like javascript errors are lost in Firefox (but show up fine in Chrome)

    // pass unhandled errors to application error handler
    Ext.Error.handle = function (err) {
      me.handleError(err);
    };

    // FIXME: This will catch more errors, but duplicates messages for ext errors
    // FIXME: Without this however some javascript errors will go unhandled
    window.onerror = function (msg, url, line) {
      me.handleError({ msg: msg + ' (' + url + ':' + line + ')' });

      // maybe delegate to original window.onerror handler
      if (originalOnError) {
        originalOnError(msg, url, line);
      }
    };

    me.logDebug('Configured error handling');
  },

  /**
   * @private
   * Log catched error.
   */
  handleError: function (error) {
    var me = this;
    NX.Messages.add({
      type: 'danger',
      text: me.errorAsString(error)
    });
  },

  /**
   * Customize error to-string handling.
   *
   * Ext.Error.toString() assumes instance, but raise(String) makes anonymous object.
   *
   * @private
   */
  errorAsString: function (error) {
    var className = error.sourceClass ? error.sourceClass : '',
        methodName = error.sourceMethod ? '.' + error.sourceMethod + '(): ' : '',
        msg = error.msg || '(No description provided)';
    return className + methodName + msg;
  },

  /**
   * @private
   * Initialize Ex.Direct remote providers.
   */
  initDirect: function () {
    var me = this;

    Ext.Direct.addProvider(NX.direct.api.REMOTING_API);
    me.logDebug('Configured Ext.Direct');
  },

  /**
   * @private
   * Initialize state manager.
   */
  initState: function () {
    var me = this, provider;

    // prefer local storage if its supported
    if (Ext.util.LocalStorage.supported) {
      provider = Ext.create('Ext.state.LocalStorageProvider');
      me.logDebug('Configured state provider: local');
    }
    else {
      // FIXME: We may want to either not support state, or implement remoting to used
      // FIXME: The remote session of the user to store state
      // FIXME: Cookie impl will create too many cookies and could cause lots of weird things to break
      provider = Ext.create('Ext.state.CookieProvider');
      me.logDebug('Configured state provider: cookie');
    }

    // HACK: for debugging
    //provider.on('statechange', function (provider, key, value, opts) {
    //  me.logDebug('State changed: ' + key + '=' + value);
    //});

    Ext.state.Manager.setProvider(provider);
  },

  /**
   * @public
   * Starts the application.
   */
  start: function () {
    var me = this;
    me.logDebug('Starting ...');

    me.managedControllers = NX.app.pluginConfig.managedControllers;

    Ext.create('NX.view.Viewport');

    me.syncManagedControllers();
    me.listen({
      controller: {
        '#State': {
          changed: me.syncManagedControllers
        }
      }
    });

    // hide the loading mask after we have loaded
    var hideMask = function () {
      Ext.get('loading').remove();
      Ext.fly('loading-mask').animate({ opacity: 0, remove: true });
    };

    // FIXME: Need a better way to know when the UI is actually rendered so we can hide the mask
    // HACK: for now increasing delay slightly to cope with longer loading times
    Ext.defer(hideMask, 500);
  },

  /**
   * Create / Destroy managed controllers based on their active status.
   */
  syncManagedControllers: function () {
    var me = this,
        ref, initializedControllers = [],
        changes = false;

    me.logDebug('Refreshing controllers');

    // destroy all controllers that are become inactive
    me.managedControllers.eachKey(function (key) {
      ref = me.managedControllers.get(key);
      if (!ref.active()) {
        if (ref.controller) {
          changes = true;
          me.logDebug('Destroying controller: ' + key);
          ref.controller.eventbus.unlisten(ref.controller.id);
          if (Ext.isFunction(ref.controller.onDestroy)) {
            ref.controller.onDestroy();
          }
          me.controllers.remove(ref.controller);
          ref.controller.clearManagedListeners();
          if (Ext.isFunction(ref.controller.destroy)) {
            ref.controller.destroy();
          }
          delete ref.controller;
        }
      }
    });

    // create & init all controllers that become active
    me.managedControllers.eachKey(function (key) {
      ref = me.managedControllers.get(key);
      if (ref.active()) {
        if (!ref.controller) {
          changes = true;
          me.logDebug('Initializing controller: ' + key);
          ref.controller = me.getController(key);
          initializedControllers.push(ref.controller);
        }
      }
    });

    // launch any initialized controller
    Ext.each(initializedControllers, function (controller) {
      controller.onLaunch(me);
    });
    // finish init on any initialized controller
    Ext.each(initializedControllers, function (controller) {
      controller.finishInit(me);
    });

    if (changes) {
      // TODO shall we do this on each refresh?
      me.getIconController().installStylesheet();
    }
  }

}, function () {
  var managedControllers = new Ext.util.MixedCollection(),
      requires = [],
      custom = {
        namespaces: [],
        controllers: []
      },
      keys = Object.keys(custom),
      parseFunction = function (fn) {
        if (Ext.isBoolean(fn)) {
          fn = function () {
            return fn;
          }
        }
        else if (Ext.isString(fn)) {
          var parts = fn.split('.'),
              i = 0,
              len = parts.length,
              current = Ext.global;

          while (current && i < len) {
            current = current[parts[i]];
            ++i;
          }

          fn = Ext.isFunction(current) ? current : null;
        }
        return fn || null;
      },
      pluginConfig;

  NX.Log.debug('[NX.app.Application]', 'Processing plugins for customizations: ' + keys);

  Ext.each(NX.app.pluginConfigClassNames, function (className) {
    NX.Log.debug('[NX.app.Application]', 'Processing ' + className);

    pluginConfig = Ext.create(className);

    // Detect customizations, these are simply fields defined on the plugin object
    // supported types are Array and String only
    Ext.each(keys, function (key) {
      var value = pluginConfig[key];
      if (value) {
        NX.Log.debug('[NX.app.Application]', ' |-' + key + ': ' + (Ext.isArray(value) ? value.join(', ') : value));
        if (Ext.isArray(value) || Ext.isString(value)) {
          if ('controllers' === key) {
            Ext.each(Ext.Array.from(value), function (controller) {
              if (Ext.isString(controller)) {
                custom[key].push(controller);
                managedControllers.add({
                  id: controller,
                  active: NX.app.Application.defaultActivation
                })
              }
              else if (Ext.isObject(controller) && Ext.isString(controller.id) && controller.id.length > 0) {
                custom[key].push(controller.id);
                managedControllers.add(Ext.apply(controller, { active: parseFunction(controller.active) }));
                if (!Ext.isFunction(managedControllers.get(controller.id).active)) {
                  Ext.Error.raise(
                      'Invalid customization; class: ' + className + ', property: controllers, value: ' +
                          controller.id
                          + ' active: must be a function that returns a boolean, a boolean'
                          + ' or a string reference to the fully qualified name of the function'
                  );
                }
              }
              else {
                Ext.Error.raise(
                    'Invalid customization; class: ' + className + ', property: controllers, value: ' + controller
                );
              }
            });
          }
          else {
            custom[key] = custom[key].concat(Ext.Array.from(value));
          }
        }
        else {
          Ext.Error.raise('Invalid customization; class: ' + className + ', property: ' + key);
        }
      }
    });

    pluginConfig.destroy();
  });

  // Have to manually add namespaces, this is done by onClassExtended in super not in parent call
  Ext.app.addNamespaces(custom.namespaces);

  // Define a class to require all controllers
  Ext.each(custom.controllers, function (name) {
    requires.push(Ext.app.Controller.getFullName(name, 'controller', 'NX').absoluteName);
  });
  NX.Log.debug('[NX.app.Application]', 'Required controllers: ' + requires.join(', '));

  NX.app.pluginConfig = Ext.apply(custom, {
    managedControllers: managedControllers,
    requires: requires
  });

  // Require all classes we need and finally start
  Ext.syncRequire(requires, function () {
    NX.getApplication().start();
  });

});