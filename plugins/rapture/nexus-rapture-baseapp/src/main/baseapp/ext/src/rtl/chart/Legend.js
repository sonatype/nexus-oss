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
Ext.define('Ext.rtl.chart.Legend', {
    override: 'Ext.chart.Legend',
    
    init: function() {
        var me = this;   
        
        me.callParent(arguments);
        me.position = me.chart.invertPosition(me.position);    
        me.rtl = me.chart.getHierarchyState().rtl;  
    },
    
    updateItemDimensions: function() {
        var me = this,
            result = me.callParent(),
            padding = me.padding,
            spacing = me.itemSpacing,
            items = me.items,
            len = items.length,
            mfloor = Math.floor,
            width = result.totalWidth,
            usedWidth = 0,
            i, item, itemWidth;
            
        if (me.rtl && !me.isVertical) {
            for (i = 0; i < len; ++i) {
                item = items[i];
 
                // Set the item's position relative to the legend box
                itemWidth = mfloor(item.getBBox().width + spacing);
                item.x = -usedWidth + padding;
                usedWidth += itemWidth;
            }
        }
        return result;
    }
})
