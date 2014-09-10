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
Ext.define('Ext.rtl.tab.Bar', {
    override: 'Ext.tab.Bar',
    adjustTabPositions: function() {
        var items = this.items.items,
            i = items.length,
            tab;

        if (!this.getHierarchyState().rtl) {
            return this.callParent();
        }

        // When tabs are rotated vertically we don't have a reliable way to position
        // them using CSS in modern browsers.  This is because of the way transform-orign
        // works - it requires the width to be known, and the width is not known in css.
        // Consequently we have to make an adjustment to the tab's position in these browsers.
        // This is similar to what we do in Ext.panel.Header#adjustTitlePosition
        if (!Ext.isIE9m) {
            if (this.dock === 'left') {
                // rotated 90 degrees around using the top left corner as the axis.
                // tabs need to be shifted to the right by their height
                while (i--) {
                    tab = items[i];
                    tab.el.setStyle('right', -tab.lastBox.height + 'px');
                }
            } else if (this.dock === 'right') {
                // rotated 270 degrees around using the top right corner as the axis.
                // tabs need to be shifted to the left by their width
                while (i--) {
                    tab = items[i];
                    tab.el.setStyle('right', tab.lastBox.width + 'px');
                }
            }
        }
    },

    getCloseXY: function(closeEl, tabX, tabY, tabWidth, tabHeight, closeWidth, closeHeight, direction) {
        var closeXY, closeX, closeY, xy;

        if (this.isOppositeRootDirection()) {
            closeXY = closeEl.getXY();
            if (direction === 'right') {
                closeX = tabX + closeXY[1] - tabY;
                closeY = tabY + tabHeight - (closeXY[0] - (tabX + tabWidth - tabHeight)) - closeWidth;
            } else {
                closeX = tabX + tabWidth - (closeXY[1] - tabY) - closeHeight;
                closeY = tabY + (closeXY[0] - (tabX + tabWidth - tabHeight));
            }
            xy = [closeX, closeY];
        } else {
            xy = this.callParent(arguments);
        }

        return xy;
    }
});
