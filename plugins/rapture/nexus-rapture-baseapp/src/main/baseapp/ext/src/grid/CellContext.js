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
/**
 * Internal utility class that provides a unique cell context.
 * @private
 */
Ext.define('Ext.grid.CellContext', {

    /**
     * @property {Boolean} isCellContext
     * @readonly
     * `true` in this class to identify an object as an instantiated CellContext, or subclass thereof.
     */
    isCellContext: true,
    
    constructor: function(view) {
        this.view = view;
    },
    
    isEqual: function(other) {
        if (other) {
            return this.record === other.record && this.columnHeader === other.columnHeader;
        }
        return false;
    },
    
    // Selection row/record & column/columnHeader
    setPosition: function(row, col) {
        var me = this;

        // We were passed {row: 1, column: 2, view: myView}
        if (arguments.length === 1) {
            if (row.view) {
                me.view = row.view;
            }
            col = row.column;
            row = row.row;
        }

        me.setRow(row);
        me.setColumn(col);
        return me;
    },

    setRow: function(row) {
        var me = this;
        if (row !== undefined) {
            // Row index passed
            if (typeof row === 'number') {
                me.row = Math.max(Math.min(row, me.view.dataSource.getCount() - 1), 0);
                me.record = me.view.dataSource.getAt(row);
            }
            // row is a Record
            else if (row.isModel) {
                me.record = row;
                me.row = me.view.indexOf(row);
            }
            // row is a grid row
            else if (row.tagName) {
                me.record = me.view.getRecord(row);
                me.row = me.view.indexOf(me.record);
            }
        }
    },
    
    setColumn: function(col) {
        var me = this,
            mgr = me.view.ownerCt.getColumnManager();
            
        if (col !== undefined) {
            if (typeof col === 'number') {
                me.column = col;
                me.columnHeader = mgr.getHeaderAtIndex(col);
            } else if (col.isHeader) {
                me.columnHeader = col;
                me.column = mgr.getHeaderIndex(col);
            }
        }
    }
});