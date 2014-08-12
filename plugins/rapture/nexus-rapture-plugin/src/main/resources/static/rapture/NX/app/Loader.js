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
 * Nexus application loader.
 *
 * @since 3.0
 */
Ext.define('NX.app.Loader', {
  requires: [
    'NX.app.Application'
  ],

  mixins: {
    logAware: 'NX.LogAware'
  },

  /**
   * Discovered plugin controllers.
   *
   * @private
   */
  controllers: new Ext.util.MixedCollection(),

  /**
   * Load the application.
   *
   * @public
   */
  load: function (config) {
    var me = this;

    me.logDebug('Loading');

    // sanity check config
    if (!Ext.isArray(config.pluginConfigs)) {
      Ext.Error.raise("Invalid config property 'pluginConfigs' (expected array): " + config.pluginConfigs);
    }
    if (!Ext.isObject(config.state)) {
      Ext.Error.raise("Invalid config property: 'state' (expected object): " + config.state);
    }

    // attach initial application state
    NX.app.state = config.state;
    NX.app.debug = false;
    if (NX.global.location.search === '?debug') {
      if (NX.app.state.uiSettings.value.debugAllowed) {
        NX.app.debug = true;
        me.logDebug('Debug mode enabled');
      }
      else {
        me.logWarn('Debug mode disallowed');
      }
    }

    // apply all plugin configurations
    me.logDebug('Plugin configs: ' + config.pluginConfigs);
    Ext.each(config.pluginConfigs, function (className) {
      me.applyPluginConfig(className);
    });

    // launch the application
    var App = Ext.ClassManager.get('NX.app.Application');
    Ext.onReady(function () {
      me.logDebug('Received ready event');
      Ext.app.Application.instance = new App({
        managedControllers: me.controllers
      });
      me.logDebug('Application loaded');
      Ext.app.Application.instance.start();
      me.logDebug('Application started');
    });
  },

  /**
   * Apply plugin customizations.
   *
   * @private
   * @param className
   */
  applyPluginConfig: function (className) {
    var me = this,
        config;

    me.logDebug('Applying plugin config: ' + className);
    config = Ext.create(className);

    // find all controllers
    if (config.controllers) {
      Ext.each(config.controllers, function (config) {
        var controller = me.defineController(config);
        me.controllers.add(controller);
      });
    }

    // resolve all controllers
    if (me.controllers) {

      me.logDebug(me.controllers.getCount() + ' plugin controllers:');
      me.controllers.each(function (controller) {
        // attach full class-name to controller defs
        controller.type = Ext.app.Controller.getFullName(controller.id, 'controller', 'NX').absoluteName;
        me.logDebug('  + ' + controller.id + (controller.id !== controller.type ? ' (' + controller.type + ')' : ''));

        // require all controllers, to avoid warning in console
        Ext.require(controller.type);
      });
    }
  },

  /**
   * Define controller from configuration.
   *
   * @private
   * @param config
   * @return        controller configuration
   */
  defineController: function(config) {
    var me = this;

    // simple definition of controller class-name
    if (Ext.isString(config)) {
      return {
        id: config,
        active: NX.app.Application.defaultActivation
      };
    }

    // advanced configuration of controller
    if (!Ext.isObject(config)) {
      Ext.Error.raise('Invalid controller definition: ' + config);
    }

    // require 'id' parameter
    if (!Ext.isString(config.id) || config.id.length === 0) {
      Ext.Error.raise('Invalid controller definition: ' + config + '; required property: id');
    }

    // require or normalize 'active' parameter
    if (Ext.isBoolean(config.active)) {
      var flag = config.active;
      config.active = function() {
        return flag;
      }
    }
    else if (!Ext.isFunction(config.active)) {
      Ext.Error.raise('Invalid controller definition: ' + config.id + '; required property: active (boolean or function)');
    }

    return config;
  }
});