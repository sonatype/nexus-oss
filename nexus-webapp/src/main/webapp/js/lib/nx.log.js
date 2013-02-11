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
    var logger, safeProxy, levelAwareProxy;

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
            info:   true,
            warn:   true,
            error:  true
        },

        isEnabled: function(level) {
            return this.enabled && (this.levels[level] || NX.global.location.search === '?debug');
        }

        // FIXME: Add ExtJS 4.x compatible(ish) log() helper
        //log: function(/*[options],[message]*/) {
        //
        //}
    }

    safeProxy = function(target, name) {
        if (Ext.isDefined(target) && Ext.isFunction(target[name])) {
            return function() {
                if (logger.enabled) {
                    target[name].apply(target, arguments);
                }
            }
        }

        return Ext.emptyFn();
    };

    levelAwareProxy = function(target, name) {
        if (Ext.isDefined(target) && Ext.isFunction(target[name])) {
            return function() {
                if (logger.isEnabled(name)) {
                    target[name].apply(target, arguments);
                }
            }
        }

        return Ext.emptyFn();
    }

    // Not adding 'log' here as we may want to reserve that for an Ext.log compatible method
    Ext.each([
        'trace',
        'debug',
        'info',
        'warn',
        'error'
    ], function (name) {
        logger[name] = levelAwareProxy(console, name);
    });

    Ext.each([
        'dir',
        'group',
        'groupEnd'
    ], function (name) {
        logger[name] = safeProxy(console, name);
    });

    return logger;
}());

// Compatibility
Ext.ns('Nexus');
Nexus.Log = NX.log;
Nexus.log = NX.log.debug;

