/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2014 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

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
