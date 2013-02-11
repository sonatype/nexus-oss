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

/**
 * Mixin to make classes logging aware.
 *
 * @since 2.4
 */
NX.define('NX.LogAwareMixin', {
    statics: {
        /**
         * True to include class-names in log message (default); false to omit it.
         *
         * @property
         */
        includeName: true,

        /**
         * True to use simple class-names (default); false to use full class-names.
         *
         * @property
         */
        simpleName: true,

        /**
         * True to include level in log message (default); false to omit it.
         *
         * @property
         */
        includeLevel: true
    },

    /**
     * @private
     */
    logx: function (level, args) {
        var name,
            fn;

        if (!Ext.isArray(args)) {
            args = [args];
        }

        // maybe prepend class-name
        if (NX.LogAwareMixin.includeName === true) {
            name = this.$className;
            if (NX.LogAwareMixin.simpleName === true) {
                name = this.$simpleClassName;
            }
            args.unshift(name + ':');
        }

        // maybe prepend level
        if (NX.LogAwareMixin.includeLevel === true) {
            args.unshift('[' + level.toUpperCase() + ']');
        }

        // find the log function for the given level
        fn = NX.log[level];
        NX.assert(Ext.isFunction(fn), 'Invalid level: ' + level);

        fn.apply(NX.log, args);
    },

    /**
     * @protected
     */
    logDebug: function () {
        this.logx('debug', arguments);
    },

    /**
     * @protected
     */
    logInfo: function () {
        this.logx('info', arguments);
    },

    /**
     * @protected
     */
    logWarn: function () {
        this.logx('warn', arguments);
    },

    /**
     * @protected
     */
    logError: function () {
        this.logx('error', arguments);
    }

});