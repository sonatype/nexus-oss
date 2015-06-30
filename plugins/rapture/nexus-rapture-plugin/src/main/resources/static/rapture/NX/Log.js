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
/*global Ext*/

/**
 * Global logging helper.
 *
 * @since 3.0
 */
Ext.define('NX.Log', {
  singleton: true,

  /**
   * @private
   */
  console: undefined,

  /**
   * Set to true to disable all application logging.
   *
   * NOTE: all logging is already disabled when using mode=prod sources; logging calls are stripped out.
   *
   * @public
   * @property {Boolean}
   */
  disable: false,

  /**
   * Set to true to enable trace logging.
   *
   * @public
   * @property {Boolean}
   */
  traceEnabled: false,

  /**
   * Set to false to disable debug logging.
   *
   * @public
   * @property {Boolean}
   */
  debugEnabled: true,

  /**
   * Set up the logging environment.
   */
  constructor: function () {
    //<if debug>
    this.console = NX.global.console || {};

    // apply default empty functions to console if missing
    Ext.applyIf(this.console, {
      log: Ext.emptyFn,
      info: Ext.emptyFn,
      warn: Ext.emptyFn,
      error: Ext.emptyFn
    });

    // use ?debug to enable
    this.debugEnabled = NX.global.location.href.search("[?&]debug") > -1;

    // use ?debug&trace to enable
    this.traceEnabled = NX.global.location.href.search("[?&]trace") > -1;
    //</if>
  },

  /**
   * @public
   * @param {String} level
   * @param {Array} args
   */
  log: function (level, args) {
    //<if debug>
    if (this.disable) {
      return;
    }

    var c = this.console;
    switch (level) {
      case 'trace':
        if (this.traceEnabled) {
          c.log.apply(c, args);
        }
        break;

      case 'debug':
        if (this.debugEnabled) {
          c.log.apply(c, args);
        }
        break;

      case 'info':
        c.info.apply(c, args);
        break;

      case 'warn':
        c.warn.apply(c, args);
        break;

      case 'error':
        c.error.apply(c, args);
        break;
    }
    //</if>
  },

  /**
   * @public
   */
  trace: function() {
    //<if debug>
    this.log('trace', Array.prototype.slice.call(arguments));
    //</if>
  },

  /**
   * @public
   */
  debug: function () {
    //<if debug>
    this.log('debug', Array.prototype.slice.call(arguments));
    //</if>
  },

  /**
   * @public
   */
  info: function () {
    //<if debug>
    this.log('info', Array.prototype.slice.call(arguments));
    //</if>
  },

  /**
   * @public
   */
  warn: function () {
    //<if debug>
    this.log('warn', Array.prototype.slice.call(arguments));
    //</if>
  },

  /**
   * @public
   */
  error: function () {
    //<if debug>
    this.log('error', Array.prototype.slice.call(arguments));
    //</if>
  }
});