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