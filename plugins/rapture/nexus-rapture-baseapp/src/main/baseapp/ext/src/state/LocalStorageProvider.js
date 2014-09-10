/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2014 Sencha Inc

Contact:  http://www.sencha.com/contact

Pre-release code in the Ext repository is intended for development purposes only and will
not always be stable. 

Use of pre-release code is permitted with your application at your own risk under standard
Ext license terms. Public redistribution is prohibited.

For early licensing, please contact us at licensing@sencha.com

Build date: 2014-09-02 11:12:40 (ef1fa70924f51a26dacbe29644ca3f31501a5fce)
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
