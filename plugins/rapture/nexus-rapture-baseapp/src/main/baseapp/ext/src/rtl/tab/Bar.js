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
