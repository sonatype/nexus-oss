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
 * Private class which acts as a HeaderContainer for the Lockable which aggregates all columns
 * from both sides of the Lockable. It is never rendered, it's just used to interrogate the
 * column collection.
 * @private
 */
Ext.define('Ext.grid.locking.HeaderContainer', {
    extend: 'Ext.grid.header.Container',
    requires: [
        'Ext.grid.ColumnManager'
    ],

    constructor: function(lockable) {
        var me = this,
            events,
            event,
            eventNames = [],
            lockedGrid = lockable.lockedGrid,
            normalGrid = lockable.normalGrid;

        me.lockable = lockable;
        me.callParent();

        // Create the unified column manager for the lockable grid assembly
        lockedGrid.visibleColumnManager.rootColumns =
            normalGrid.visibleColumnManager.rootColumns =
            lockable.visibleColumnManager =
            me.visibleColumnManager = new Ext.grid.ColumnManager(true, lockedGrid.headerCt, normalGrid.headerCt);
            
        lockedGrid.columnManager.rootColumns =
            normalGrid.columnManager.rootColumns =
            lockable.columnManager =
            me.columnManager = new Ext.grid.ColumnManager(false, lockedGrid.headerCt, normalGrid.headerCt);

        // Relay events from both sides' headerCts
        events = lockedGrid.headerCt.events;
        for (event in events) {
            if (events.hasOwnProperty(event)) {
                eventNames.push(event);
            }
        }
        me.relayEvents(lockedGrid.headerCt, eventNames);
        me.relayEvents(normalGrid.headerCt, eventNames);
    },

    getRefItems: function() {
        return this.lockable.lockedGrid.headerCt.getRefItems().concat(this.lockable.normalGrid.headerCt.getRefItems());
    },

    // This is the function which all other column access methods are based upon
    // Return the full column set for the whole Lockable assembly
    getGridColumns: function() {
        return this.lockable.lockedGrid.headerCt.getGridColumns().concat(this.lockable.normalGrid.headerCt.getGridColumns());
    },

    // Lockable uses its headerCt to gather column state
    getColumnsState: function () {
        var me = this,
            locked = me.lockable.lockedGrid.headerCt.getColumnsState(),
            normal = me.lockable.normalGrid.headerCt.getColumnsState();

        return locked.concat(normal);
    },

    // Lockable uses its headerCt to apply column state
    applyColumnsState: function (columns) {
        var me             = this,
            lockedGrid     = me.lockable.lockedGrid,
            lockedHeaderCt = lockedGrid.headerCt,
            normalHeaderCt = me.lockable.normalGrid.headerCt,
            lockedCols     = Ext.Array.toValueMap(lockedHeaderCt.items.items, 'headerId'),
            normalCols     = Ext.Array.toValueMap(normalHeaderCt.items.items, 'headerId'),
            locked         = [],
            normal         = [],
            lockedWidth    = 1,
            length         = columns.length,
            i, existing,
            lockedDefault,
            col;

        for (i = 0; i < length; i++) {
            col = columns[i];

            lockedDefault = lockedCols[col.id];
            existing = lockedDefault || normalCols[col.id];

            if (existing) {
                if (existing.applyColumnState) {
                    existing.applyColumnState(col);
                }
                if (existing.locked === undefined) {
                    existing.locked = !!lockedDefault;
                }
                if (existing.locked) {
                    locked.push(existing);
                    if (!existing.hidden && typeof existing.width == 'number') {
                        lockedWidth += existing.width;
                    }
                } else {
                    normal.push(existing);
                }
            }
        }

        // state and config must have the same columns (compare counts for now):
        if (locked.length + normal.length == lockedHeaderCt.items.getCount() + normalHeaderCt.items.getCount()) {
            lockedHeaderCt.removeAll(false);
            normalHeaderCt.removeAll(false);

            lockedHeaderCt.add(locked);
            normalHeaderCt.add(normal);

            lockedGrid.setWidth(lockedWidth);
        }
    }
});