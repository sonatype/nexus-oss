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
Ext.define('Ext.rtl.panel.Header', {
    override: 'Ext.panel.Header',

    rtlPositions: {
        top: 'top',
        right: 'left',
        bottom: 'bottom',
        left: 'right'
    },

    adjustTitlePosition: function() {
        var titleCmp = this.titleCmp,
            titleEl, width;

        if (!Ext.isIE9m && titleCmp) { // some Headers don't have a titleCmp, e.g. TabBar
            // in browsers that use css3 transform to rotate text we have to
            // adjust the element's position after rotating.  See comment in overridden
            // method for details.
            titleEl = titleCmp.el;
            width = titleEl.getWidth();
            if (this.isParentRtl()) {
                // in rtl mode we rotate 270 instead of 90 degrees with a transform
                // origin of the top right corner so moving the element right by the
                // same number of pixels as its width results in the correct positioning.
                titleEl.setStyle('right', width + 'px');
            } else if (!Ext.isIE9m) {
                titleEl.setStyle('left', width + 'px');
            }
        }
    },

    onTitleRender: function() {
        if (this.orientation === 'vertical') {
            this.titleCmp.el.setVertical(this.isParentRtl() ? 270 : 90);
        }
    },

    getDockName: function() {
        var me = this,
            dock = me.dock;
            
        return me.isParentRtl() ? me.rtlPositions[dock] : dock
    }
});