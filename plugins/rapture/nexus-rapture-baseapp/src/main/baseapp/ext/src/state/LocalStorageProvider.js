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
 * A Provider implementation which saves and retrieves state via the HTML5 localStorage API
 * or IE `userData` storage. For details see `Ext.util.LocalStorage`.
 * 
 * If the browser does not support local storage, there will be no attempt to read the state.
 * Before creating this class, check {@link Ext.util.LocalStorage#supported}.
 */
Ext.define('Ext.state.LocalStorageProvider', {
    extend: 'Ext.state.Provider',
    requires: [
        'Ext.util.LocalStorage'
    ],
    
    alias: 'state.localstorage',
   
    constructor: function () {
        var me = this;

        me.callParent(arguments);

        me.store = me.getStorageObject();
        if (me.store) {
            me.state = me.readLocalStorage();
        } else {
            me.state = {};
        }
    },
    
    readLocalStorage: function () {
        var store = this.store,
            data = {},
            keys = store.getKeys(),
            i = keys.length,
            key;
            
        while (i--) {
            key = keys[i];
            data[key] = this.decodeValue(store.getItem(key));
        }

        return data;
    },
    
    set: function (name, value) {
        var me = this;
        
        me.clear(name);
        if (value != null) { // !== undefined && !== null
            me.store.setItem(name, me.encodeValue(value));
            me.callParent(arguments);
        }
    },

    // private
    clear: function (name) {
        this.store.removeItem(name);
        this.callParent(arguments);
    },
    
    getStorageObject: function () {
        var prefix = this.prefix,
            id = prefix,
            n = id.length - 1;

        if (id.charAt(n) === '-') {
            id = id.substring(0, n);
        }

        return new Ext.util.LocalStorage({
            id: id,
            prefix: prefix
        });
    }    
});
