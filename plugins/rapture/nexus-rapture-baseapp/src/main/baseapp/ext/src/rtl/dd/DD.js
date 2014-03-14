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
Ext.define('Ext.rtl.dd.DD', {
    override: 'Ext.dd.DD',

    // used be alignElWithMouse to get the local x coordinate adjusted for rtl mode if
    // the page-level coordinate system is rtl.
    getLocalX: function(el) {
        return Ext.rootHierarchyState.rtl ? el.rtlGetLocalX() : el.getLocalX();
    },

    // setLocalXY is used by alignElWithMouse to avoid the overhead that would be incurred
    // by using setXY to calculate left/right/top styles from page coordinates.  Since the
    // coordinates that go into the calculation are page-level, we need to use rtl local
    // coordinates if the page-level coordinate system is rtl.
    setLocalXY: function(el, x, y) {
        Ext.rootHierarchyState.rtl ? el.rtlSetLocalXY(x, y) : el.setLocalXY(x, y);
    }
});