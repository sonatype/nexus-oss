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
 * This class is used as a base class from which to derive Models used in Trees.
 */
Ext.define('Ext.data.TreeModel', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.NodeInterface'
    ],

    mixins: {
        queryable: 'Ext.Queryable'
    },

     getRefItems: function() {
         return this.childNodes;
     },

     getRefOwner: function() {
         return this.parentNode;
     }
},
function () {
    Ext.data.NodeInterface.decorate(this);
});