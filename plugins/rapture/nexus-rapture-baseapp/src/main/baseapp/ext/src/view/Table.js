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
 * This class encapsulates the user interface for a tabular data set.
 * It acts as a centralized manager for controlling the various interface
 * elements of the view. This includes handling events, such as row and cell
 * level based DOM events. It also reacts to events from the underlying {@link Ext.selection.Model}
 * to provide visual feedback to the user.
 *
 * This class does not provide ways to manipulate the underlying data of the configured
 * {@link Ext.data.Store}.
 *
 * This is the base class for both {@link Ext.grid.View} and {@link Ext.tree.View} and is not
 * to be used directly.
 */
Ext.define('Ext.view.Table', {
    extend: 'Ext.view.View',
    alias: 'widget.tableview',
    requires: [
        'Ext.grid.CellContext',
        'Ext.view.TableLayout',
        'Ext.view.NodeCache',
        'Ext.util.DelayedTask',
        'Ext.util.MixedCollection'
    ],

    componentLayout: 'tableview',

    baseCls: Ext.baseCSSPrefix + 'grid-view',

    unselectableCls: Ext.baseCSSPrefix + 'unselectable',

    /**
     * @cfg {String} [firstCls='x-grid-cell-first']
     * A CSS class to add to the *first* cell in every row to enable special styling for the first column.
     * If no styling is needed on the first column, this may be configured as `null`.
     */
    firstCls: Ext.baseCSSPrefix + 'grid-cell-first',

    /**
     * @cfg {String} [lastCls='x-grid-cell-last']
     * A CSS class to add to the *last* cell in every row to enable special styling for the last column.
     * If no styling is needed on the last column, this may be configured as `null`.
     */
    lastCls: Ext.baseCSSPrefix + 'grid-cell-last',

    headerRowSelector: 'tr.' + Ext.baseCSSPrefix + 'grid-header-row',

    selectedItemCls: Ext.baseCSSPrefix + 'grid-row-selected',
    beforeSelectedItemCls: Ext.baseCSSPrefix + 'grid-row-before-selected',
    selectedCellCls: Ext.baseCSSPrefix + 'grid-cell-selected',
    focusedItemCls: Ext.baseCSSPrefix + 'grid-row-focused',
    beforeFocusedItemCls: Ext.baseCSSPrefix + 'grid-row-before-focused',
    tableFocusedFirstCls: Ext.baseCSSPrefix + 'grid-table-focused-first',
    tableSelectedFirstCls: Ext.baseCSSPrefix + 'grid-table-selected-first',
    tableOverFirstCls: Ext.baseCSSPrefix + 'grid-table-over-first',
    overItemCls: Ext.baseCSSPrefix + 'grid-row-over',
    beforeOverItemCls: Ext.baseCSSPrefix + 'grid-row-before-over',
    altRowCls:   Ext.baseCSSPrefix + 'grid-row-alt',
    dirtyCls: Ext.baseCSSPrefix + 'grid-dirty-cell',
    rowClsRe: new RegExp('(?:^|\\s*)' + Ext.baseCSSPrefix + 'grid-row-(first|last|alt)(?:\\s+|$)', 'g'),
    cellRe: new RegExp(Ext.baseCSSPrefix + 'grid-cell-headerId-([^\\s]+)(?:\\s|$)', ''),
    positionBody: true,

    // cfg docs inherited
    trackOver: true,

    /**
     * Override this function to apply custom CSS classes to rows during rendering. This function should return the
     * CSS class name (or empty string '' for none) that will be added to the row's wrapping div. To apply multiple
     * class names, simply return them space-delimited within the string (e.g. 'my-class another-class').
     * Example usage:
     *
     *     viewConfig: {
     *         getRowClass: function(record, rowIndex, rowParams, store){
     *             return record.get("valid") ? "row-valid" : "row-error";
     *         }
     *     }
     *
     * @param {Ext.data.Model} record The record corresponding to the current row.
     * @param {Number} index The row index.
     * @param {Object} rowParams **DEPRECATED.** For row body use the
     * {@link Ext.grid.feature.RowBody#getAdditionalData getAdditionalData} method of the rowbody feature.
     * @param {Ext.data.Store} store The store this grid is bound to
     * @return {String} a CSS class name to add to the row.
     * @method
     */
    getRowClass: null,

    /**
     * @cfg {Boolean} stripeRows
     * True to stripe the rows.
     *
     * This causes the CSS class **`x-grid-row-alt`** to be added to alternate rows of
     * the grid. A default CSS rule is provided which sets a background color, but you can override this
     * with a rule which either overrides the **background-color** style using the `!important`
     * modifier, or which uses a CSS selector of higher specificity.
     */
    stripeRows: true,

    /**
     * @cfg {Boolean} markDirty
     * True to show the dirty cell indicator when a cell has been modified.
     */
    markDirty : true,

    /**
     * @cfg {Boolean} enableTextSelection
     * True to enable text selections.
     */
     
    ariaRole: 'grid',

    /**
     * @private
     * Simple initial tpl for TableView just to satisfy the validation within AbstractView.initComponent.
     */
    tpl: '{%values.view.tableTpl.applyOut(values, out)%}',

    tableTpl: [
        '{%',
            'var view=values.view,tableCls="' + Ext.baseCSSPrefix + '" + view.id + "-table ' + Ext.baseCSSPrefix + 'grid-table";',
        '%}',
        '<table id="{view.id}-table" class="{[tableCls]}" border="0" cellspacing="0" cellpadding="0" style="{tableStyle}" {ariaTableAttr}>',
            '{[view.renderColumnSizer(out)]}',
            '{[view.renderTHead(values, out)]}',
            '{[view.renderTFoot(values, out)]}',
            '<tbody id="{view.id}-body" {ariaTbodyAttr}>',
            '{%',
                'view.renderRows(values.rows, values.viewStartIndex, out);',
            '%}',
            '</tbody>',
        '</table>',
        {
            priority: 0
        }
    ],

    rowTpl: [
        '{%',
            'var dataRowCls = values.recordIndex === -1 ? "" : " ' + Ext.baseCSSPrefix + 'grid-data-row";',
        '%}',
        '<tr {[values.rowId ? ("id=\\"" + values.rowId + "\\"") : ""]} ',
            'data-boundView="{view.id}" ',
            'data-recordId="{record.internalId}" ',
            'data-recordIndex="{recordIndex}" ',
            'class="{[values.itemClasses.join(" ")]} {[values.rowClasses.join(" ")]}{[dataRowCls]}" ',
            '{rowAttr:attributes} tabIndex="-1" {ariaRowAttr}>',
            '<tpl for="columns">' +
                '{%',
                    'parent.view.renderCell(values, parent.record, parent.recordIndex, parent.rowIndex, xindex - 1, out, parent)',
                 '%}',
            '</tpl>',
        '</tr>',
        {
            priority: 0
        }
    ],

    cellTpl: [
        '<td class="{tdCls}" {tdAttr} {[Ext.aria ? "id=\\"" + Ext.id() + "\\"" : ""]} {ariaCellAttr}>',
            '<div {unselectableAttr} class="' + Ext.baseCSSPrefix + 'grid-cell-inner {innerCls}"',
                'style="text-align:{align};<tpl if="style">{style}</tpl>" {ariaCellInnerAttr}>{value}</div>',
        '</td>', {
            priority: 0
        }
    ],

    /**
     * @private
     * Flag to disable refreshing SelectionModel on view refresh. Table views render rows with selected CSS class already added if necessary.
     */
    refreshSelmodelOnRefresh: false,

    tableValues: {},

    // Private properties used during the row and cell render process.
    // They are allocated here on the prototype, and cleared/re-used to avoid GC churn during repeated rendering.
    rowValues: {
        itemClasses: [],
        rowClasses: []
    },
    cellValues: {
        classes: [
            Ext.baseCSSPrefix + 'grid-cell ' + Ext.baseCSSPrefix + 'grid-td' // for styles shared between cell and rowwrap
        ]
    },

    /// Private used for buffered rendering
    renderBuffer: document.createElement('div'),

    constructor: function(config) {
        // Adjust our base class if we are inside a TreePanel
        if (config.grid.isTree) {
            config.baseCls = Ext.baseCSSPrefix + 'tree-view';
        }
        this.callParent([config]);
    },

    initComponent: function() {
        var me = this,
            scroll = me.scroll;

        this.addEvents(
            /**
             * @event beforecellclick
             * Fired before the cell click is processed. Return false to cancel the default action.
             * @param {Ext.view.Table} this
             * @param {HTMLElement} td The TD element for the cell.
             * @param {Number} cellIndex
             * @param {Ext.data.Model} record
             * @param {HTMLElement} tr The TR element for the cell.
             * @param {Number} rowIndex
             * @param {Ext.EventObject} e
             */
            'beforecellclick',
            /**
             * @event cellclick
             * Fired when table cell is clicked.
             * @param {Ext.view.Table} this
             * @param {HTMLElement} td The TD element for the cell.
             * @param {Number} cellIndex
             * @param {Ext.data.Model} record
             * @param {HTMLElement} tr The TR element for the cell.
             * @param {Number} rowIndex
             * @param {Ext.EventObject} e
             */
            'cellclick',
            /**
             * @event beforecelldblclick
             * Fired before the cell double click is processed. Return false to cancel the default action.
             * @param {Ext.view.Table} this
             * @param {HTMLElement} td The TD element for the cell.
             * @param {Number} cellIndex
             * @param {Ext.data.Model} record
             * @param {HTMLElement} tr The TR element for the cell.
             * @param {Number} rowIndex
             * @param {Ext.EventObject} e
             */
            'beforecelldblclick',
            /**
             * @event celldblclick
             * Fired when table cell is double clicked.
             * @param {Ext.view.Table} this
             * @param {HTMLElement} td The TD element for the cell.
             * @param {Number} cellIndex
             * @param {Ext.data.Model} record
             * @param {HTMLElement} tr The TR element for the cell.
             * @param {Number} rowIndex
             * @param {Ext.EventObject} e
             */
            'celldblclick',
            /**
             * @event beforecellcontextmenu
             * Fired before the cell right click is processed. Return false to cancel the default action.
             * @param {Ext.view.Table} this
             * @param {HTMLElement} td The TD element for the cell.
             * @param {Number} cellIndex
             * @param {Ext.data.Model} record
             * @param {HTMLElement} tr The TR element for the cell.
             * @param {Number} rowIndex
             * @param {Ext.EventObject} e
             */
            'beforecellcontextmenu',
            /**
             * @event cellcontextmenu
             * Fired when table cell is right clicked.
             * @param {Ext.view.Table} this
             * @param {HTMLElement} td The TD element for the cell.
             * @param {Number} cellIndex
             * @param {Ext.data.Model} record
             * @param {HTMLElement} tr The TR element for the cell.
             * @param {Number} rowIndex
             * @param {Ext.EventObject} e
             */
            'cellcontextmenu',
            /**
             * @event beforecellmousedown
             * Fired before the cell mouse down is processed. Return false to cancel the default action.
             * @param {Ext.view.Table} this
             * @param {HTMLElement} td The TD element for the cell.
             * @param {Number} cellIndex
             * @param {Ext.data.Model} record
             * @param {HTMLElement} tr The TR element for the cell.
             * @param {Number} rowIndex
             * @param {Ext.EventObject} e
             */
            'beforecellmousedown',
            /**
             * @event cellmousedown
             * Fired when the mousedown event is captured on the cell.
             * @param {Ext.view.Table} this
             * @param {HTMLElement} td The TD element for the cell.
             * @param {Number} cellIndex
             * @param {Ext.data.Model} record
             * @param {HTMLElement} tr The TR element for the cell.
             * @param {Number} rowIndex
             * @param {Ext.EventObject} e
             */
            'cellmousedown',
            /**
             * @event beforecellmouseup
             * Fired before the cell mouse up is processed. Return false to cancel the default action.
             * @param {Ext.view.Table} this
             * @param {HTMLElement} td The TD element for the cell.
             * @param {Number} cellIndex
             * @param {Ext.data.Model} record
             * @param {HTMLElement} tr The TR element for the cell.
             * @param {Number} rowIndex
             * @param {Ext.EventObject} e
             */
            'beforecellmouseup',
            /**
             * @event cellmouseup
             * Fired when the mouseup event is captured on the cell.
             * @param {Ext.view.Table} this
             * @param {HTMLElement} td The TD element for the cell.
             * @param {Number} cellIndex
             * @param {Ext.data.Model} record
             * @param {HTMLElement} tr The TR element for the cell.
             * @param {Number} rowIndex
             * @param {Ext.EventObject} e
             */
            'cellmouseup',
            /**
             * @event beforecellkeydown
             * Fired before the cell key down is processed. Return false to cancel the default action.
             * @param {Ext.view.Table} this
             * @param {HTMLElement} td The TD element for the cell.
             * @param {Number} cellIndex
             * @param {Ext.data.Model} record
             * @param {HTMLElement} tr The TR element for the cell.
             * @param {Number} rowIndex
             * @param {Ext.EventObject} e
             */
            'beforecellkeydown',
            /**
             * @event cellkeydown
             * Fired when the keydown event is captured on the cell.
             * @param {Ext.view.Table} this
             * @param {HTMLElement} td The TD element for the cell.
             * @param {Number} cellIndex
             * @param {Ext.data.Model} record
             * @param {HTMLElement} tr The TR element for the cell.
             * @param {Number} rowIndex
             * @param {Ext.EventObject} e
             */
            'cellkeydown'
        );

        /**
         * @private
         * @property {Ext.dom.AbstractElement.Fly} body
         * A flyweight Ext.Element which encapsulates a reference to the view's main row containing element.
         * *Note that the `dom` reference will not be present until the first data refresh*
         */
        me.body = new Ext.dom.Element.Fly();
        me.body.id = me.id + 'gridBody';

        // Scrolling within a TableView is controlled by the scroll config of its owning GridPanel
        // It must see undefined in this property in order to leave the scroll styles alone at afterRender time
        me.autoScroll = undefined;

        // If trackOver has been turned off, null out the overCls because documented behaviour
        // in AbstractView is to turn trackOver on if overItemCls is set.
        if (!me.trackOver) {
            me.overItemCls = null;
            me.beforeOverItemCls = null;
        }

        // Convert grid scroll config to standard Component scrolling configurations.
        if (scroll === true || scroll === 'both') {
            me.autoScroll = true;
        } else if (scroll === 'horizontal') {
            me.overflowX = 'auto';
        } else if (scroll === 'vertical') {
            me.overflowY = 'auto';
        }
        me.selModel.view = me.headerCt.view = me;

        // Features need a reference to the grid.
        // Grid needs an immediate reference to its view so that the view can reliably be got from the grid during initialization
        me.grid.view = me;
        me.initFeatures(me.grid);
        delete me.grid;

        me.itemSelector = me.getItemSelector();
        me.all = new Ext.view.NodeCache(me);
        me.callParent();
    },

    getVisibleColumnManager: function(){
        var owner = this.ownerCt,
            lock = owner.ownerLockable;

        return lock ? lock.getVisibleColumnManager() : owner.getVisibleColumnManager();

    },

    /**
     * @private
     * Move a grid column from one position to another
     * @param {Number} fromIdx The index from which to move columns
     * @param {Number} toIdx The index at which to insert columns.
     * @param {Number} [colsToMove=1] The number of columns to move beginning at the `fromIdx`
     */
    moveColumn: function(fromIdx, toIdx, colsToMove) {
        var me = this,
            fragment = (colsToMove > 1) ? document.createDocumentFragment() : undefined,
            destinationCellIdx = toIdx,
            colCount = me.getGridColumns().length,
            lastIndex = colCount - 1,
            doFirstLastClasses = (me.firstCls || me.lastCls) && (toIdx === 0 || toIdx == colCount || fromIdx === 0 || fromIdx == lastIndex),
            i,
            j,
            rows, len, tr, cells,
            tables;

        // Dragging between locked and unlocked side first refreshes the view, and calls onHeaderMoved with
        // fromIndex and toIndex the same.
        if (me.rendered && toIdx !== fromIdx) {
            // Grab all rows which have column cells in.
            // That is data rows and column sizing rows.
            rows = me.el.query(me.getDataRowSelector());

            if (toIdx > fromIdx && fragment) {
                destinationCellIdx -= colsToMove;
            }

            for (i = 0, len = rows.length; i < len; i++) {
                tr = rows[i];
                cells = tr.childNodes;

                // Keep first cell class and last cell class correct *only if needed*
                if (doFirstLastClasses) {

                    if (cells.length === 1) {
                        Ext.fly(cells[0]).addCls(me.firstCls);
                        Ext.fly(cells[0]).addCls(me.lastCls);
                        continue;
                    }
                    if (fromIdx === 0) {
                        Ext.fly(cells[0]).removeCls(me.firstCls);
                        Ext.fly(cells[1]).addCls(me.firstCls);
                    } else if (fromIdx === lastIndex) {
                        Ext.fly(cells[lastIndex]).removeCls(me.lastCls);
                        Ext.fly(cells[lastIndex - 1]).addCls(me.lastCls);
                    }
                    if (toIdx === 0) {
                        Ext.fly(cells[0]).removeCls(me.firstCls);
                        Ext.fly(cells[fromIdx]).addCls(me.firstCls);
                    } else if (toIdx === colCount) {
                        Ext.fly(cells[lastIndex]).removeCls(me.lastCls);
                        Ext.fly(cells[fromIdx]).addCls(me.lastCls);
                    }
                }

                if (fragment) {
                    for (j = 0; j < colsToMove; j++) {
                        fragment.appendChild(cells[fromIdx]);
                    }
                    tr.insertBefore(fragment, cells[destinationCellIdx] || null);
                } else {
                    tr.insertBefore(cells[fromIdx], cells[destinationCellIdx] || null);
                }
            }

            // Shuffle the <colgroup> elements at the ta=op of all <tables> in the grid
            tables = me.el.query(me.getBodySelector());
            for (i = 0, len = tables.length; i < len; i++) {
                tr = tables[i];
                if (fragment) {
                    for (j = 0; j < colsToMove; j++) {
                        fragment.appendChild(tr.childNodes[fromIdx]);
                    }
                    tr.insertBefore(fragment, tr.childNodes[destinationCellIdx] || null);
                } else {
                    tr.insertBefore(tr.childNodes[fromIdx], tr.childNodes[destinationCellIdx] || null);
                }
            }
        }
    },

    // scroll the view to the top
    scrollToTop: Ext.emptyFn,

    /**
     * Add a listener to the main view element. It will be destroyed with the view.
     * @private
     */
    addElListener: function(eventName, fn, scope){
        this.mon(this, eventName, fn, scope, {
            element: 'el'
        });
    },

    /**
     * Get the leaf columns used for rendering the grid rows.
     * @private
     */
    getGridColumns: function() {
        return this.ownerCt.getVisibleColumnManager().getColumns();
    },

    /**
     * Get a leaf level header by index regardless of what the nesting
     * structure is.
     * @private
     * @param {Number} index The index
     */
    getHeaderAtIndex: function(index) {
        return this.ownerCt.getVisibleColumnManager().getHeaderAtIndex(index);
    },

    /**
     * Get the cell (td) for a particular record and column.
     * @param {Ext.data.Model} record
     * @param {Ext.grid.column.Column} column
     * @private
     */
    getCell: function(record, column) {
        var row = this.getNode(record, true);
        return Ext.fly(row).down(column.getCellSelector());
    },

    /**
     * Get a reference to a feature
     * @param {String} id The id of the feature
     * @return {Ext.grid.feature.Feature} The feature. Undefined if not found
     */
    getFeature: function(id) {
        var features = this.featuresMC;
        if (features) {
            return features.get(id);
        }
    },

    // @private
    // Finds a features by ftype in the features array
    findFeature: function(ftype) {
        if (this.features) {
            return Ext.Array.findBy(this.features, function(feature) {
                if (feature.ftype === ftype) {
                    return true;
                }
            });
        }
    },

    /**
     * Initializes each feature and bind it to this view.
     * @private
     */
    initFeatures: function(grid) {
        var me = this,
            i,
            features,
            feature,
            len;

        me.tableTpl = Ext.XTemplate.getTpl(this, 'tableTpl');
        me.rowTpl   = Ext.XTemplate.getTpl(this, 'rowTpl');
        me.cellTpl  = Ext.XTemplate.getTpl(this, 'cellTpl');

        me.featuresMC = new Ext.util.MixedCollection();
        features = me.features = me.constructFeatures();
        len = features ? features.length : 0;
        for (i = 0; i < len; i++) {
            feature = features[i];

            // inject a reference to view and grid - Features need both
            feature.view = me;
            feature.grid = grid;
            me.featuresMC.add(feature);
            feature.init(grid);
        }
    },

    renderTHead: function(values, out) {
        var headers = values.view.headerFns,
            len, i;

        if (headers) {
            for (i = 0, len = headers.length; i < len; ++i) {
                headers[i].call(this, values, out);
            }
        }
    },

    // Currently, we don't have ordering support for header/footer functions,
    // they will be pushed on at construction time. If the need does arise,
    // we can add this functionality in the future, but for now it's not
    // really necessary since currently only the summary feature uses this.
    addHeaderFn: function(){
        var headers = this.headerFns;
        if (!headers) {
            headers = this.headerFns = [];
        }
        headers.push(fn);
    },

    renderTFoot: function(values, out){
        var footers = values.view.footerFns,
            len, i;

        if (footers) {
            for (i = 0, len = footers.length; i < len; ++i) {
                footers[i].call(this, values, out);
            }
        }
    },

    addFooterFn: function(fn){
        var footers = this.footerFns;
        if (!footers) {
            footers = this.footerFns = [];
        }
        footers.push(fn);
    },

    addTableTpl: function(newTpl) {
        return this.addTpl('tableTpl', newTpl);
    },

    addRowTpl: function(newTpl) {
        return this.addTpl('rowTpl', newTpl);
    },

    addCellTpl: function(newTpl) {
        return this.addTpl('cellTpl', newTpl);
    },

    addTpl: function(which, newTpl) {
        var me = this,
            tpl,
            prevTpl;

        newTpl = Ext.Object.chain(newTpl);

        // If we have been passed an object of the form
        // {
        //      before: fn
        //      after: fn
        // }
        if (!newTpl.isTemplate) {
            newTpl.applyOut = me.tplApplyOut;
        }

        // Stop at the first TPL who's priority is less than the passed rowTpl
        for (tpl = me[which]; newTpl.priority < tpl.priority; tpl = tpl.nextTpl) {
            prevTpl = tpl;
        }

        // If we had skipped over some, link the previous one to the passed rowTpl
        if (prevTpl) {
            prevTpl.nextTpl = newTpl;
        }
        // First one
        else {
            me[which] = newTpl;
        }
        newTpl.nextTpl = tpl;
        return newTpl;
    },

    tplApplyOut: function(values, out) {
        if (this.before) {
            if (this.before(values, out) === false) {
                return;
            }
        }
        this.nextTpl.applyOut(values, out);
        if (this.after) {
            this.after(values, out);
        }
    },

    /**
     * @private
     * Converts the features array as configured, into an array of instantiated Feature objects.
     *
     * Must have no side effects other than Feature instantiation.
     *
     * MUST NOT update the this.features property, and MUST NOT update the instantiated Features.
     */
    constructFeatures: function() {
        var me = this,
            features = me.features,
            feature,
            result,
            i = 0, len;

        if (features) {
            result = [];
            len = features.length;
            for (; i < len; i++) {
                feature = features[i];
                if (!feature.isFeature) {
                    feature = Ext.create('feature.' + feature.ftype, feature);
                }
                result[i] = feature;
            }
        }
        return result;
    },

    beforeRender: function() {
        var me = this;
        me.callParent();

        if (!me.enableTextSelection) {
            me.protoEl.unselectable();
        }
    },

    // Private template method implemented starting at the AbstractView class.
    onViewScroll: function(e, t) {
        // We ignore scrolling caused by focusing
        if (!this.ignoreScroll) {
            this.callParent(arguments);
            this.fireEvent('bodyscroll', e, t);
        }
    },

    // private
    // Create the DOM element which enapsulates the passed record.
    // Used when updating existing rows, so drills down into resulting structure .
    createRowElement: function(record, index) {
        var me = this,
            div = me.renderBuffer;

        me.tpl.overwrite(div, me.collectData([record], index));
        // Return first element within node containing element
        return Ext.fly(div).down(me.getNodeContainerSelector(), true).firstChild;
    },

    // private
    // Override so that we can use a quicker way to access the row nodes.
    // They are simply all child nodes of the TBODY element.
    bufferRender: function(records, index) {
        var me = this,
            div = me.renderBuffer;

        me.tpl.overwrite(div, me.collectData(records, index));
        return Ext.Array.toArray(Ext.fly(div).down(me.getNodeContainerSelector(), true).childNodes);
    },

    collectData: function(records, startIndex) {
        this.rowValues.view = this;

        return {
            view: this,
            rows: records,
            viewStartIndex: startIndex,
            tableStyle: this.bufferedRenderer ? ('position:absolute;top:' + this.bufferedRenderer.bodyTop) : ''
        };
    },

    // Overridden implementation.
    // Called by refresh to collect the view item nodes.
    // Note that these may be wrapping rows which *contain* rows which map to records
    collectNodes: function(targetEl) {
        this.all.fill(this.getNodeContainer().childNodes, this.all.startIndex);
    },

    // Private. Called when the table changes height.
    // For example, see examples/grid/group-summary-grid.html
    // If we have flexed column headers, we need to update the header layout
    // because it may have to accommodate (or cease to accommodate) a vertical scrollbar.
    // Only do this on platforms which have a space-consuming scrollbar.
    // Only do it when vertical scrolling is enabled.
    refreshSize: function() {
        var me = this,
            grid,
            bodySelector = me.getBodySelector();

        // On every update of the layout system due to data update, capture the view's main element in our private flyweight.
        // IF there *is* a main element. Some TplFactories emit naked rows.
        if (bodySelector) {
            me.body.attach(me.el.child(bodySelector, true));
        }

        if (!me.hasLoadingHeight) {
            grid = me.up('tablepanel');

            // Suspend layouts in case the superclass requests a layout. We might too, so they
            // must be coalescsed.
            Ext.suspendLayouts();

            me.callParent();

            // Since columns and tables are not sized by generated CSS rules any more, EVERY table refresh
            // has to be followed by a layout to ensure correct table and column sizing.
            grid.updateLayout();

            Ext.resumeLayouts(true);
        }
    },

    statics: {
        getBoundView: function(node) {
            return Ext.getCmp(node.getAttribute('data-boundView'));
        }
    },

    getRecord: function(node) {
        var me = this,
            recordIndex;

        // If store.destroyStore has been called before some delayed event fires on a node, we must ignore the event.
        if (me.store.isDestroyed) {
            return;
        }

        node = me.getNode(node);
        if (node) {
            // If we're grouping, the indexes may be off
            // because of collapsed groups, so just grab it by id
            if (!me.hasActiveGrouping()) {
                recordIndex = node.getAttribute('data-recordIndex');
                if (recordIndex) {
                    recordIndex = parseInt(recordIndex, 10);
                    if (recordIndex > -1) {
                        // The index is the index in the original Store, not in a GroupStore
                        // The Grouping Feature increments the index to skip over unrendered records in collapsed groups
                        return me.store.data.getAt(recordIndex);
                    }
                }
            }
            return me.store.getByInternalId(node.getAttribute('data-recordId'));
        }
    },

    indexOf: function(node) {
        node = this.getNode(node, false);
        if (!node && node !== 0) {
            return -1;
        }
        return this.all.indexOf(node);
    },

    indexInStore: function(node) {
        node = node.isCollapsedPlaceholder ? this.getNode(node) : this.getNode(node, false);
        if (!node && node !== 0) {
            return -1;
        }
        var recordIndex = node.getAttribute('data-recordIndex');
        if (recordIndex) {
            return parseInt(recordIndex, 10);
        }
        return this.dataSource.indexOf(this.getRecord(node));
    },

    renderRows: function(rows, viewStartIndex, out) {
        var rowValues = this.rowValues,
            rowCount = rows.length,
            i;

        rowValues.view = this;
        rowValues.columns = this.ownerCt.getVisibleColumnManager().getColumns();

        for (i = 0; i < rowCount; i++, viewStartIndex++) {
            rowValues.itemClasses.length = rowValues.rowClasses.length = 0;
            this.renderRow(rows[i], viewStartIndex, out);
        }

        // Dereference objects since rowValues is a persistent on our prototype
        rowValues.view = rowValues.columns = rowValues.record = null;
    },

    /* Alternative column sizer element renderer.
    renderTHeadColumnSizer: function(out) {
        var columns = this.getGridColumns(),
            len = columns.length, i,
            column, width;

        out.push('<thead><tr class="' + Ext.baseCSSPrefix + 'grid-header-row">');
        for (i = 0; i < len; i++) {
            column = columns[i];
            width = column.hidden ? 0 : (column.lastBox ? column.lastBox.width : Ext.grid.header.Container.prototype.defaultWidth);
            out.push('<th class="', Ext.baseCSSPrefix, 'grid-cell-', columns[i].getItemId(), '" style="width:' + width + 'px"></th>');
        }
        out.push('</tr></thead>');
    },
    */

    renderColumnSizer: function(out) {
        var columns = this.getGridColumns(),
            len = columns.length, i,
            column, width;

        for (i = 0; i < len; i++) {
            column = columns[i];
            width = column.hidden ? 0 : (column.lastBox ? column.lastBox.width : Ext.grid.header.Container.prototype.defaultWidth);
            out.push('<colgroup role="presentation"><col role="presentation" class="', columns[i].getCellId(), '" style="width:' + width + 'px"></colgroup>');
        }
    },

    /**
     * @private
     * Renders the HTML markup string for a single row into the passed array as a sequence of strings, or
     * returns the HTML markup for a single row.
     *
     * @param {Ext.data.Model} record The record to render.
     * @param {String[]} [out] A string array onto which to append the resulting HTML string. If omitted,
     * the resulting HTML string is returned.
     * @return {String} **only when the out parameter is omitted** The resulting HTML string.
     */
    renderRow: function(record, rowIdx, out) {
        var me = this,
            isMetadataRecord = rowIdx === -1,
            selModel = me.selModel,
            rowValues = me.rowValues,
            itemClasses = rowValues.itemClasses,
            rowClasses = rowValues.rowClasses,
            cls,
            rowTpl = me.rowTpl;

        // Set up mandatory properties on rowValues
        rowValues.record = record;
        rowValues.recordId = record.internalId;

        // recordIndex is index in true store (NOT the data source - possibly a GroupStore)
        rowValues.recordIndex = me.store.indexOf(record);

        // rowIndex is the row number in the view.
        rowValues.rowIndex = rowIdx;
        rowValues.rowId = me.getRowId(record);
        rowValues.itemCls = rowValues.rowCls = '';
        if (!rowValues.columns) {
            rowValues.columns = me.ownerCt.getVisibleColumnManager().getColumns();
        }

        itemClasses.length = rowClasses.length = 0;

        // If it's a metadata record such as a summary record.
        // So do not decorate it with the regular CSS.
        // The Feature which renders it must know how to decorate it.
        if (!isMetadataRecord) {
            itemClasses[0] = Ext.baseCSSPrefix + "grid-row";

            if (!me.ownerCt.disableSelection && selModel.isRowSelected) {
                // Selection class goes on the outermost row, so it goes into itemClasses
                if (selModel.isRowSelected(record)) {
                    itemClasses.push(me.selectedItemCls);
                }
                /* TODO: When merging into sencha-5.0.x branch. These classes should be pushed into the itemClasses array to go on the outermost view item element */
                // Ensure selection class is added to selected records, and to the record *before* any selected record
                // When looking ahead to see if the next record is selected, ensure we do not look past the end!
                if (me.rowValues.recordIndex < me.store.getTotalCount() - 1 && selModel.isRowSelected(me.rowValues.recordIndex + 1) && !me.isRowStyleFirst(rowIdx + 1)) {
                    rowClasses.push(me.beforeSelectedItemCls);
                }
            }

            if (me.stripeRows && rowIdx % 2 !== 0) {
                rowClasses.push(me.altRowCls);
            }

            if (me.getRowClass) {
                cls = me.getRowClass(record, rowIdx, null, me.dataSource);
                if (cls) {
                    rowClasses.push(cls);
                }
            }
        }

        if (out) {
            rowTpl.applyOut(rowValues, out);
        } else {
            return rowTpl.apply(rowValues);
        }
    },

    /**
     * @private
     * Emits the HTML representing a single grid cell into the passed output stream (which is an array of strings).
     *
     * @param {Ext.grid.column.Column} column The column definition for which to render a cell.
     * @param {Number} recordIndex The row index (zero based within the {@link #store}) for which to render the cell.
     * @param {Number} rowIndex The row index (zero based within this view for which to render the cell.
     * @param {Number} columnIndex The column index (zero based) for which to render the cell.
     * @param {String[]} out The output stream into which the HTML strings are appended.
     */
    renderCell: function(column, record, recordIndex, rowIndex, columnIndex, out) {
        var me = this,
            selModel = me.selModel,
            cellValues = me.cellValues,
            classes = cellValues.classes,
            fieldValue = record.data[column.dataIndex],
            cellTpl = me.cellTpl,
            fullIndex, value, clsInsertPoint;

        cellValues.record = record;
        cellValues.column = column;
        cellValues.recordIndex = recordIndex;
        cellValues.rowIndex = rowIndex;
        cellValues.columnIndex = columnIndex;
        cellValues.cellIndex = columnIndex;
        cellValues.align = column.align;
        cellValues.tdCls = column.tdCls;
        cellValues.innerCls = column.innerCls;
        cellValues.style = cellValues.tdAttr = "";
        cellValues.unselectableAttr = me.enableTextSelection ? '' : 'unselectable="on"';

        if (column.renderer && column.renderer.call) {
            fullIndex = me.ownerCt.columnManager.getHeaderIndex(column);
            value = column.renderer.call(column.scope || me.ownerCt, fieldValue, cellValues, record, recordIndex, fullIndex, me.dataSource, me);
            if (cellValues.css) {
                // This warning attribute is used by the compat layer
                // TODO: remove when compat layer becomes deprecated
                record.cssWarning = true;
                cellValues.tdCls += ' ' + cellValues.css;
                delete cellValues.css;
            }
        } else {
            value = fieldValue;
        }
        cellValues.value = (value == null || value === '') ? '&#160;' : value;

        // Calculate classes to add to cell
        classes[1] = column.getCellId();

        // On IE8, array[len] = 'foo' is twice as fast as array.push('foo')
        // So keep an insertion point and use assignment to help IE!
        clsInsertPoint = 2;

        if (column.tdCls) {
            classes[clsInsertPoint++] = column.tdCls;
        }
        if (me.markDirty && record.isModified(column.dataIndex)) {
            classes[clsInsertPoint++] = me.dirtyCls;
        }
        if (column.isFirstVisible) {
            classes[clsInsertPoint++] = me.firstCls;
        }
        if (column.isLastVisible) {
            classes[clsInsertPoint++] = me.lastCls;
        }
        if (!me.enableTextSelection) {
            classes[clsInsertPoint++] = me.unselectableCls;
        }
        if (cellValues.tdCls) {
            classes[clsInsertPoint++] = cellValues.tdCls;
        }
        if (selModel && selModel.isCellModel && selModel.isCellSelected(me, recordIndex, columnIndex)) {
            classes[clsInsertPoint++] = (me.selectedCellCls);
        }

        // Chop back array to only what we've set
        classes.length = clsInsertPoint;

        cellValues.tdCls = classes.join(' ');

        cellTpl.applyOut(cellValues, out);

        // Dereference objects since cellValues is a persistent var in the XTemplate's scope chain
        cellValues.column = null;
    },

    /**
     * Returns the node given the passed Record, or index or node.
     * @param {HTMLElement/String/Number/Ext.data.Model} nodeInfo The node or record
     * @param {Boolean} [dataRow] `true` to return the data row (not the top level row if wrapped), `false`
     * to return the top level row.
     * @return {HTMLElement} The node or null if it wasn't found
     */
    getNode: function(nodeInfo, dataRow) {
        var fly,
            result = this.callParent(arguments);

        if (result && result.tagName) {
            if (dataRow) {
                if (!(fly = Ext.fly(result)).is(this.dataRowSelector)) {
                    return fly.down(this.dataRowSelector, true);
                }
            } else if (dataRow === false) {
                if (!(fly = Ext.fly(result)).is(this.itemSelector)) {
                    return fly.up(this.itemSelector, null, true);
                }
            }
        }
        return result;
    },

    getRowId: function(record){
        return this.id + '-record-' + record.internalId;
    },

    constructRowId: function(internalId){
        return this.id + '-record-' + internalId;
    },

    getNodeById: function(id, dataRow){
        id = this.constructRowId(id);
        return this.retrieveNode(id, dataRow);
    },

    getNodeByRecord: function(record, dataRow) {
        var id = this.getRowId(record);
        return this.retrieveNode(id, dataRow);
    },

    retrieveNode: function(id, dataRow){
        var result = this.el.getById(id, true),
            itemSelector = this.itemSelector,
            fly;

        if (dataRow === false && result) {
            if (!(fly = Ext.fly(result)).is(itemSelector)) {
                return fly.up(itemSelector, null, true);
            }
        }
        return result;
    },

    // Links back from grid rows are installed by the XTemplate as data attributes
    updateIndexes: Ext.emptyFn,

    // Outer table
    bodySelector: 'table',

    // Element which contains rows
    nodeContainerSelector: 'tbody',

    // view item (may be a wrapper)
    itemSelector: 'tr.' + Ext.baseCSSPrefix + 'grid-row',

    // row which contains cells as opposed to wrapping rows
    dataRowSelector: 'tr.' + Ext.baseCSSPrefix + 'grid-data-row',

    // cell
    cellSelector: 'td.' + Ext.baseCSSPrefix + 'grid-cell',

    // `<column sizer>`
    sizerSelector: 'col.' + Ext.baseCSSPrefix + 'grid-cell-headerId',

    innerSelector: 'div.' + Ext.baseCSSPrefix + 'grid-cell-inner',

    getNodeContainer: function() {
        return this.el.down(this.nodeContainerSelector, true);
    },

    /**
     * Returns a CSS selector which selects the outermost element(s) in this view.
     */
    getBodySelector: function() {
        return this.bodySelector + '.' + Ext.baseCSSPrefix + this.id + '-table';
    },

    /**
     * Returns a CSS selector which selects the element which contains record nodes.
     */
    getNodeContainerSelector: function() {
        return this.nodeContainerSelector;
    },

    /**
     * Returns a CSS selector which selects the element(s) which define the width of a column.
     *
     * This is used by the {@link Ext.view.TableLayout} when resizing columns.
     *
     */
    getColumnSizerSelector: function(header) {
        return this.sizerSelector + '-' + header.getItemId();
    },

    /**
     * Returns a CSS selector which selects items of the view rendered by the rowTpl
     */
    getItemSelector: function() {
        return this.itemSelector;
    },

    /**
     * Returns a CSS selector which selects a row which contains cells.
     *
     * These *may not* correspond to actual records in the store. This selector may be used
     * to identify things like total rows or header rows as injected by features which modify
     * the rowTpl.
     *
     */
    getDataRowSelector: function() {
        return this.dataRowSelector;
    },

    /**
     * Returns a CSS selector which selects a particular column if the desired header is passed,
     * or a general cell selector is no parameter is passed.
     *
     * @param {Ext.grid.column.Column} [header] The column for which to return the selector. If
     * omitted, the general cell selector which matches **ant cell** will be returned.
     *
     */
    getCellSelector: function(header) {
        return header ? header.getCellSelector() : this.cellSelector; 
    },

    /*
     * Returns a CSS selector which selects the content carrying element within cells.
     */
    getCellInnerSelector: function(header) {
        return this.getCellSelector(header) + ' ' + this.innerSelector;
    },

    /**
     * Adds a CSS Class to a specific row.
     * @param {HTMLElement/String/Number/Ext.data.Model} rowInfo An HTMLElement, index or instance of a model
     * representing this row
     * @param {String} cls
     */
    addRowCls: function(rowInfo, cls) {
        var row = this.getNode(rowInfo, false);
        if (row) {
            Ext.fly(row).addCls(cls);
        }
    },

    /**
     * Removes a CSS Class from a specific row.
     * @param {HTMLElement/String/Number/Ext.data.Model} rowInfo An HTMLElement, index or instance of a model
     * representing this row
     * @param {String} cls
     */
    removeRowCls: function(rowInfo, cls) {
        var row = this.getNode(rowInfo, false);
        if (row) {
            Ext.fly(row).removeCls(cls);
        }
    },

    setHighlightedItem: function(item) {
        var me = this,
            highlighted = me.highlightedItem;

        if (highlighted && me.el.isAncestor(highlighted) && me.isRowStyleFirst(highlighted)) {
            me.toggleRowTableCls(highlighted, me.tableOverFirstCls, false);
        }

        item = me.getNode(item, false); // make sure item is not a "data row"

        if (item && me.isRowStyleFirst(item)) {
            me.toggleRowTableCls(item, me.tableOverFirstCls, true);
        }

        me.callParent(arguments);
    },

    // GridSelectionModel invokes onRowSelect as selection changes
    onRowSelect: function(rowIdx) {
        var me = this,
            beforeSelectedItemCls = me.beforeSelectedItemCls;

        me.addRowCls(rowIdx, me.selectedItemCls);
        if (me.isRowStyleFirst(rowIdx)) {
            me.toggleRowTableCls(rowIdx, me.tableSelectedFirstCls, true);
            if (rowIdx > 0) {
                me.removeRowCls(rowIdx - 1, beforeSelectedItemCls);
            }
        } else {
            me.addRowCls(rowIdx - 1, beforeSelectedItemCls);
        }
    },

    // GridSelectionModel invokes onRowDeselect as selection changes
    onRowDeselect: function(rowIdx) {
        var me = this;

        me.removeRowCls(rowIdx, [me.selectedItemCls, me.focusedItemCls]);
        if (me.isRowStyleFirst(rowIdx)) {
            me.toggleRowTableCls(rowIdx, [me.tableFocusedFirstCls, me.tableSelectedFirstCls], false);
        } else {
            me.removeRowCls(rowIdx - 1, [me.beforeFocusedItemCls, me.beforeSelectedItemCls]);
        }
    },

    onCellSelect: function(position) {
        var cell = this.getCellByPosition(position);
        if (cell) {
            cell.addCls(this.selectedCellCls);
        }
    },

    onCellDeselect: function(position) {
        var cell = this.getCellByPosition(position, true);
        if (cell) {
            Ext.fly(cell).removeCls(this.selectedCellCls);
        }

    },

    getCellByPosition: function(position, returnDom) {
        if (position) {
            var row   = this.getNode(position.row, true),
                header = this.ownerCt.getColumnManager().getHeaderAtIndex(position.column);

            if (header && row) {
                return Ext.fly(row).down(this.getCellSelector(header), returnDom);
            }
        }
        return false;
    },

    getFocusEl: function() {
        return this.focusEl;
    },

    // GridSelectionModel invokes onRowFocus to 'highlight'
    // the last row focused
    onRowFocus: function(rowIdx, highlight, supressFocus) {
        var me = this;

        if (highlight) {
            me.addRowCls(rowIdx, me.focusedItemCls);
            if (me.isRowStyleFirst(rowIdx)) {
                me.toggleRowTableCls(rowIdx, me.tableFocusedFirstCls, true);
            } else {
                me.addRowCls(rowIdx - 1, me.beforeFocusedItemCls);
            }
            if (!supressFocus) {
                me.focusRow(rowIdx);
            }
            //this.el.dom.setAttribute('aria-activedescendant', row.id);
        } else {
            me.removeRowCls(rowIdx, me.focusedItemCls);
            if (me.isRowStyleFirst(rowIdx)) {
                me.toggleRowTableCls(rowIdx, me.tableFocusedFirstCls, false);
            } else {
                me.removeRowCls(rowIdx - 1, me.beforeFocusedItemCls);
            }
        }

        if ((Ext.isIE6 || Ext.isIE7) && !me.ownerCt.rowLines) {
            me.repaintRow(rowIdx)
        }
    },

    /**
     * Focuses a particular row and brings it into view. Will fire the rowfocus event.
     * @param {HTMLElement/String/Number/Ext.data.Model} row An HTMLElement template node, index of a template node, the id of a template node or the
     * @param {Boolean/Number} [delay] Delay the focus this number of milliseconds (true for 10 milliseconds).
     * record associated with the node.
     */
    focusRow: function(row, delay) {
        var me = this,
            record,
            focusTask = me.getFocusTask();

        if (delay) {
            focusTask.delay(Ext.isNumber(delay) ? delay : 10, me.focusRow, me, [row, false]);
            return;
        }

        // An immediate focus call must cancel any outstanding delayed focus calls.
        focusTask.cancel();

        // Do not attempt to focus if hidden or within collapsed Panel
        // Maintainer: Note that to avoid an unnecessary call to me.getNode if not visible, or another, nested if test,
        // the assignment of the row var is embedded inside the condition expression.
        if (me.isVisible(true) && (row = me.getNode(row, true))) {
            me.scrollRowIntoView(row);
            record = me.getRecord(row);
            me.selModel.setLastFocused(record);
            me.doFocus(row);
            me.fireEvent('rowfocus', record, row, me.indexInStore(row));
        }
    },

    scrollRowIntoView: function(row) {
        row = this.getNode(row, true);
        if (row) {
            Ext.fly(row).scrollIntoView(this.el, false);
        }
    },

    /**
     * Focuses a particular cell and brings it into view. Will fire the rowfocus event.
     * @param {Ext.grid.CellContext} pos The cell to select
     * @param {Boolean/Number} [delay] Delay the focus this number of milliseconds (true for 10 milliseconds).
     */
    focusCell: function(position, delay) {
        var me = this,
            cell,
            focusTask = me.getFocusTask();

        if (delay) {
            focusTask.delay(Ext.isNumber(delay) ? delay : 10, me.focusCell, me, [position, false]);
            return;
        }

        // An immediate focus call must cancel any outstanding delayed focus calls.
        focusTask.cancel();

        // Do not attempt to focus if hidden or within collapsed Panel
        // Maintainer: Note that to avoid an unnecessary call to me.getCellByPosition if not visible, or another, nested if test,
        // the assignment of the cell var is embedded inside the condition expression.
        if (me.isVisible(true) && (cell = me.getCellByPosition(position))) {
            me.scrollCellIntoView(cell);
            me.doFocus(me.getNode(position.row));
            me.fireEvent('cellfocus', position.record, cell, position);
        }
    },

    // Private
    // Implementation which saves and restores scroll position on IE.
    doFocus: function(rowDom) {
        var me = this,
            saveScroll = Ext.isIE,
            scrollLeft;

        if (saveScroll) {
            scrollLeft = me.el.getScrollLeft();
            me.ignoreScroll = true;
        }

        (me.focusEl = Ext.get(rowDom)).focus();

        if (saveScroll) {
            me.el.setScrollLeft(scrollLeft);
            me.ignoreScroll = false;
        }
    },

    scrollCellIntoView: function(cell) {
        // Allow cell context object to be passed.
        // TODO: change to .isCellContext check when implement cell context class
        if (cell.row != null && cell.column != null) {
            cell = this.getCellByPosition(cell);
        }
        if (cell) {
            Ext.fly(cell).scrollIntoView(this.el, true);
        }
    },

    /**
     * Scrolls by delta. This affects this individual view ONLY and does not
     * synchronize across views or scrollers.
     * @param {Number} delta
     * @param {String} [dir] Valid values are scrollTop and scrollLeft. Defaults to scrollTop.
     * @private
     */
    scrollByDelta: function(delta, dir) {
        dir = dir || 'scrollTop';
        var elDom = this.el.dom;
        elDom[dir] = (elDom[dir] += delta);
    },

    /**
     * @private
     * Used to test if a row being updated is a basic data row as opposed to containing extra markup
     * provided by a Feature, eg a wrapping row containing extra elements such as group header, group summary,
     * row body etc.
     *
     * If A row being updated *is not* a data row, then the elements within it which are not valid data rows
     * must be copied.
     * @param row
     * @return {Boolean} `true` if the passed element is a basic data row.
     */
    isDataRow: function(row) {
        return Ext.fly(row).hasCls(Ext.baseCSSPrefix + 'grid-data-row');
    },

    syncRowHeights: function(firstRow, secondRow) {
        firstRow = Ext.get(firstRow);
        secondRow = Ext.get(secondRow);
        firstRow.dom.style.height = secondRow.dom.style.height = '';
        var me = this,
            rowTpl = me.rowTpl,
            firstRowHeight = firstRow.dom.offsetHeight,
            secondRowHeight = secondRow.dom.offsetHeight;

        // If the two rows *need* syncing...
        if (firstRowHeight !== secondRowHeight) {

            // loop thru all of rowTpls asking them to sync the two row heights if they know how to.
            while (rowTpl) {
                if (rowTpl.syncRowHeights) {
                    // If any rowTpl in the chain returns false, quit processing
                    if (rowTpl.syncRowHeights(firstRow, secondRow) === false) {
                        break;
                    }
                }
                rowTpl = rowTpl.nextTpl;
            }

            // If that did not fix it, see if we have nested data rows, and equalize the data row heights
            firstRowHeight = firstRow.dom.offsetHeight;
            secondRowHeight = secondRow.dom.offsetHeight;
            if (firstRowHeight !== secondRowHeight) {

                // See if the real data row has been nested deeper by a Feature.
                firstRow = firstRow.down('[data-recordId]') || firstRow;
                secondRow = secondRow.down('[data-recordId]') || secondRow;

                // Yes, there's a nested data row on each side. Sync the heights of the two.
                if (firstRow && secondRow) {
                    firstRow.dom.style.height = secondRow.dom.style.height = '';
                    firstRowHeight = firstRow.dom.offsetHeight;
                    secondRowHeight = secondRow.dom.offsetHeight;

                    if (firstRowHeight > secondRowHeight) {
                        firstRow.setHeight(firstRowHeight);
                        secondRow.setHeight(firstRowHeight);
                    } else if (secondRowHeight > firstRowHeight) {
                        firstRow.setHeight(secondRowHeight);
                        secondRow.setHeight(secondRowHeight);
                    }
                }
            }
        }
    },

    onIdChanged: function(store, rec, oldId, newId, oldInternalId){
        var me = this,
            rowDom;

        if (me.viewReady) {
            rowDom = me.getNodeById(oldInternalId);
            if (rowDom) {
                rowDom.setAttribute('data-recordId', rec.internalId);
                rowDom.id = me.getRowId(rec);
            }
        }
    },

    // private
    onUpdate: function(store, record, operation, changedFieldNames) {
        var me = this,
            rowTpl = me.rowTpl,
            index,
            oldRow, oldRowDom, oldDataRow,
            newRowDom,
            newAttrs, attLen, attName, attrIndex,
            overItemCls, beforeOverItemCls,
            focusedItemCls, beforeFocusedItemCls,
            selectedItemCls, beforeSelectedItemCls,
            columns;

        if (me.viewReady) {
            // Table row being updated
            oldRowDom = me.getNodeByRecord(record, false);

            // Row might not be rendered due to buffered rendering or being part of a collapsed group...
            if (oldRowDom) {
                overItemCls = me.overItemCls;
                beforeOverItemCls = me.beforeOverItemCls;
                focusedItemCls = me.focusedItemCls;
                beforeFocusedItemCls = me.beforeFocusedItemCls;
                selectedItemCls = me.selectedItemCls;
                beforeSelectedItemCls = me.beforeSelectedItemCls;

                index = me.indexInStore(record);
                oldRow = Ext.fly(oldRowDom, '_internal');
                newRowDom = me.createRowElement(record, index);
                if (oldRow.hasCls(overItemCls)) {
                    Ext.fly(newRowDom).addCls(overItemCls);
                }
                if (oldRow.hasCls(beforeOverItemCls)) {
                    Ext.fly(newRowDom).addCls(beforeOverItemCls);
                }
                if (oldRow.hasCls(focusedItemCls)) {
                    Ext.fly(newRowDom).addCls(focusedItemCls);
                }
                if (oldRow.hasCls(beforeFocusedItemCls)) {
                    Ext.fly(newRowDom).addCls(beforeFocusedItemCls);
                }
                if (oldRow.hasCls(selectedItemCls)) {
                    Ext.fly(newRowDom).addCls(selectedItemCls);
                }
                if (oldRow.hasCls(beforeSelectedItemCls)) {
                    Ext.fly(newRowDom).addCls(beforeSelectedItemCls);
                }
                columns = me.ownerCt.getVisibleColumnManager().getColumns();

                // Copy new row attributes across. Use IE-specific method if possible.
                // In IE10, there is a problem where the className will not get updated
                // in the view, even though the className on the dom element is correct.
                // See EXTJSIV-9462
                if (Ext.isIE9m && oldRowDom.mergeAttributes) {
                    oldRowDom.mergeAttributes(newRowDom, true);
                } else {
                    newAttrs = newRowDom.attributes;
                    attLen = newAttrs.length;
                    for (attrIndex = 0; attrIndex < attLen; attrIndex++) {
                        attName = newAttrs[attrIndex].name;
                        if (attName !== 'id') {
                            oldRowDom.setAttribute(attName, newAttrs[attrIndex].value);
                        }
                    }
                }

                // If we have columns which may *need* updating (think locked side of lockable grid with all columns unlocked)
                // and the changed record is within our view, then update the view
                if (columns.length && (oldDataRow = me.getNode(oldRowDom, true))) {
                    me.updateColumns(record, oldDataRow, me.getNode(newRowDom, true), columns, changedFieldNames);
                }

                // loop thru all of rowTpls asking them to sync the content they are responsible for if any.
                while (rowTpl) {
                    if (rowTpl.syncContent) {
                        if (rowTpl.syncContent(oldRowDom, newRowDom) === false) {
                            break;
                        }
                    }
                    rowTpl = rowTpl.nextTpl;
                }

                // Since we don't actually replace the row, we need to fire the event with the old row
                // because it's the thing that is still in the DOM
                me.fireEvent('itemupdate', record, index, oldRowDom);
                me.refreshSize();
            }
        }
    },

    updateColumns: function(record, oldRowDom, newRowDom, columns, changedFieldNames) {
        var me = this,
            newAttrs, attLen, attName, attrIndex,
            colCount = columns.length,
            colIndex,
            column,
            oldCell, newCell,
            row,

            // See if our View has an editingPlugin, or if we are a locking twin, see if the top LockingView
            // has an editingPlugin.
            // We do not support one editing plugin on the top lockable and some other on the twinned views.
            editingPlugin = me.editingPlugin || (me.lockingPartner && me.ownerCt.ownerLockable.view.editingPlugin),

            // See if the found editing plugin is active.
            isEditing = editingPlugin && editingPlugin.editing,
            cellSelector = me.getCellSelector();

            // Copy new row attributes across. Use IE-specific method if possible.
            // Must do again at this level because the row DOM passed here may be the nested row in a row wrap.
            if (oldRowDom.mergeAttributes) {
                oldRowDom.mergeAttributes(newRowDom, true);
            } else {
                newAttrs = newRowDom.attributes;
                attLen = newAttrs.length;
                for (attrIndex = 0; attrIndex < attLen; attrIndex++) {
                    attName = newAttrs[attrIndex].name;
                    if (attName !== 'id') {
                        oldRowDom.setAttribute(attName, newAttrs[attrIndex].value);
                    }
                }
            }

        // Replace changed cells in the existing row structure with the new version from the rendered row.
        for (colIndex = 0; colIndex < colCount; colIndex++) {
            column = columns[colIndex];

            // If the field at this column index was changed, or column has a custom renderer
            // (which means value could rely on any other changed field) the update the cell's content.
            if (me.shouldUpdateCell(record, column, changedFieldNames)) {

                // Pluck out cells using the column's unique cell selector.
                // Becuse in a wrapped row, there may be several TD elements.
                cellSelector = me.getCellSelector(column);
                oldCell = Ext.DomQuery.selectNode(cellSelector, oldRowDom);
                newCell = Ext.DomQuery.selectNode(cellSelector, newRowDom);

                // If an editor plugin is active, we carefully replace just the *contents* of the cell.
                if (isEditing) {
                    Ext.fly(oldCell).syncContent(newCell);
                }
                // Otherwise, we simply replace whole TDs with a new version
                else {
                    // Use immediate parentNode when replacing in case the main row was a wrapping row
                    row = oldCell.parentNode;
                    row.insertBefore(newCell, oldCell);
                    row.removeChild(oldCell);
                }
            }
        }
    },

    shouldUpdateCell: function(record, column, changedFieldNames){
        // Though this may not be the most efficient, a renderer could be dependent on any field in the
        // store, so we must always update the cell.
        // If no changeFieldNames array was passed, we have to assume that that information
        // is unknown and update all cells.
        if (column.hasCustomRenderer || !changedFieldNames) {
            return true;
        }

        if (changedFieldNames) {
            var len = changedFieldNames.length,
                i, field;

            for (i = 0; i < len; ++i) {
                field = changedFieldNames[i];
                if (field === column.dataIndex || field === record.idProperty) {
                    return true;
                }
            }
        }
        return false;
    },

    /**
     * Refreshes the grid view. Sets the sort state and focuses the previously focused row.
     */
    refresh: function() {
        var me = this;

        me.callParent(arguments);
        me.headerCt.setSortState();

        // Create horizontal stretcher element if no records in view and there is overflow of the header container.
        // Element will be transient and destroyed by the next refresh.
        if (me.el && !me.all.getCount() && me.headerCt && me.headerCt.tooNarrow) {
            me.el.createChild({
                role: 'presentation',
                style:'position:absolute;height:1px;width:1px;left:' + (me.headerCt.getFullWidth() - 1) + 'px'
            });
        }

        me.refreshSelection();
    },

    refreshSelection: function() {
        var me = this,
            selModel = me.selModel,
            selected, len, i;

        if (selModel.isRowModel) {
            selected = selModel.selected.items;
            len = selected.length;
            for (i = 0; i < len; i++) {
                me.onRowSelect(me.indexOf(me.getNode(selected[i])));
            }
        }
        me.selModel.onLastFocusChanged(null, me.selModel.lastFocused, true);
    },

    processItemEvent: function(record, row, rowIndex, e) {
        // Only process the event if it occurred within a row which maps to a record in the store
        if (this.indexInStore(row) !== -1) {
            var me = this,
                cell = e.getTarget(me.getCellSelector(), row),
                cellIndex,
                map = me.statics().EventMap,
                selModel = me.getSelectionModel(),
                type = e.type,
                features = me.features,
                len = features.length,
                i, result, feature, header;

            if (type == 'keydown' && !cell && selModel.getCurrentPosition) {
                // CellModel, otherwise we can't tell which cell to invoke
                cell = me.getCellByPosition(selModel.getCurrentPosition(), true);
            }

            // cellIndex is an attribute only of TD elements. Other TplFactories must use the data-cellIndex attribute.
            if (cell) {
                if (!cell.parentNode) {
                    // If we have no parentNode, the td has been removed from the DOM, probably via an update,
                    // so just jump out since the target for the event isn't valid
                    return false;
                }
                // We can't use the cellIndex because we don't render hidden columns
                header = me.getHeaderByCell(cell);

                // Find the index of the header in the *full* (including hidden columns) leaf column set.
                // Because In 4.0.0 we rendered hidden cells, and the cellIndex included the hidden ones.
                cellIndex = me.ownerCt.getColumnManager().getHeaderIndex(header);
            } else {
                cellIndex = -1;
            }

            result = me.fireEvent('uievent', type, me, cell, rowIndex, cellIndex, e, record, row);

            if ((result === false || me.callParent(arguments) === false) && selModel.onVetoUIEvent) {
                selModel.onVetoUIEvent(type, me, cell, rowIndex, cellIndex, e, record, row);
                return false;
            }

            for (i = 0; i < len; ++i) {
                feature = features[i];
                // In some features, the first/last row might be wrapped to contain extra info,
                // such as grouping or summary, so we may need to stop the event
                if (feature.wrapsItem) {
                    if (feature.vetoEvent(record, row, rowIndex, e) === false) {
                        // If the feature is vetoing the event, there's a good chance that
                        // it's for some feature action in the wrapped row.
                        me.processSpecialEvent(e);
                        return false;
                    }
                }
            }

            // Don't handle cellmouseenter and cellmouseleave events for now
            if (type == 'mouseover' || type == 'mouseout') {
                return true;
            }

            if(!cell) {
                // if the element whose event is being processed is not an actual cell (for example if using a rowbody
                // feature and the rowbody element's event is being processed) then do not fire any "cell" events
                return true;
            }

            return !(
                // We are adding cell and feature events
                (me['onBeforeCell' + map[type]](cell, cellIndex, record, row, rowIndex, e) === false) ||
                (me.fireEvent('beforecell' + type, me, cell, cellIndex, record, row, rowIndex, e) === false) ||
                (me['onCell' + map[type]](cell, cellIndex, record, row, rowIndex, e) === false) ||
                (me.fireEvent('cell' + type, me, cell, cellIndex, record, row, rowIndex, e) === false)
            );
        } else {
            // If it's not in the store, it could be a feature event, so check here
            this.processSpecialEvent(e);
            return false;
        }
    },

    processSpecialEvent: function(e) {
        var me = this,
            features = me.features,
            ln = features.length,
            type = e.type,
            i, feature, prefix, featureTarget,
            beforeArgs, args,
            panel = me.ownerCt;

        me.callParent(arguments);

        if (type == 'mouseover' || type == 'mouseout') {
            return;
        }

        for (i = 0; i < ln; i++) {
            feature = features[i];
            if (feature.hasFeatureEvent) {
                featureTarget = e.getTarget(feature.eventSelector, me.getTargetEl());
                if (featureTarget) {
                    prefix = feature.eventPrefix;
                    // allows features to implement getFireEventArgs to change the
                    // fireEvent signature
                    beforeArgs = feature.getFireEventArgs('before' + prefix + type, me, featureTarget, e);
                    args = feature.getFireEventArgs(prefix + type, me, featureTarget, e);

                    if (
                        // before view event
                        (me.fireEvent.apply(me, beforeArgs) === false) ||
                        // panel grid event
                        (panel.fireEvent.apply(panel, beforeArgs) === false) ||
                        // view event
                        (me.fireEvent.apply(me, args) === false) ||
                        // panel event
                        (panel.fireEvent.apply(panel, args) === false)
                    ) {
                        return false;
                    }
                }
            }
        }
        return true;
    },

    onCellMouseDown: Ext.emptyFn,
    onCellMouseUp: Ext.emptyFn,
    onCellClick: Ext.emptyFn,
    onCellDblClick: Ext.emptyFn,
    onCellContextMenu: Ext.emptyFn,
    onCellKeyDown: Ext.emptyFn,
    onBeforeCellMouseDown: Ext.emptyFn,
    onBeforeCellMouseUp: Ext.emptyFn,
    onBeforeCellClick: Ext.emptyFn,
    onBeforeCellDblClick: Ext.emptyFn,
    onBeforeCellContextMenu: Ext.emptyFn,
    onBeforeCellKeyDown: Ext.emptyFn,

    /**
     * Expands a particular header to fit the max content width.
     * @deprecated Use {@link #autoSizeColumn} instead.
     */
    expandToFit: function(header) {
        this.autoSizeColumn(header);
    },

    /**
     * Sizes the passed header to fit the max content width.
     * *Note that group columns shrinkwrap around the size of leaf columns. Auto sizing a group column
     * autosizes descendant leaf columns.*
     * @param {Ext.grid.column.Column/Number} header The header (or index of header) to auto size.
     */
    autoSizeColumn: function(header) {
        if (Ext.isNumber(header)) {
            header = this.getGridColumns[header];
        }
        if (header) {
            if (header.isGroupHeader) {
                header.autoSize();
                return;
            }
            delete header.flex;
            header.setWidth(this.getMaxContentWidth(header));
        }
    },

    /**
     * Returns the max contentWidth of the header's text and all cells
     * in the grid under this header.
     * @private
     */
    getMaxContentWidth: function(header) {
        var me = this,
            cells = me.el.query(header.getCellInnerSelector()),
            originalWidth = header.getWidth(),
            i = 0,
            ln = cells.length,
            columnSizer = me.body.select(me.getColumnSizerSelector(header)),
            max = Math.max,
            widthAdjust = 0,
            maxWidth;

        if (ln > 0) {
            if (Ext.supports.ScrollWidthInlinePaddingBug) {
                widthAdjust += me.getCellPaddingAfter(cells[0]);
            }
            if (me.columnLines) {
                widthAdjust += Ext.fly(cells[0].parentNode).getBorderWidth('lr');
            }
        }

        // Set column width to 1px so we can detect the content width by measuring scrollWidth
        columnSizer.setWidth(1);

        // We are about to measure the offsetWidth of the textEl to determine how much
        // space the text occupies, but it will not report the correct width if the titleEl
        // has text-overflow:ellipsis.  Set text-overflow to 'clip' before proceeding to
        // ensure we get the correct measurement.
        header.titleEl.setStyle('text-overflow', 'clip');

        // Allow for padding round text of header
        maxWidth = header.textEl.dom.offsetWidth + header.titleEl.getPadding('lr');

        // revert to using text-overflow defined by the stylesheet
        header.titleEl.setStyle('text-overflow', '');

        for (; i < ln; i++) {
            maxWidth = max(maxWidth, cells[i].scrollWidth);
        }

        // in some browsers, the "after" padding is not accounted for in the scrollWidth
        maxWidth += widthAdjust;

        // 40 is the minimum column width.  TODO: should this be configurable?
        maxWidth = max(maxWidth, 40);

        // Set column width back to original width
        columnSizer.setWidth(originalWidth);

        return maxWidth;
    },

    getPositionByEvent: function(e) {
        var me       = this,
            cellNode = e.getTarget(me.cellSelector),
            rowNode  = e.getTarget(me.itemSelector),
            record   = me.getRecord(rowNode),
            header   = me.getHeaderByCell(cellNode);

        return me.getPosition(record, header);
    },

    getHeaderByCell: function(cell) {
        if (cell) {
            var match = cell.className.match(this.cellRe);
            if (match && match[1]) {
                return this.ownerCt.getVisibleColumnManager().getHeaderById(match[1]);
            }
        }
        return false;
    },

    /**
     * @param {Object} position The current row and column: an object containing the following properties:
     *
     * - row - The row index
     * - column - The column index
     *
     * @param {String} direction 'up', 'down', 'right' and 'left'
     * @param {Ext.EventObject} e event
     * @param {Boolean} preventWrap Set to true to prevent wrap around to the next or previous row.
     * @param {Function} verifierFn A function to verify the validity of the calculated position.
     * When using this function, you must return true to allow the newPosition to be returned.
     * @param {Object} scope Scope to run the verifierFn in
     * @returns {Ext.grid.CellContext} An object encapsulating the unique cell position.
     *
     * @private
     */
    walkCells: function(pos, direction, e, preventWrap, verifierFn, scope) {

        // Caller (probably CellModel) had no current position. This can happen
        // if the main el is focused and any navigation key is presssed.
        if (!pos) {
            return false;
        }

        var me           = this,
            row          = pos.row,
            column       = pos.column,
            rowCount     = me.dataSource.getCount(),
            allCols      = me.ownerCt.getColumnManager(),
            visible      = me.ownerCt.getVisibleColumnManager(),
            firstIndex   = allCols.getHeaderIndex(visible.getFirst()),
            lastIndex    = allCols.getHeaderIndex(visible.getLast()),
            newRow       = row,
            newColumn    = column,
            activeHeader = allCols.getHeaderAtIndex(column);

        // no active header or its currently hidden
        if (!activeHeader || activeHeader.hidden || !rowCount) {
            return false;
        }

        e = e || {};
        direction = direction.toLowerCase();
        switch (direction) {
            case 'right':
                // has the potential to wrap if its last
                if (column === lastIndex) {
                    // if bottom row and last column, deny right
                    if (preventWrap || row === rowCount - 1) {
                        return false;
                    }
                    if (!e.ctrlKey) {
                        // otherwise wrap to nextRow and firstCol
                        newRow = me.walkRows(row, 1);
                        if (newRow !== row) {
                            newColumn = firstIndex;
                        }
                    }
                // go right
                } else {
                    if (!e.ctrlKey) {
                        newColumn = allCols.getHeaderIndex(visible.getNextSibling(activeHeader));
                    } else {
                        newColumn = lastIndex;
                    }
                }
                break;

            case 'left':
                // has the potential to wrap
                if (column === firstIndex) {
                    // if top row and first column, deny left
                    if (preventWrap || row === 0) {
                        return false;
                    }
                    if (!e.ctrlKey) {
                        // otherwise wrap to prevRow and lastIndex
                        newRow = me.walkRows(row, -1);
                        if (newRow !== row) {
                            newColumn = lastIndex;
                        }
                    }
                // go left
                } else {
                    if (!e.ctrlKey) {
                        newColumn = allCols.getHeaderIndex(visible.getPreviousSibling(activeHeader));
                    } else {
                        newColumn = firstIndex;
                    }
                }
                break;

            case 'up':
                // if top row, deny up
                if (row === 0) {
                    return false;
                // go up
                } else {
                    if (!e.ctrlKey) {
                        newRow = me.walkRows(row, -1);
                    } else {
                        // Go to first row by walking down from row -1
                        newRow = me.walkRows(-1, 1);
                    }
                }
                break;

            case 'down':
                // if bottom row, deny down
                if (row === rowCount - 1) {
                    return false;
                // go down
                } else {
                    if (!e.ctrlKey) {
                        newRow = me.walkRows(row, 1);
                    } else {
                        // Go to first row by walking up from beyond the last row
                        newRow = me.walkRows(rowCount, -1);
                    }
                }
                break;
        }

        if (verifierFn && verifierFn.call(scope || me, {row: newRow, column: newColumn}) !== true) {
            return false;
        }

        newColumn = allCols.getHeaderAtIndex(newColumn);
        return new Ext.grid.CellContext(me).setPosition(newRow, newColumn);
    },

    /**
     * Increments the passed row index by the passed increment which may be +ve or -ve
     *
     * Skips hidden rows.
     *
     * If no row is visible in the specified direction, returns the input row index unchanged.
     * @param {Number} startRow The zero-based row index to start from.
     * @param {Number} distance The distance to move the row by. May be +ve or -ve.
     */
    walkRows: function(startRow, distance) {
        // Note that we use the **dataSource** here because row indices mean view row indices
        // so records in collapsed groups must be omitted.
        var me = this,
            moved = 0,
            lastValid = startRow,
            node,
            last = (me.dataSource.buffered ? me.dataSource.getTotalCount() : me.dataSource.getCount()) - 1,
            limit = (distance < 0) ? 0 : last,
            increment = limit ? 1 : -1,
            result = startRow;

        do {
            // Walked off the end: return the last encountered valid row
            if (limit ? result >= limit : result <= 0) {
                return lastValid || limit;
            }

            // Move the result pointer on by one position. We have to count intervening VISIBLE nodes
            result += increment;

            // Stepped onto VISIBLE record: Increment the moved count.
            // We must not count stepping onto a non-rendered record as a move.
            if ((node = Ext.fly(me.getNode(result, true))) && node.isVisible(true)) {
                moved += increment;
                lastValid = result;
            }
        } while (moved !== distance);
        return result;
    },

    /**
     * Navigates from the passed record by the passed increment which may be +ve or -ve
     *
     * Skips hidden records.
     *
     * If no record is visible in the specified direction, returns the starting record index unchanged.
     * @param {Ext.data.Model} startRec The Record to start from.
     * @param {Number} distance The distance to move from the record. May be +ve or -ve.
     */
    walkRecs: function(startRec, distance) {
        // Note that we use the **store** to access the records by index because the dataSource omits records in collapsed groups.
        // This is used by selection models which use the **store**
        var me = this,
            moved = 0,
            lastValid = startRec,
            node,
            last = (me.store.buffered ? me.store.getTotalCount() : me.store.getCount()) - 1,
            limit = (distance < 0) ? 0 : last,
            increment = limit ? 1 : -1,
            testIndex = me.store.indexOf(startRec),
            rec;

        do {
            // Walked off the end: return the last encountered valid record
            if (limit ? testIndex >= limit : testIndex <= 0) {
                return lastValid;
            }

            // Move the result pointer on by one position. We have to count intervening VISIBLE nodes
            testIndex += increment;

            // Stepped onto VISIBLE record: Increment the moved count.
            // We must not count stepping onto a non-rendered record as a move.
            rec = me.store.getAt(testIndex);
            if ((node = Ext.fly(me.getNodeByRecord(rec, true))) && node.isVisible(true)) {
                moved += increment;
                lastValid = rec;
            }
        } while (moved !== distance);
        return lastValid;
    },

    getFirstVisibleRowIndex: function() {
        var me = this,
            count = (me.dataSource.buffered ? me.dataSource.getTotalCount() : me.dataSource.getCount()),
            result = me.indexOf(me.all.first()) - 1;

        do {
            result += 1;
            if (result === count) {
                return;
            }
        } while (!Ext.fly(me.getNode(result, true)).isVisible(true));
        return result;
    },

    getLastVisibleRowIndex: function() {
        var me = this,
            result = me.indexOf(me.all.last());

        do {
            result -= 1;
            if (result === -1) {
                return;
            }
        } while (!Ext.fly(me.getNode(result, true)).isVisible(true));
        return result;
    },

    getHeaderCt: function() {
        return this.headerCt;
    },

    getPosition: function(record, header) {
        return new Ext.grid.CellContext(this).setPosition(record, header);
    },

    beforeDestroy: function() {
        var me = this;

        if (me.rendered) {
            me.el.removeAllListeners();
        }
        me.callParent(arguments);
    },

    onDestroy: function() {
        var me = this,
            features = me.featuresMC,
            len,
            i;

        if (features) {
            for (i = 0, len = features.getCount(); i < len; ++i) {
                features.getAt(i).destroy();
            }
        }
        me.featuresMC = null;
        this.callParent(arguments);
    },

    // after adding a row stripe rows from then on
    onAdd: function(ds, records, index) {
        var me = this,
            selModel = me.selModel,
            nextIndex, isNextRowSelected, isNextRowFocused;

        me.callParent(arguments);
        me.doStripeRows(index);

        if (me.rendered && selModel.isRowModel && !records[0].isCollapsedPlaceholder) {
            nextIndex = index + records.length;
            isNextRowSelected = selModel.isRowSelected(nextIndex);
            isNextRowFocused = me.indexOf(selModel.lastFocused) === (nextIndex);
            if (isNextRowSelected || isNextRowFocused) {
                // calling onRowDeselect ensures before focus/selected border gets removed from
                // previous row if the row previously at this index was selected or focused.
                me.onRowDeselect(index);
            }
            if (isNextRowSelected) {
                me.onRowSelect(nextIndex);
            }
            if (selModel.isRowSelected(index)) {
                me.onRowSelect(index);
            }
        }
        me.selModel.onLastFocusChanged(null, me.selModel.lastFocused, true);
    },

    // after removing a row stripe rows from then on
    onRemove: function(ds, records, indexes) {
        var me = this,
            index,
            selModel = me.selModel,
            len = indexes.length,
            i = 0,
            delta = 0,
            record;

        // Ensure "beforeSelected" classes are maintained correctly.
        // Some of the removed rows my have been carrying the beforeSelected class.
        me.callParent(arguments);
        me.doStripeRows(indexes[0]);

        if (me.rendered && selModel.isRowModel && !records[0].isCollapsedPlaceholder) {
            for (; i < len; i++, delta++) {
                index = indexes[i] - delta;
                record = me.store.getAt(index);
                me.onRowDeselect(index);
                if (selModel.isRowSelected(record) && me.getNode(record)) {
                    me.onRowSelect(index);
                }
            }
        }
        selModel.onLastFocusChanged(null, selModel.lastFocused, true);
    },

    /**
     * Stripes rows from a particular row index.
     * @param {Number} startRow
     * @param {Number} [endRow] argument specifying the last row to process.
     * By default process up to the last row.
     * @private
     */
    doStripeRows: function(startRow, endRow) {
        var me = this,
            rows,
            rowsLn,
            i,
            row;

        // ensure stripeRows configuration is turned on
        if (me.rendered && me.stripeRows) {
            rows = me.getNodes(startRow, endRow);

            for (i = 0, rowsLn = rows.length; i < rowsLn; i++) {
                row = rows[i];
                // Remove prior applied row classes.
                row.className = row.className.replace(me.rowClsRe, ' ');
                startRow++;
                // Every odd row will get an additional cls
                if (startRow % 2 === 0) {
                    row.className += (' ' + me.altRowCls);
                }
            }
        }
    },

    repaintRow: function(rowIdx) {
        var node = this.getNode(rowIdx),
            tds,
            i;

        if (node) {
            tds = node.childNodes;
            i = tds.length;
            while (i--) {
                tds[i].className = tds[i].className;
            }
        }
    },

    // private
    // returns the table that gains a top border when the first grid row is focused, selected,
    // or hovered.  Usually the main grid table but can be a sub-table, if a grouping
    // feature is being used.
    getRowStyleTableEl: function(item /* view item or row index */) {
       var me = this;

        if (!item.tagName) {
            item = this.getNode(item);
        }
        return (me.hasActiveGrouping() ? Ext.fly(item) : this.el).down('table.' + Ext.baseCSSPrefix + 'grid-table');
    },

    // private
    // adds or removes a css class to or from the table that gains a top border when the
    // first grid row is focused, selected, or hovered.  Usually the main grid table but
    // can be a sub-table, if a grouping feature is being used.
    toggleRowTableCls: function(item /* view item or row index */, cls, enabled) {
       var me = this,
           table, root;

        if (!item.tagName) {
            item = this.getNode(item);
        }

        root = me.isGrouping ? Ext.fly(item) : this.el;

        if (root) {
            table = root.down('table.' + Ext.baseCSSPrefix + 'grid-table');
        }

        if (table) {
            table[enabled ? 'addCls' : 'removeCls'](cls);
        }
    },

    // private
    // returns true if the row should be treated as the first row stylistically.  Typically
    // only returns true for the first row in a grid.  Returns true for the first row
    // in each group of a grouped grid.
    isRowStyleFirst: function(item /* view item or row index */) {
        var me = this,
            index;

        // An item not in the view
        if (item === -1) {
            return false;
        }

        if (!item.tagName) {
            index = item;
            item = this.getNode(item);
        } else {
            index = me.indexOf(item);
        }

        return (!index || me.hasActiveGrouping() && item && Ext.fly(item).hasCls(Ext.baseCSSPrefix + 'grid-group-row'));
    },

    hasActiveGrouping: function(){
        return this.isGrouping && this.store.isGrouped();
    },

    getCellPaddingAfter: function(cell) {
        return Ext.fly(cell).getPadding('r');
    },

    hasVerticalScroll: function(){
        var me = this,
            first;

        if (me.ownerCt.isLocked || !me.scrollFlags.y) {
            return false;
        }

        first = me.el.down('table');
        if (!first) {
            return false;
        }

        return this.getHeight() < first.getHeight(); 
    }

});
