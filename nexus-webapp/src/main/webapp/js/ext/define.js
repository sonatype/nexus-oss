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
// FIXME this file should be moved into js/lib directory, but there are pending changes on other branches ATM. Leaving it here to avoid merge issues.

    //
    // NOTE: extjs 3.4.1 has an Ext.define, but its no available in a GPL version, so we have to use this cruft until we move over to extjs 4.x
    //

    /**
     * Used as the super-class for all Ext.define classes which not not provide a class to extend.
     * Do not put anything special in here, since not everything will use this as its super-class.
     *
     * @constructor
     */
    Ext.Base = function (config) {
        //console.log('Ext.Base CTOR');
        Ext.apply(this, config);
    };

    /**
     * Define a new class.
     *
     * @param {String} className        The name of the class to define.
     * @param {Object} data             Configuration for the class.
     * @param {Function} [createdFn]    Function to execute when class has been defined.
     * @return {Function}               A reference to the defined class.
     * @static
     */
    Ext.define = function (className, data, createdFn) {
        data = data || {};

        var i, nameSpace, baseClassName, superName, type, superClass, statics, obj;

        obj = function (path) {
            var context = window;
            Ext.each(path.split('.'), function (part) {
                context = context[part];
            });
            return context;
        };

        // Find the namespace (if any) for the new class
        i = className.lastIndexOf('.');
        if (i !== -1) {
            nameSpace = className.substring(0, i);
            baseClassName = className.substring(i + 1);
        }
        else {
            baseClassName = className;
        }

        // Determine the super-class
        superName = data.extend || 'Ext.Base';
        delete data.extend;

        // Extract static configuration
        statics = data.statics;
        delete data.statics;

        Nexus.log('Defining class: ' + className + ' (ns: ' + nameSpace + ', super: ' + superName + ')');

        // Create namespace if required
        if (nameSpace) {
            Ext.namespace(nameSpace);
        }

        // Get a reference to the super-class
        superClass = obj(superName);

        // When no constructor given in configuration (its always there due to picking upt from Object.prototype), use a synthetic version
        if (data.constructor === Object.prototype.constructor) {
            data.constructor = function () {
                // Just call superclass constructor
                this.constructor.superclass.constructor.apply(this, arguments);
            };
        }

        // Create the sub-class
        type = Ext.extend(superClass, data);

        // Assign to global namespace
        obj(nameSpace)[baseClassName] = type;

        // Enrich the sub-class prototype
        type.prototype.$className = className;

        // FIXME: Figure out how to fucking define this properly, all seem to work in different fucking ways
        //type.prototype.superclass = superClass;
        //type.prototype.$super = superClass.prototype;
        //type.prototype.$super = eval(className + '.superclass');
        //type.prototype.$super = function () {};

        type.prototype.$log = function (message) {
            Nexus.log(this.$className + ': ' + message);
        };

        // Apply any static members
        if (statics !== undefined) {
            Ext.apply(type, statics);
        }

        // Call post-define hook
        if (createdFn !== undefined) {
            // Scope to created type, empty args seems to be required here
            createdFn.call(type, []);
        }

        return type;
    };

    // FIXME: Port over extjs-4 Ext.create() bits so we can have sane[r] object creation

    // FIXME: Port over extjs-4 Ext.Error.* bits so we can have sane[r] exception handling
