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
 * Support for icon containers.  An icon container is a helper to manage image/icon resources used by an application.
 *
 * The container automatically creates CSS styles for icons and installs them into the browser when loading.
 *
 * A simple example of an icon container with a single icon, and how to reference the icon.
 *
 *      @example
 *      var myIcons = NX.create('Nexus.util.IconContainer', {
 *          icons: {
 *              hello:  'hello.png'
 *          }
 *      });
 *
 *      // returns an icon object
 *      var icon = myIcons.get('hello);
 *
 *      // the name of the CSS class of the icon
 *      var cssClass = icon.cls;
 *
 *      // renders a <img> html snippet for the icon
 *      var imgHtml = icon.img;
 *
 * Icons can reference other icons, this helps avoid loading duplicate assents into the browser, and still allows
 * for flexible icon naming.  This allows applications to define meaningful names, and then manage which asset
 * belongs to that name inside of the icon container:
 *
 *      @example
 *      var myIcons = NX.create('Nexus.util.IconContainer', {
 *          icons: {
 *              rulePassed:         'tick.png',
 *              event_rulesPassed:  '@rulePassed',
 *              event_rulePassed:   '@rulePassed'
 *             }
 *      });
 *
 * Here 'event_rulesPassed' and 'event_rulePassed' are aliases to the 'rulePassed' icon.  Only 1 img/css asset for 'rulePassed'
 * is installed into the browser.
 *
 * For icons which might have many different sizes, and icon definition can be configured to know about different variants:
 *
 *      @example
 *      var myIcons = NX.create('Nexus.util.IconContainer', {
 *          icons: {
 *              rulePassed: {
 *                  x16:    'tick.png',
 *                  x32:    'tick-32x32.png',
 *                  _:      '^x16'
 *              },
 *              event_rulesPassed:   '@rulePassed',
 *              event_rulePassed:    '@rulePassed'
 *             }
 *      });
 *
 * This defines 5 icons, and 2 img+css (the assets loaded into the browser).
 *
 * The first bits for rulePassed define 2 icons 'rulePassedx16' and 'rulePassedx32' pointing at tick.png and tick-32x32.png respectively.
 * The special _ key is the default which will be used to set the 'rulePassed' icon.
 * The special syntax here with '^x16' means that he default icon is really the 'x16' variant and
 * ATM this only works for the default '_' variant.
 *
 * Icon objects have a variant() method which can be used to access a variant:
 *
 *      @example
 *      var icon = MyIcons.get('rulePassed');
 *      var bigger = icon.variant('x32');
 *
 * If the variant does not exist then it returns the same icon.
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
     * This defaults to the base resource path of Nexus + '/static/icons'.
     *
     * @public
     * @property
     * @type {String}
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

        self.logGroup('Loading icons');

        Ext.iterate(icons, function (key, value, obj) {
            self.loadIcon(key, value);
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
     * Load icons from configuration.
     *
     * @private
     *
     * @param {String} name     Icon name.
     * @param {*} config        String fileName/alias or object for icon with variants.
     */
    loadIcon: function(name, config) {
        var self = this;

        // if config is a string, then its a simple fileName/alias just define it
        if (Ext.isString(config)) {
            self.defineIcon(name, config);
        }
        // else its a complex icon definition with variants
        else if (Ext.isObject(config)) {
            // strip off the default icon configuration.
            var defaultIconFileName = config._;
            delete config._;

            // define icons for each variant, remember last icon name we created for default
            var lastIcon;
            Ext.iterate(config, function(key, value) {
                lastIcon = self.defineIcon(name + key, value, name);
            });

            // complain if there were no variants configured
            NX.assert(lastIcon !== undefined, 'No icon variants defined');

            // handle default icon
            if (defaultIconFileName !== undefined) {
                // if the fileName starts with '^' then its a back reference to a variant
                if (defaultIconFileName.startsWith('^')) {
                    defaultIconFileName = '@' + name + defaultIconFileName.substring(1);
                }
                self.defineIcon(name, defaultIconFileName);
            }
            else {
                // if no default configuration, then the default icon is the last icon defined
                self.defineIcon(name, '@' + lastIconName);
            }
        }
    },

    /**
     * Define an icon from a fileName (or @alias).
     *
     * @private
     *
     * @param {string} name         Icon name.
     * @param {string} fileName     Icon file name (or @alias).
     * @param {string} [baseName]   Base icon name when used with variants.
     * @return {*}                  Icon helper.
     */
    defineIcon: function (name, fileName, baseName) {
        var self = this,
            alias,
            iconPath,
            cls,
            icon;

        // TODO: fileName, really a config, if a string, is this below, if an object is variant icon container

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

            // install stylesheet for icon
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
                 * The base symbolic name for an icon with variants.
                 *
                 * @type {String}
                 */
                baseName: baseName,

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
                cls: cls,

                /**
                 * Return an icon variant, or the same icon if the variant does not exist.
                 *
                 * @param {String} variantName
                 * @return {*} Icon; never null
                 */
                variant: function(variantName) {
                    // if baseName is not set, then we have no variants configured, return same icon
                    if (baseName === undefined) {
                        self.logWarn('Icon has no variants:', name);
                        return this;
                    }

                    // else look for a variant
                    var icon = self.find(baseName, variantName);
                    if (icon !== undefined) {
                        return icon;
                    }

                    self.logWarn('No such icon variant:', variantName, 'for icon:', baseName);

                    // still nothing, return same icon
                    return this;
                }
            }
        }

        self.icons[name] = icon;

        return icon;
    },

    /**
     * Lookup an icon by name.
     *
     * @public
     *
     * @param name      The name of the icon.
     * @param variant   Optional icon variant name.
     * @return {*}      Icon; or null
     */
    find: function (name, variant) {
        var self = this;

        if (variant !== undefined) {
            // TODO: Probably should verify that variant is a string, as that is what we expect ATM
            name = name + variant;
        }

        return self.icons[name];
    },

    /**
     * Lookup an icon by name.  If the named icon is not defined an exception will be thrown.
     *
     * @public
     *
     * @param name      The name of the icon.
     * @param variant   Optional icon variant name.
     * @return {*}      Icon; never null/undefined.
     */
    get: function (name, variant) {
        var self = this,
            icon = self.find(name, variant);

        if (variant !== undefined) {
            NX.assert(icon !== undefined, 'No icon defined for name: ' + name + ' variant: ' + variant);
        }
        else {
            NX.assert(icon !== undefined, 'No icon defined for name: ' + name);
        }

        return icon;
    }

});
