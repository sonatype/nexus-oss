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
 * This class implements the component event domain. All classes extending from
 * {@link Ext.Component} are included in this domain. The matching criteria uses
 * {@link Ext.ComponentQuery}.
 * 
 * @private
 */
Ext.define('Ext.app.domain.Component', {
    extend: 'Ext.app.EventDomain',
    singleton: true,

    requires: [
        'Ext.Component'
    ],

    type: 'component',

    constructor: function() {
        var me = this;
        
        me.callParent();
        me.monitor(Ext.Component);
    },

    match: function(target, selector) {
        return target.is(selector);
    }
});
