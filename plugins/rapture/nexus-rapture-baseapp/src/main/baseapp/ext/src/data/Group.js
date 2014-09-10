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
/** */
Ext.define('Ext.data.Group', {

    extend: 'Ext.util.Observable',

    key: undefined,

    dirty: true,

    constructor: function(){
        this.callParent(arguments);
        this.records = [];    
    },

    contains: function(record){
        return Ext.Array.indexOf(this.records, record) !== -1;
    },

    add: function(records) {
        Ext.Array.push(this.records, records);
        this.dirty = true;  
    },

    remove: function(records) {
        if (!Ext.isArray(records)) {
            records = [records];
        }

        var len = records.length,
            i;

        for (i = 0; i < len; ++i) {
            Ext.Array.remove(this.records, records[i]);
        }
        this.dirty = true;
    },

    isDirty: function(){
        return this.dirty;    
    },

    hasAggregate: function(){
        return !!this.aggregate;
    },

    setDirty: function(){
        this.dirty = true;
    },

    commit: function(){
        this.dirty = false;
    },

    isCollapsed: function(){
        return this.collapsed;    
    },

    getAggregateRecord: function(forceNew){
        var me = this,
            Model;

        if (forceNew === true || me.dirty || !me.aggregate) {
            Model = me.store.model;
            me.aggregate = new Model();
            me.aggregate.isSummary = true;
        }
        return me.aggregate;
    }

});
