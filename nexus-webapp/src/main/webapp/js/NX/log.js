/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

/*global define,Ext,NX,Nexus */

define('NX/log', ['NX/base'], function() {
  Ext.ns('NX');

  /**
   * Logging helper.
   *
   * @singleton
   */
  NX.log = (function () {
      var logger;

      // FIXME: Really would like a better/more-powerful logging API here
      // FIXME: Including the ability to remote back to the server to capture UI events
      //
      // http://log4javascript.org/index.html
      // http://www.gscottolson.com/blackbirdjs
      // http://log4js.berlios.de

      logger = {
          enabled: true,

          levels: {
              trace:  false,
              debug:  false,
              group:  false,
              info:   true,
              warn:   true,
              error:  true
          },

          isEnabled: function(level) {
              return this.enabled && (this.levels[level] || NX.global.location.search === '?debug');
          }

          // FIXME: Add ExtJS 4.x compatible(ish) log() helper
          //log: function(/*[options],[message]*/) {
          //}
      };

      function safeProxy(target, name) {
          var log;
          if (Ext.isDefined(target)) {
            if (Ext.isFunction(target[name])) {
              log = function() {
                  if (log.enabled) {
                      target[name].apply(target, arguments);
                  }
              };
            } else if (Ext.isDefined(target[name])) {
              log = function() {
                if (logger.enabled) {
                  // "IE mode", pretty messed up
                  var args = [];
                  Ext.each(arguments, function(item) {
                    args.push(item);
                  });
                  target[name](args.join(' '));
                }
              };
            } else if (Ext.isDefined(target.log)) {
              log = safeProxy(target, 'log');
            } else {
              log = Ext.emptyFn;
            }
          }

          return log;
      }

      function levelAwareProxy(target, name) {
        var log;
        if (Ext.isDefined(target)) {
          if (Ext.isFunction(target[name])) {
            log = function() {
              if (logger.isEnabled(name)) {
                target[name].apply(target, arguments);
              }
            };
          } else if (Ext.isDefined(target[name])) {
            log = function() {
              if (logger.isEnabled(name)) {
                // "IE mode", pretty messed up
                var args = [];
                Ext.each(arguments, function(item) {
                  args.push(item);
                });
                target[name](args.join(' '));
              }
            };
          } else if (Ext.isDefined(target.log)) {
            log = levelAwareProxy(target, 'log');
          } else {
            log = Ext.emptyFn;
          }
        }

        return log;
      }

      Ext.each([
          'trace',
          //'log', skipping; as we may want to make an Ext.log compatible method
          'debug',
          'group',
          'info',
          'warn',
          'error'
      ], function (name) {
          logger[name] = levelAwareProxy(console, name);
      });

      Ext.each([
          'groupEnd'
      ], function (name) {
          logger[name] = safeProxy(console, name);
      });

      return logger;
  }());

  NX.log.debug('Logging initialized');

  // Compatibility
  Ext.ns('Nexus');
  Nexus.Log = NX.log;
  Nexus.log = NX.log.debug;

});
