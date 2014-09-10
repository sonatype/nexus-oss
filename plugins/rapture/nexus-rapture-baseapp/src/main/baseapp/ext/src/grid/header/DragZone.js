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
 * @private
 */
Ext.define('Ext.grid.header.DragZone', {
    extend: 'Ext.dd.DragZone',
    colHeaderSelector: '.' + Ext.baseCSSPrefix + 'column-header',
    colInnerSelector: '.' + Ext.baseCSSPrefix + 'column-header-inner',
    maxProxyWidth: 120,

    constructor: function(headerCt) {
        var me = this;
        
        me.headerCt = headerCt;
        me.ddGroup =  me.getDDGroup();
        me.autoGroup = true;
        me.callParent([headerCt.el]);
        me.proxy.el.addCls(Ext.baseCSSPrefix + 'grid-col-dd');
    },
    
    getDDGroup: function() {
        return 'header-dd-zone-' + this.headerCt.up('[scrollerOwner]').id;
    },

    getDragData: function(e) {
        if (e.getTarget(this.colInnerSelector)) {
            var header = e.getTarget(this.colHeaderSelector),
                headerCmp,
                ddel;

            if (header) {
                headerCmp = Ext.getCmp(header.id);
                if (!this.headerCt.dragging && headerCmp.draggable && !(headerCmp.isOnLeftEdge(e) || headerCmp.isOnRightEdge(e))) {
                    ddel = document.createElement('div');
                    ddel.role = 'presentation';
                    ddel.innerHTML = Ext.getCmp(header.id).text;
                    return {
                        ddel: ddel,
                        header: headerCmp
                    };
                }
            }
        }
        return false;
    },

    onBeforeDrag: function() {
        return !(this.headerCt.dragging || this.disabled);
    },

    onInitDrag: function() {
        this.headerCt.dragging = true;
        this.callParent(arguments);
    },

    onDragDrop: function() {
        this.headerCt.dragging = false;
        this.callParent(arguments);
    },

    afterRepair: function() {
        this.callParent();
        this.headerCt.dragging = false;
    },

    getRepairXY: function() {
        return this.dragData.header.el.getXY();
    },
    
    disable: function() {
        this.disabled = true;
    },
    
    enable: function() {
        this.disabled = false;
    }
});
