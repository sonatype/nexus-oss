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
Ext.define('Ext.rtl.layout.container.Box', {
    override: 'Ext.layout.container.Box',

    initLayout: function() {
        var me = this;

        if (me.owner.getHierarchyState().rtl) {
            me.names = Ext.Object.chain(me.names);
            Ext.apply(me.names, me.rtlNames);
        }

        me.callParent(arguments);
    },

    getRenderData: function () {
        var renderData = this.callParent();

        if (this.owner.getHierarchyState().rtl) {
            renderData.targetElCls =
                (renderData.targetElCls || '') + ' ' + Ext.baseCSSPrefix + 'rtl';
        }
        
        return renderData;
    }
});
