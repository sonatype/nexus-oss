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
/**
 * Internal utility class that provides default configuration for cell editing.
 * @private
 */
Ext.define('Ext.grid.CellEditor', {
    extend: 'Ext.Editor',
    constructor: function(config) {
        config = Ext.apply({}, config);
        
        if (config.field) {
            config.field.monitorTab = false;
        }
        this.callParent([config]);
    },
    
    /**
     * @private
     * Hides the grid cell inner element when a cell editor is shown.
     */
    onShow: function() {
        var me = this,
            innerCell = me.boundEl.first();

        if (innerCell) {
            if (me.isForTree) {
                innerCell = innerCell.child(me.treeNodeSelector);
            }
            innerCell.hide();
        }

        me.callParent(arguments);
    },

    /**
     * @private
     * Shows the grid cell inner element when a cell editor is hidden
     */
    onHide: function() {
        var me = this,
            innerCell = me.boundEl.first();

        if (innerCell) {
            if (me.isForTree) {
                innerCell = innerCell.child(me.treeNodeSelector);
            }
            innerCell.show();
        }
        
        me.callParent(arguments);
    },

    /**
     * @private
     * Fix checkbox blur when it is clicked.
     */
    afterRender: function() {
        var me = this,
            field = me.field;

        me.callParent(arguments);
        if (field.isCheckbox) {
            field.mon(field.inputEl, {
                mousedown: me.onCheckBoxMouseDown,
                click: me.onCheckBoxClick,
                scope: me
            });
        }
    },
    
    /**
     * @private
     * Because when checkbox is clicked it loses focus  completeEdit is bypassed.
     */
    onCheckBoxMouseDown: function() {
        this.completeEdit = Ext.emptyFn;
    },
    
    /**
     * @private
     * Restore checkbox focus and completeEdit method.
     */
    onCheckBoxClick: function() {
        delete this.completeEdit;
        this.field.focus(false, 10);
    },
    
    /**
     * @private
     * Realigns the Editor to the grid cell, or to the text node in the grid inner cell
     * if the inner cell contains multiple child nodes.
     */
    realign: function(autoSize) {
        var me = this,
            boundEl = me.boundEl,
            innerCell = boundEl.first(),
            innerCellTextNode = innerCell.dom.firstChild,
            width = boundEl.getWidth(),
            offsets = Ext.Array.clone(me.offsets),
            grid = me.grid,
            xOffset,
            v = '',

            // innerCell is empty if there are no children, or there is one text node, and it contains whitespace
            isEmpty = !innerCellTextNode || (innerCellTextNode.nodeType === 3 && !(Ext.String.trim(v = innerCellTextNode.data).length));

        if (me.isForTree) {
            // When editing a tree, adjust the width and offsets of the editor to line
            // up with the tree cell's text element
            xOffset = me.getTreeNodeOffset(innerCell);
            width -= Math.abs(xOffset);
            offsets[0] += xOffset;
        }

        if (grid.columnLines) {
            // Subtract the column border width so that the editor displays inside the
            // borders. The column border could be either on the left or the right depending
            // on whether the grid is RTL - using the sum of both borders works in both modes. 
            width -= boundEl.getBorderWidth('rl');
        }

        if (autoSize === true) {
            me.field.setWidth(width);
        }

        // https://sencha.jira.com/browse/EXTJSIV-10871 Ensure the data bearing element has a height from text.
        if (isEmpty) {
            innerCell.dom.innerHTML = 'X';
        }
        me.alignTo(innerCell, me.alignment, offsets);
        if (isEmpty) {
            innerCell.dom.firstChild.data = v;
        }
    },

    // private
    getTreeNodeOffset: function(innerCell) {
        return innerCell.child(this.treeNodeSelector).getOffsetsTo(innerCell)[0];
    },

    onEditorTab: function(e){
        var field = this.field;
        if (field.onEditorTab) {
            field.onEditorTab(e);
        }
    },

    onFieldBlur : function() {
        this.callParent(arguments);
        // Reset the flag that may have been set by CellEditing#startEdit to prevent
        // Ext.Editor#onFieldBlur from canceling editing.
        this.selectSameEditor = false;
    },

    alignment: "l-l",
    hideEl : false,
    cls: Ext.baseCSSPrefix + 'small-editor ' +
        Ext.baseCSSPrefix + 'grid-editor ' + 
        Ext.baseCSSPrefix + 'grid-cell-editor',
    treeNodeSelector: '.' + Ext.baseCSSPrefix + 'tree-node-text',
    shim: false,
    shadow: false
});