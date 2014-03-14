/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
/**
 * A specific {@link Ext.data.Model} type that represents a name/value pair and is made to work with the
 * {@link Ext.grid.property.Grid}. Typically, Properties do not need to be created directly as they can be
 * created implicitly by simply using the appropriate data configs either via the
 * {@link Ext.grid.property.Grid#source} config property or by calling {@link Ext.grid.property.Grid#setSource}.
 * However, if the need arises, these records can also be created explicitly as shown below. Example usage:
 *
 *     var rec = new Ext.grid.property.Property({
 *         name: 'birthday',
 *         value: Ext.Date.parse('17/06/1962', 'd/m/Y')
 *     });
 *     // Add record to an already populated grid
 *     grid.store.addSorted(rec);
 *
 * @constructor
 * Creates new property.
 * @param {Object} config A data object in the format:
 * @param {String/String[]} config.name A name or names for the property.
 * @param {Mixed/Mixed[]} config.value A value or values for the property.
 * The specified value's type will be read automatically by the grid to determine the type of editor to use when
 * displaying it.
 * @return {Object}
 */
Ext.define('Ext.grid.property.Property', {
    extend: 'Ext.data.Model',

    alternateClassName: 'Ext.PropGridProperty',

    fields: [{
        name: 'name',
        type: 'string'
    }, {
        name: 'value'
    }],
    idProperty: 'name'
});