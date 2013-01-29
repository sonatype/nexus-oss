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
 * "ExtJS version 4"-ish compatible Ext.create.
 *
 * Not replacing Ext.create, since 3.4 has a very different use for this static method.
 *
 * @static
 *
 * @param {String} name
 * @param {*} [args]
 */
Ext.xcreate = function (name, args) {
    var obj,
        type,
        construct;

    obj = function (path) {
        var context = window;
        Ext.each(path.split('.'), function (part) {
            context = context[part];
        });
        return context;
    };

    type = obj(name);
    if (type === undefined) {
        type = require(name.replaceAll('.', '/'));
    }

    construct = function (constructor, args) {
        function F() {
            return constructor.apply(this, args);
        }
        F.prototype = constructor.prototype;
        return new F();
    }

    return construct(type, args);
};
