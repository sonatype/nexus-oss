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
 * @author Ed Spencer
 *
 * Small helper class to make creating {@link Ext.data.Store}s from Array data easier. An ArrayStore will be
 * automatically configured with a {@link Ext.data.reader.Array}.
 *
 * A store configuration would be something like:
 *
 *     var store = Ext.create('Ext.data.ArrayStore', {
 *         // store configs
 *         storeId: 'myStore',
 *         // reader configs
 *         fields: [
 *            'company',
 *            {name: 'price', type: 'float'},
 *            {name: 'change', type: 'float'},
 *            {name: 'pctChange', type: 'float'},
 *            {name: 'lastChange', type: 'date', dateFormat: 'n/j h:ia'}
 *         ]
 *     });
 *
 * This store is configured to consume a returned object of the form:
 *
 *     var myData = [
 *         ['3m Co',71.72,0.02,0.03,'9/1 12:00am'],
 *         ['Alcoa Inc',29.01,0.42,1.47,'9/1 12:00am'],
 *         ['Boeing Co.',75.43,0.53,0.71,'9/1 12:00am'],
 *         ['Hewlett-Packard Co.',36.53,-0.03,-0.08,'9/1 12:00am'],
 *         ['Wal-Mart Stores, Inc.',45.45,0.73,1.63,'9/1 12:00am']
 *     ];
 *
 * An object literal of this form could also be used as the {@link #cfg-data} config option.
 *
 */
Ext.define('Ext.data.ArrayStore', {
    extend: 'Ext.data.Store',
    alias: 'store.array',
    requires: [
        'Ext.data.proxy.Memory',
        'Ext.data.reader.Array'
    ],

    constructor: function(config) {
        config = Ext.apply({
            proxy: {
                type: 'memory',
                reader: 'array'
            }
        }, config);
        this.callParent([config]);
    },

    loadData: function(data, append) {
        if (this.expandData === true) {
            var r = [],
                i = 0,
                ln = data.length;

            for (; i < ln; i++) {
                r[r.length] = [data[i]];
            }

            data = r;
        }

        this.callParent([data, append]);
    }
}, function() {
    // backwards compat
    Ext.data.SimpleStore = Ext.data.ArrayStore;
    // Ext.reg('simplestore', Ext.data.SimpleStore);
});