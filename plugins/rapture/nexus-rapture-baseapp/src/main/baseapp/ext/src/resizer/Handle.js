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
 * Provides a handle for 9-point resizing of Elements or Components.
 */
Ext.define('Ext.resizer.Handle', {
    extend: 'Ext.Component',
    handleCls: '',
    baseHandleCls: Ext.baseCSSPrefix + 'resizable-handle',
    // Ext.resizer.Resizer.prototype.possiblePositions define the regions
    // which will be passed in as a region configuration.
    region: '',
    
    ariaRole: 'presentation',

    beforeRender: function() {
        var me = this;

        me.callParent();

        me.protoEl.unselectable();

        me.addCls(
            me.baseHandleCls,
            me.baseHandleCls + '-' + me.region,
            me.handleCls
        );
    }
});
