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
/**
 * @override Ext.rtl.layout.ContextItem
 * This override adds RTL support to Ext.layout.ContextItem.
 */
Ext.define('Ext.rtl.layout.ContextItem', {
    override: 'Ext.layout.ContextItem',

    addPositionStyles: function(styles, props) {
        var x = props.x,
            y = props.y,
            count = 0;

        if (x !== undefined) {
            styles[this.parent.target.getHierarchyState().rtl ? 'right' : 'left'] = x + 'px';
            ++count;
        }
        if (y !== undefined) {
            styles.top = y + 'px';
            ++count;
        }
        return count;
    }

});