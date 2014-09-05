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
Ext.define('Ext.rtl.slider.Multi', {
    override: 'Ext.slider.Multi',
    
    initComponent: function(){
        if (this.getHierarchyState().rtl) {
            this.horizontalProp = 'right';
        }    
        this.callParent();
    },
    
    onDragStart: function(){
        this.callParent(arguments);
        // Cache the width so we don't recalculate it during the drag
        this._rtlInnerWidth = this.innerEl.getWidth();
    },
    
    onDragEnd: function(){
        this.callParent(arguments);
        delete this._rtlInnerWidth;
    },
    
    transformTrackPoints: function(pos){
        var left, innerWidth;
        
        if (this.isOppositeRootDirection()) {
            left = pos.left;
            delete pos.left;
            
            innerWidth = typeof this._rtlInnerWidth !== 'undefined' ? this._rtlInnerWidth : this.innerEl.getWidth();
            pos.right = innerWidth - left;
            
            return pos;
        } else {
            return this.callParent(arguments);
        }
    },
    
    getSubTplData : function() {
        var me = this,
            data = me.callParent(),
            rtlCls = me._rtlCls;
        
        if (rtlCls && me.getHierarchyState().rtl) {
            data.childElCls = ' ' + rtlCls;
        }

        return data;
    }
});