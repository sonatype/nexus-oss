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
/*global NX, Ext, Nexus*/

/**
 * Support for icon containers.
 *
 * @since 2.4
 */
NX.define('Nexus.util.IconContainer', {
    mixins: [
        'Nexus.LogAwareMixin'
    ],

    requirejs: [
        'Nexus/config'
    ],

    /**
     * Defined icons.
     *
     * @private
     */
    icons: {},

    /**
     * Base-path for images.
     *
     * @public
     * @property
     */
    basePath: undefined,

    /**
     * @constructor
     *
     * @param config
     *
     * @cfg {string} stylePrefix    Optional icon style prefix.
     * @cfg {*} icons               At least one {name: fileName} icon configuration is required.
     * @cfg {String} basePath       Optional base path for images.
     */
    constructor: function (config) {
        var self = this,
            config = config || {},
            icons;

        // apply defaults to configuration
        Ext.applyIf(config, {
            basePath: Sonatype.config.resourcePath + '/static/icons',
            stylePrefix: 'nx-icons-'
        });

        // verify, capture and strip out 'icons' from configuration
        NX.assert(config.icons !== undefined, 'At least one icon definition must be configured');
        icons = config.icons;
        delete config.icons;

        // apply configuration
        Ext.apply(self, config);

        self.logGroup('Defining icons');

        Ext.iterate(icons, function (key, value, obj) {
            self.defineIcon(key, value);
        });

        // TODO: Pre-load all icons into browser

        self.logGroupEnd();
    },

    /**
     * Returns the full path to an icon file.
     *
     * @private
     *
     * @return {string}
     */
    iconPath: function (file) {
        if (!file.startsWith('/')) {
            file = '/' + file;
        }
        return this.basePath + file;
    },

    /**
     * Define an icon.
     *
     * @private
     *
     * @param {string} name         Icon name.
     * @param {string} fileName     Icon file name (or @alias).
     * @return {*}                  Icon helper.
     */
    defineIcon: function (name, fileName) {
        var self = this,
            alias,
            iconPath,
            cls,
            icon;

        // Puke early if icon already defined, this is likely a mistake
        NX.assert(self.icons[name] === undefined, 'Icon already defined with name: ' + name);

        // If fileName is an alias, then resolve it
        if (fileName.startsWith('@')) {
            alias = fileName.substring(1, fileName.length);
            icon = self.icons[alias];
            NX.assert(icon !== undefined, 'Invalid alias; No icon defined with name: ' + alias);

            self.logDebug('Defining icon:', name, 'aliased to:', alias);
        }
        else {
            // else define a new icon
            iconPath = self.iconPath(fileName);

            cls = self.stylePrefix + name;

            self.logDebug('Defining icon:', name, 'cls:', cls, 'path:', iconPath);

            Ext.util.CSS.createStyleSheet(
                '.' + cls + ' { background: url(' + iconPath + ') no-repeat !important; }',
                cls // use class as id
            );

            /**
             * Icon.
             */
            icon = {
                /**
                 * Symbolic name for icon.
                 *
                 * @type {String}
                 */
                name: name,

                /**
                 * Short icon file-name.
                 *
                 * @type {String}
                 */
                fileName: fileName,

                /**
                 * Full icon path.
                 *
                 * @type {String}
                 */
                path: iconPath,

                /**
                 * HTML <img> representation.
                 *
                 * @type {String}
                 */
                img: '<img src="' + iconPath + '">',

                /**
                 * Icon CSS class.
                 *
                 * @type {String}
                 */
                cls: cls
            }
        }

        self.icons[name] = icon;

        return icon;
    },

    /**
     * Lookup an icon by name.  If the named icon is not defined an exception will be thrown.
     *
     * @public
     *
     * @param name  The name of the icon.
     * @return {*}  Icon; never null/undefined.
     */
    get: function (name) {
        var self = this,
            icon;

        icon = self.icons[name];
        NX.assert(icon !== undefined, 'No icon defined for name: ' + name);

        return icon;
    }

});
