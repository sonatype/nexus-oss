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
     * @param config
     *
     * @cfg {string} stylePrefix    Optional icon style prefix.
     * @cfg {*} icons               At least one {name: fileName} icon configuration is required.
     */
    constructor: function(config) {
        var self = this,
            config = config || {};

        self.stylePrefix = config.stylePrefix || 'nx-icons-';

        if (config.icons === undefined) {
            throw 'At least one icon definition must be configured';
        }

        Ext.iterate(config.icons, function(key, value, obj) {
            self.defineIcon(key, value);
        });

        // TODO: Pre-load all icons into browser

        // FIXME: This will recurs, something likely not correct in NX.define() which is causing this
        //self.constructor.superclass.constructor.apply(self, arguments);
    },

    /**
     * Returns the full path to an icon file.
     *
     * @private
     *
     * @return {string}
     */
    iconPath: function(file) {
        if (!file.startsWith('/')) {
            file = '/' + file;
        }
        return Sonatype.config.resourcePath + '/static/icons' + file;
    },

    /**
     * Define an icon.
     *
     * @private
     *
     * @param {string} name         Icon alias.
     * @param {string} fileName     Icon file name.
     * @return {*}                  Icon helper.
     */
    defineIcon: function(name, fileName) {
        var self = this,
            iconPath,
            cls,
            icon;

        // Puke early if icon already defined, this is likely a mistake
        if (self.icons[name] !== undefined) {
            throw 'Icon already defined with name: ' + name;
        }

        iconPath = self.iconPath(fileName);

        cls = self.stylePrefix + name;

        self.$log('Defining icon: ' + name + ' (' + cls + ') = ' + iconPath);

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
             */
            name: name,

            /**
             * Short icon file-name.
             */
            fileName: fileName,

            /**
             * Full icon path.
             */
            path: iconPath,

            /**
             * <img> representation.
             */
            img: '<img src="' + iconPath + '">',

            /**
             * Icon class.
             */
            cls: cls
        }

        self.icons[name] = icon;

        return icon;
    },

    /**
     * Lookup an icon by name.  If the named icon is not defined an exception will be thrown.
     *
     * @param name  The name of the icon.
     * @return {*}  Icon; never null/undefined.
     */
    get: function(name) {
        var self = this,
            icon;

        icon = self.icons[name];
        if (icon === undefined) {
            throw 'No icon defined for name: ' + name;
        }

        return icon;
    }
});
