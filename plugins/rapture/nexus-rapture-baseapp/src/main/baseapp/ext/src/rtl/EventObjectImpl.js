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
Ext.define('Ext.rtl.EventObjectImpl', {
    override: 'Ext.EventObjectImpl',
    
    getXY: function() {
        var xy = this.xy;

        if (!xy) {
            xy = this.callParent();
            // since getXY is a page-level concept, we only need to check the
            // rootHierarchyState once to see if all successive calls to getXY() should have
            // their x-coordinate converted to rtl.
            if (this.rtl || (this.rtl = Ext.rootHierarchyState.rtl)) {
                xy[0] = Ext.Element.getViewportWidth() - xy[0];
            }
        }
        return xy;
    }

});