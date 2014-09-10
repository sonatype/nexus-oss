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
Ext.define('Ext.rtl.chart.LegendItem', {
    override: 'Ext.chart.LegendItem',
    
    updatePosition: function(relativeTo) {
        var me = this,
            items = me.items,
            ln = items.length,
            legend = me.legend,
            currentX = me.x,
            currentY = me.y,
            item, i, x, y, translate, o, width,
            relativeX, relativeY;
            
        if (!relativeTo) {
            relativeTo = legend;
        }
            
        if (!legend.chart.getHierarchyState().rtl || !relativeTo.width) {
            me.callParent(arguments);
            return;
        }
        
        relativeX = relativeTo.x;
        relativeY = relativeTo.y;
        width = relativeTo.width;
        for (i = 0; i < ln; i++) {
            translate = true;
            item = items[i];
            switch (item.type) {
                case 'text':
                    x = width + relativeX + currentX - 30 - item.getBBox().width; // -25 & -5 for a gap
                    y = relativeY + currentY;
                    translate = false;
                    break;
                case 'rect':
                    x = width + relativeX + currentX - 25;
                    y = relativeY + currentY - 6;
                    break;
                default:
                    x = width + relativeX + currentX - 25;
                    y = relativeY + currentY;
            }
            
            o = {
                x: x,
                y: y
            };
            
            item.setAttributes(translate ? {
                translate: o
            } : o, true);
        }
    }    
});
