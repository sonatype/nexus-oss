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
// feature idea to enable Ajax loading and then the content
// cache would actually make sense. Should we dictate that they use
// data or support raw html as well?

/**
 * Plugin (ptype = 'rowexpander') that adds the ability to have a Column in a grid which enables
 * a second row body which expands/contracts.  The expand/contract behavior is configurable to react
 * on clicking of the column, double click of the row, and/or hitting enter while a row is selected.
 */
Ext.define('Ext.grid.plugin.RowExpander', {
    extend: 'Ext.AbstractPlugin',
    lockableScope: 'normal',

    requires: 'Ext.grid.feature.RowBody',

    alias: 'plugin.rowexpander',

    /**
     * @cfg {Number} [columnWidth=24]
     * The width of the row expander column which contains the [+]/[-] icons to toggle row expansion.
     */
    columnWidth: 24,

    /**
     * @cfg {Ext.XTemplate} rowBodyTpl
     * An XTemplate which, when passed a record data object, produces HTML for the expanded row content.
     *
     * Note that if this plugin is applied to a lockable grid, the rowBodyTpl applies to the normal (unlocked) side.
     * See {@link #lockedTpl}
     *
     */
    rowBodyTpl: null,

    /**
     * @cfg {Ext.XTemplate} [lockedTpl]
     * An XTemplate which, when passed a record data object, produces HTML for the expanded row content *on the locked side of a lockable grid*.
     */
    lockedTpl: null,

    /**
     * @cfg {Boolean} expandOnEnter
     * `true` to toggle selected row(s) between expanded/collapsed when the enter
     * key is pressed (defaults to `true`).
     */
    expandOnEnter: true,

    /**
     * @cfg {Boolean} expandOnDblClick
     * `true` to toggle a row between expanded/collapsed when double clicked
     * (defaults to `true`).
     */
    expandOnDblClick: true,

    /**
     * @cfg {Boolean} selectRowOnExpand
     * `true` to select a row when clicking on the expander icon
     * (defaults to `false`).
     */
    selectRowOnExpand: false,

    rowBodyTrSelector: '.' + Ext.baseCSSPrefix + 'grid-rowbody-tr',
    rowBodyHiddenCls: Ext.baseCSSPrefix + 'grid-row-body-hidden',
    rowCollapsedCls: Ext.baseCSSPrefix + 'grid-row-collapsed',

    addCollapsedCls: {
        before: function(values, out) {
            var me = this.rowExpander;
            if (!me.recordsExpanded[values.record.internalId]) {
                values.itemClasses.push(me.rowCollapsedCls);
            }
        },
        priority: 500
    },

    /**
     * @event expandbody
     * **Fired through the grid's View**
     * @param {HTMLElement} rowNode The &lt;tr> element which owns the expanded row.
     * @param {Ext.data.Model} record The record providing the data.
     * @param {HTMLElement} expandRow The &lt;tr> element containing the expanded data.
     */
    /**
     * @event collapsebody
     * **Fired through the grid's View.**
     * @param {HTMLElement} rowNode The &lt;tr> element which owns the expanded row.
     * @param {Ext.data.Model} record The record providing the data.
     * @param {HTMLElement} expandRow The &lt;tr> element containing the expanded data.
     */

    setCmp: function(grid) {
        var me = this,
            features = [],
            featuresCfg, rowBodyTpl;

        me.callParent(arguments);

        me.recordsExpanded = {};
        // <debug>
        if (!me.rowBodyTpl) {
            Ext.Error.raise("The 'rowBodyTpl' config is required and is not defined.");
        }
        // </debug>

        rowBodyTpl = me.rowBodyTpl = Ext.XTemplate.getTpl(me, 'rowBodyTpl');

        featuresCfg = {
            ftype: 'rowbody',
            recordsExpanded: me.recordsExpanded,
            rowBodyHiddenCls: me.rowBodyHiddenCls,
            rowCollapsedCls: me.rowCollapsedCls,
            setupRowData: me.getRowBodyFeatureData,
            setup: me.setup
        };

        features.push(Ext.apply({
            lockableScope: 'normal',
            getRowBodyContents: function (record) {
                return rowBodyTpl.applyTemplate(record.getData());
            }
        }, featuresCfg));

        if (me.lockedTpl) {
            features.push(Ext.apply({
                lockableScope: 'locked',
                getRowBodyContents: function (record) {
                    return me.lockedTpl.applyTemplate(record.getData());
                }
            }, featuresCfg));
        }
 
        if (grid.features) {
            grid.features = Ext.Array.push(features, grid.features);
        } else {
            grid.features = features;
        }
        // NOTE: features have to be added before init (before Table.initComponent)
    },

    init: function(grid) {
        var me = this,
            reconfigurable = grid,
            view, normalView, lockedView;

        if (grid.lockable) {
            grid = grid.lockedGrid;
        }

        me.callParent(arguments);
        me.grid = grid;
        view = me.view = grid.getView();
        // Columns have to be added in init (after columns has been used to create the headerCt).
        // Otherwise, shared column configs get corrupted, e.g., if put in the prototype.
        me.addExpander();
        
        // Bind to view for key and mouse events
        // Add row processor which adds collapsed class
        me.bindView(view);
        view.addRowTpl(me.addCollapsedCls).rowExpander = me;

        // If the owning grid is lockable, then disable row height syncing - we do it here.
        // Also ensure the collapsed class is applied to the locked side by adding a row processor.
        if (grid.ownerLockable) {
            // If our client grid is the normal side of a lockable grid, we listen to its lockable owner's beforereconfigure
            reconfigurable = grid.ownerLockable;
            reconfigurable.syncRowHeight = false;
            lockedView = reconfigurable.lockedGrid.getView();

            // Bind to locked view for key and mouse events
            // Add row processor which adds collapsed class
            me.bindView(lockedView);
            lockedView.addRowTpl(me.addCollapsedCls).rowExpander = me;

            // Refresh row heights of expended rows on the locked (non body containing) side upon lock & unlock.
            // The locked side's expanded rows will collapse back because there's no body there
            reconfigurable.mon(reconfigurable, 'columnschanged', me.refreshRowHeights, me);
            reconfigurable.mon(reconfigurable.store, 'datachanged', me.refreshRowHeights, me);
        }
        reconfigurable.on('beforereconfigure', me.beforeReconfigure, me);
    },
    
    beforeReconfigure: function(grid, store, columns, oldStore, oldColumns) {
        var expander = this.getHeaderConfig();
        expander.locked = true;
        columns.unshift(expander);
    },

    /**
     * @private
     * Inject the expander column into the correct grid.
     * 
     * If we are expanding the normal side of a lockable grid, poke the column into the locked side
     */
    addExpander: function() {
        var me = this,
            expanderGrid = me.grid,
            expanderHeader = me.getHeaderConfig();

        // If this is the normal side of a lockable grid, find the other side.
        if (expanderGrid.ownerLockable) {
            expanderGrid = expanderGrid.ownerLockable.lockedGrid;
            expanderGrid.width += expanderHeader.width;
            // If the ownerLockable is configured with `enableLocking` but no initial locked columns, Lockable
            // will hide the locked grid so it won't call view.refresh. This is b/c of a bug with buffered rendering
            // not being able to determine the height of the locked grid's rows (see EXTJS-11863). To be safe,
            // whenever the rowexpander is used, set the locked grid to be shown. This works because the plugin adds
            // a column and thus there will be a row height to measure (see EXTJS-13323).
            expanderGrid.hidden = false;
        }

        expanderGrid.headerCt.insert(0, expanderHeader);

        // If a CheckboxModel, it must now put its checkbox in at position one because this
        // cell spans 2 columns.
        expanderGrid.getSelectionModel().injectCheckbox = 1;
    },

    getRowBodyFeatureData: function(record, idx, rowValues) {
        var me = this;
        me.self.prototype.setupRowData.apply(me, arguments);

        rowValues.rowBody = me.getRowBodyContents(record);
        rowValues.rowBodyCls = me.recordsExpanded[record.internalId] ? '' : me.rowBodyHiddenCls;
    },
    
    setup: function(rows, rowValues){
        var me = this;
        me.self.prototype.setup.apply(me, arguments);
        // If we are lockable, the expander column is moved into the locked side, so we don't have to span it
        if (!me.grid.ownerLockable) {
            rowValues.rowBodyColspan -= 1;
        }    
    },

    bindView: function(view) {
        if (this.expandOnEnter) {
            view.on('itemkeydown', this.onKeyDown, this);
        }
        if (this.expandOnDblClick) {
            view.on('itemdblclick', this.onDblClick, this);
        }
    },

    onKeyDown: function(view, record, row, rowIdx, e) {
        if (e.getKey() == e.ENTER) {
            var ds   = view.store,
                sels = view.getSelectionModel().getSelection(),
                ln   = sels.length,
                i = 0;

            for (; i < ln; i++) {
                rowIdx = ds.indexOf(sels[i]);
                this.toggleRow(rowIdx, sels[i]);
            }
        }
    },

    onDblClick: function(view, record, row, rowIdx, e) {
        this.toggleRow(rowIdx, record);
    },

    toggleRow: function(rowIdx, record) {
        var me = this,
            view = me.view,
            rowNode = view.getNode(rowIdx),
            normalRow = Ext.fly(rowNode, '_rowExpander'),
            lockedRow,
            nextBd = normalRow.down(me.rowBodyTrSelector, true),
            wasCollapsed = normalRow.hasCls(me.rowCollapsedCls),
            addOrRemoveCls = wasCollapsed ? 'removeCls' : 'addCls',
            ownerLockable, fireView,
            needsRefresh = view.getSizeModel().height.shrinkWrap,
            lockedRowHeight,
            normalRowHeight;

        // Suspend layouts because of possible TWO views having their height changed
        Ext.suspendLayouts();
        normalRow[addOrRemoveCls](me.rowCollapsedCls);
        Ext.fly(nextBd)[addOrRemoveCls](me.rowBodyHiddenCls);
        me.recordsExpanded[record.internalId] = wasCollapsed;

        // Only need to layout on row toggle if the view is shrinkwrapping the height
        if (needsRefresh) {
            view.refreshSize();
        }

        // Sync the height and class of the row on the locked side
        if (me.grid.ownerLockable) {
            normalRow.setHeight('');
            normalRowHeight = normalRow.getHeight();
            ownerLockable = me.grid.ownerLockable;
            fireView = ownerLockable.getView();
            view = ownerLockable.lockedGrid.view;
            // EXTJSIV-9848: in Firefox the offsetHeight of a row may not match
            // it's actual rendered height due to sub-pixel rounding errors. To ensure
            // the rows heights on both sides of the grid are the same, we have to set
            // them both.
            normalRow.setHeight(wasCollapsed ? normalRowHeight : '');

            // Process the locked side.
            lockedRow = Ext.fly(view.getNode(rowIdx), '_rowExpander');
            lockedRow[addOrRemoveCls](me.rowCollapsedCls);

            // If there is a template for expander content in the locked side, toggle that side too
            if (me.lockedTpl) {
                nextBd = lockedRow.down(me.rowBodyTrSelector, true);
                Ext.fly(nextBd)[addOrRemoveCls](me.rowBodyHiddenCls);
                lockedRowHeight = lockedRow.getHeight();
                if (wasCollapsed) {
                    if (lockedRowHeight > normalRowHeight) {
                        normalRow.setHeight(lockedRowHeight);
                    } else {
                        lockedRow.setHeight(normalRowHeight);
                    }
                } else {
                    lockedRow.setHeight('');
                }
            } else {
                lockedRow.setHeight(wasCollapsed ? normalRowHeight : '');
            }
            if (needsRefresh) {
                view.refreshSize();
            }
        } else {
            fireView = view;
        }
        fireView.fireEvent(wasCollapsed ? 'expandbody' : 'collapsebody', normalRow.dom, record, nextBd);
        // Coalesce laying out due to view size changes
        Ext.resumeLayouts(true);
    },

    // refreshRowHeights often gets called in the middle of some complex processing.
    // For example, it's called on the store's datachanged event, but it must execute
    // *after* other objects interested in datachanged have done their job.
    // Or it's called on column lock/unlock, but that could be just the start of a cross-container
    // drag/drop of column headers which then moves the column into its final place.
    // So this throws execution forwards until the idle event.
    refreshRowHeights: function() {
        Ext.globalEvents.on({
            idle: this.doRefreshRowHeights,
            scope: this,
            single: true
        });
    },

    doRefreshRowHeights: function() {
        var me = this,
            recordsExpanded = me.recordsExpanded,
            key, record,
            lockedView = me.grid.ownerLockable.lockedGrid.view,
            normalView = me.grid.ownerLockable.normalGrid.view,
            normalRow,
            lockedRow,
            lockedHeight,
            normalHeight;

        for (key in recordsExpanded) {
            if (recordsExpanded.hasOwnProperty(key)) {
                record = this.view.store.data.get(key);
                lockedRow = lockedView.getNode(record, false);
                normalRow = normalView.getNode(record, false);
                lockedRow.style.height = normalRow.style.height = '';
                lockedHeight = lockedRow.offsetHeight;
                normalHeight = normalRow.offsetHeight;
                if (normalHeight > lockedHeight) {
                    lockedRow.style.height = normalHeight + 'px';
                } else if (lockedHeight > normalHeight) {
                    normalRow.style.height = lockedHeight + 'px';
                }
            }
        }
    },

    getHeaderConfig: function() {
        var me = this;

        return {
            width: me.columnWidth,
            lockable: false,
            sortable: false,
            resizable: false,
            draggable: false,
            hideable: false,
            menuDisabled: true,
            tdCls: Ext.baseCSSPrefix + 'grid-cell-special',
            innerCls: Ext.baseCSSPrefix + 'grid-cell-inner-row-expander',
            renderer: function(value, metadata) {
                // Only has to span 2 rows if it is not in a lockable grid.
                if (!me.grid.ownerLockable) {
                    metadata.tdAttr += ' rowspan="2"';
                }
                return '<div class="' + Ext.baseCSSPrefix + 'grid-row-expander" role="presentation"></div>';
            },
            processEvent: function(type, view, cell, rowIndex, cellIndex, e, record) {
                if (e.getTarget('.' + Ext.baseCSSPrefix + 'grid-row-expander')) {
                    if (type == "click") {
                        me.toggleRow(rowIndex, record);
                        return me.selectRowOnExpand;
                    }
                }
            }
        };
    }
});
