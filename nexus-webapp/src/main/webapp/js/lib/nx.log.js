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

/* global Ext,NX,Nexus */

Ext.ns('NX');

/**
 * Logging helper.
 *
 * @singleton
 */
NX.log = (function () {
    var log = Ext.emptyFn;

    // FIXME: Can probably expose the console object more directly w/fallback adapter?

    // reference the console as 'log' if it exists
    try {
        if (typeof(console) === "object" && console.log !== undefined) {
            log = function (level, msg) {
                console.log(level + ' ' + msg);
            };
        }
    }
    catch (e) {
        // ignore, use emptyFn
    }

    return {
        forceDebug: false,

        /**
         * @return {boolean} True if debug is enabled.
         */
        isDebug: function () {
            return this.forceDebug === true || NX.global.location.search === '?debug';
        },

        /**
         * @param msg {String} The message to log.
         */
        debug: function (msg) {
            if (this.isDebug()) {
                log('DEBUG', msg);
            }
        },

        /**
         * @param msg {String} The message to log.
         */
        info: function (msg) {
            log('INFO', msg);
        },

        /**
         * @param msg {String} The message to log.
         */
        warn: function (msg) {
            log('WARN', msg);
        },

        /**
         * @param msg {String} The message to log.
         */
        error: function (msg) {
            log('ERROR', msg);
        }
    };
}());

// Compatibility
Ext.ns('Nexus');
Nexus.Log = NX.log;
Nexus.log = NX.log.debug;

