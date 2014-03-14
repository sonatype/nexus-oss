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
Ext.define('Ext.rtl.resizer.SplitterTracker', {
    override: 'Ext.resizer.SplitterTracker',

    getVertPrevConstrainLeft: function(o) {
        return (!this.splitter.getHierarchyState().rtl !== !Ext.rootHierarchyState.rtl) ?
            ((o.prevCmp.maxWidth ? o.prevBox.right - o.prevCmp.maxWidth :
            o.nextBox.x + (o.nextCmp.minWidth || o.defaultMin)) - o.splitWidth) :
            this.callParent(arguments);
    },

    getVertPrevConstrainRight: function(o) {
        return (!this.splitter.getHierarchyState().rtl !== !Ext.rootHierarchyState.rtl) ?
            o.prevBox.right - (o.prevCmp.minWidth || o.defaultMin) :
            this.callParent(arguments);
    },


    getVertNextConstrainLeft: function(o) {
        return (!this.splitter.getHierarchyState().rtl !== !Ext.rootHierarchyState.rtl) ?
            o.nextBox.x + (o.nextCmp.minWidth || o.defaultMin) :
            this.callParent(arguments);
    },

    getVertNextConstrainRight: function(o) {
        return (!this.splitter.getHierarchyState().rtl !== !Ext.rootHierarchyState.rtl) ?
            ((o.nextCmp.maxWidth ? o.nextBox.x + o.nextCmp.maxWidth :
            o.prevBox.right - (o.prevBox.minWidth || o.defaultMin)) + o.splitWidth) :
            this.callParent(arguments);
    },

    getResizeOffset: function() {
        var offset = this.getOffset('dragTarget');
        if (!this.splitter.getHierarchyState().rtl !== !Ext.rootHierarchyState.rtl) {
            offset[0] = -offset[0];
        }
        return offset;
    }
});