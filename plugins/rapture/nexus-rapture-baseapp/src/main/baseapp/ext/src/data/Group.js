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
