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
 * Adds logging support helpers to objects.
 *
 * @since 3.0
 */
Ext.define('NX.LogAware', {
  requires: [
    'NX.Log'
  ],

  /**
   * @param {String} level
   * @param {Array} args
   */
  log: function (level, args) {
    args.unshift('[' + Ext.getClassName(this) + ']');
    NX.Log.log(level, args);
  },

  /**
   * @public
   */
  logDebug: function () {
    this.log('debug', Array.prototype.slice.call(arguments));
  },

  /**
   * @public
   */
  logInfo: function () {
    this.log('info', Array.prototype.slice.call(arguments));
  },

  /**
   * @public
   */
  logWarn: function () {
    this.log('warn', Array.prototype.slice.call(arguments));
  },

  /**
   * @public
   */
  logError: function () {
    this.log('error', Array.prototype.slice.call(arguments));
  }
});