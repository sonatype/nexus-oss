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
/**
 * Implements buffered rendering of a grid, allowing users can scroll
 * through thousands of records without the performance penalties of
 * renderering all the records on screen at once.
 *
 * The number of rows rendered outside the visible area, and the
 * buffering of pages of data from the remote server for immediate
 * rendering upon scroll can be controlled by configuring the plugin.
 *
 * You can tell it to create a larger table to provide more scrolling
 * before new rows need to be added to the leading edge of the table.
 *
 *     var myStore = Ext.create('Ext.data.Store', {
 *         // ...
 *         pageSize: 100,
 *         // ...
 *     });
 *
 *     var grid = Ext.create('Ext.grid.Panel', {
 *         // ...
 *         autoLoad: true,
 *         plugins: {
 *             ptype: 'bufferedrenderer',
 *             trailingBufferZone: 20,  // Keep 20 rows rendered in the table behind scroll
 *             leadingBufferZone: 50   // Keep 50 rows rendered in the table ahead of scroll
 *         },
 *         // ...
 *     });
 *
 * ## Implementation notes
 *
 * This class monitors scrolling of the {@link Ext.view.Table
 * TableView} within a {@link Ext.grid.Panel GridPanel} to render a small section of
 * the dataset.
 *
 */
Ext.define('Ext.grid.plugin.BufferedRenderer', {
    extend: 'Ext.AbstractPlugin',
    requires: [
        'Ext.grid.plugin.BufferedRendererTableView',
        'Ext.grid.plugin.BufferedRendererTreeView'
    ],
    alias: 'plugin.bufferedrenderer',
    lockableScope: 'both',

    /**
     * @cfg {Number}
     * @deprecated This config is now ignored.
     */
    percentageFromEdge: 0.35,

    /**
     * @cfg {Boolean} [variableRowHeight=false]
     * Configure as `true` if the row heights are not all the same height as the first row. Only configure this is needed - this will be if the
     * rows contain unpredictably sized data, or you have changed the cell's text overflow stype to `'wrap'`.
     */
    variableRowHeight: false,

    /**
     * @cfg {Number}
     * The zone which causes new rows to be appended to the view. As soon as the edge
     * of the rendered grid is this number of rows from the edge of the viewport, the view is moved.
     */
    numFromEdge: 8,

    /**
     * @cfg {Number}
     * The number of extra rows to render on the trailing side of scrolling
     * **outside the {@link #numFromEdge}** buffer as scrolling proceeds.
     */
    trailingBufferZone: 10,

    /**
     * @cfg {Number}
     * The number of extra rows to render on the leading side of scrolling
     * **outside the {@link #numFromEdge}** buffer as scrolling proceeds.
     */
    leadingBufferZone: 20,

    /**
     * @cfg {Boolean} [synchronousRender=true]
     * By default, on detection of a scroll event which brings the end of the rendered table within
     * `{@link #numFromEdge}` rows of the grid viewport, if the required rows are available in the Store,
     * the BufferedRenderer will render rows from the Store *immediately* before returning from the event handler. 
     * This setting helps avoid the impression of whitespace appearing during scrolling.
     *
     * Set this to `true` to defer the render until the scroll event handler exits. This allows for faster
     * scrolling, but also allows whitespace to be more easily scrolled into view.
     *
     */
    synchronousRender: true,

    /**
     * @cfg {Number}
     * This is the time in milliseconds to buffer load requests when scrolling the PagingScrollbar.
     */
    scrollToLoadBuffer: 200,

    // private. Initial value of zero.
    viewSize: 0,
    // private. Start at default value
    rowHeight: 21,
    /**
     * @property {Number} position
     * Current pixel scroll position of the associated {@link Ext.view.Table View}.
     */
    position: 0,
    lastScrollDirection: 1,
    bodyTop: 0,

    // Initialize this as a plugin
    init: function(grid) {
        var me = this,
            view = grid.view,
            viewListeners = {
                scroll: {
                    fn: me.onViewScroll,
                    element: 'el',
                    scope: me
                },
                boxready: me.onViewResize,
                resize: me.onViewResize,
                refresh: me.onViewRefresh,
                scope: me,
                destroyable: true
            },
            initialConfig = view.initialConfig;

        // If we are using default row heights, then do not sync row heights for efficiency
        if (!me.variableRowHeight && grid.ownerLockable) {
            grid.ownerLockable.syncRowHeight = false;
        }

        // If we are going to be handling a NodeStore then it's driven by node addition and removal, *not* refreshing.
        // The view overrides required above change the view's onAdd and onRemove behaviour to call onDataRefresh when necessary.
        if (grid.isTree || (grid.ownerLockable && grid.ownerLockable.isTree)) {
            view.blockRefresh = false;

            // Set a load mask if undefined in the view config.
            if (initialConfig && initialConfig.loadMask === undefined) {
                view.loadMask = true;
            }
        }

        if (view.positionBody) {
            viewListeners.refresh = me.onViewRefresh;
        }

        me.grid = grid;
        me.view = view;
        me.isRTL = view.getHierarchyState().rtl;
        view.bufferedRenderer = me;
        view.preserveScrollOnRefresh = true;
        view.animate = false;

        me.bindStore(view.dataSource);
        view.getViewRange = function() {
            return me.getViewRange();
        };

        me.position = 0;

        me.gridListeners = grid.on('reconfigure', me.onReconfigure, me);
        me.viewListeners = view.on(viewListeners);
    },

    bindStore: function (store) {
        var me = this,
            view = me.view,
            dataSource = view.dataSource,
            hasFeatureStore = dataSource && dataSource.isFeatureStore;

        // Don't bind the new store if it's not the same type of store as what the plugin was initialized with.
        // For example, the plugin is initialized with a GroupStore if it has a grouping feature. Then,
        // grid.reconfigure() is called, passing in a new data store here. This would be a problem, so if the
        // store to bind isn't the same type as the currently bound store, then don't allow it.
        //
        // Note that the feature should have a reconfigure listener that will bind and process the new store so
        // skipping this doesn't mean that the new store isn't processed, it just happens elsewhere.
        if (hasFeatureStore === store.isFeatureStore) {
            if (me.store) {
                me.unbindStore();
            }
            me.storeListeners = store.on({
                scope: me,
                clear: me.onStoreClear,
                destroyable: true
            });
            me.store = store;
        }

        // If the view has acquired a size, calculate a new view size and scroll range when the store changes.
        if (me.view.componentLayout.layoutCount) {
            me.onViewResize(me.view, 0, me.view.getHeight());
        }
    },

    onReconfigure: function(grid, store){
        if (store && store !== this.store) {
            this.bindStore(store);
        }
    },

    unbindStore: function() {
        this.storeListeners.destroy();
        this.store = null;
    },

    onStoreClear: function() {
        var me = this;

        // Do not do anything if view is not rendered, or if the reason for cache clearing is store destruction
        if (me.view.rendered && !me.store.isDestroyed) {
            // Temporarily disable scroll monitoring until the scroll event caused by any following *change* of scrollTop has fired.
            // Otherwise it will attempt to process a scroll on a stale view
            if (me.scrollTop !== 0) {
                me.ignoreNextScrollEvent = true;
                me.view.el.dom.scrollTop = 0;
            }

            me.bodyTop = me.scrollTop = me.position = me.scrollHeight = 0;
            me.lastScrollDirection = me.scrollOffset = null;

            // MUST delete, not null out because the calculation checks hasOwnProperty
            delete me.rowHeight;
        }
    },

    onViewRefresh: function() {
        var me = this,
            view = me.view,
            oldScrollHeight = me.scrollHeight,
            scrollHeight;

        // View has rows, delete the rowHeight property to trigger a recalculation when scrollRange is calculated
        if (view.all.getCount()) {
            // We need to calculate the table size based upon the new viewport size and current row height
            // It tests hasOwnProperty so must delete the property to make it recalculate.
            delete me.rowHeight;
        }

        // Calculates scroll range. Also calculates rowHeight if we do not have an own rowHeight property.
        // That will be the case if the view contains some rows.
        scrollHeight = me.getScrollHeight();

        if (!oldScrollHeight || scrollHeight != oldScrollHeight) {
            me.stretchView(view, scrollHeight);
        }

        if (me.scrollTop !== view.el.dom.scrollTop) {
            // The view may have refreshed and scrolled to the top, for example
            // on a sort. If so, it's as if we scrolled to the top, so we'll simulate
            // it here.
            me.onViewScroll();    
        } else {
            if (!me.hasOwnProperty('bodyTop')) {
                me.bodyTop = view.all.startIndex * me.rowHeight;
                view.el.dom.scrollTop = me.bodyTop;
                // sencha-5.0.x: view.setScrollY(me.bodyTop);
            }
            me.setBodyTop(me.bodyTop);

            // With new data, the height may have changed, so recalculate the rowHeight and viewSize.
            if (view.all.getCount()) {
                me.viewSize = 0;
                me.onViewResize(view, null, view.getHeight());
            }
        }
    },

    onViewResize: function(view, width, height, oldWidth, oldHeight) {
        var me = this,
            newViewSize;

        // So we can set correct top position to position the data rows regardless of any top border
        me.tableTopBorderWidth = view.body.getBorderWidth('t');

        // Only process first layout (the boxready event) or height resizes.
        if (!oldHeight || height !== oldHeight) {

            // Recalculate the view size in rows now that the grid view has changed height
            newViewSize = Math.ceil(height / me.rowHeight) + me.trailingBufferZone + me.leadingBufferZone;
            me.viewSize = me.setViewSize(newViewSize);
        }
    },
 
    stretchView: function(view, scrollRange) {
        var me = this,
            recordCount = (me.store.buffered ? me.store.getTotalCount() : me.store.getCount()),
            stretcherSpec,
            el;

        if (me.stretcher) {
            me.stretcher.dom.style.marginTop = (recordCount <= me.viewSize ? 0 : (scrollRange - 1)) + 'px';
        } else {
            el = view.el;

            // If the view has already been refreshed by the time we get here (eg, the grid, has undergone a reconfigure operation - which performs a refresh),
            // keep it informed of fixed nodes which it must leave alone on refresh.
            if (view.refreshCounter) {
                view.fixedNodes++;
            }

            // If this is the last page, correct the scroll range to be just enough to fit.
            if (recordCount && (me.view.all.endIndex === recordCount - 1)) {
                scrollRange = me.bodyTop + view.body.dom.offsetHeight;
            }
            stretcherSpec = {
                style: {
                    width: '1px',
                    height: '1px',
                    'marginTop': (scrollRange - 1) + 'px',
                    position: 'absolute'
                }
            };
            stretcherSpec.style[me.isRTL ? 'right' : 'left'] = 0;
            this.stretcher = el.createChild(stretcherSpec, el.dom.firstChild);
        }
    },

    setViewSize: function(viewSize) {
        if (viewSize !== this.viewSize) {

            // Must be set for getFirstVisibleRowIndex to work
            this.scrollTop = this.view.el.dom.scrollTop;

            var me = this,
                store = me.store,
                elCount = me.view.all.getCount(),
                start, end,
                lockingPartner = me.lockingPartner;

                me.viewSize = store.viewSize = viewSize;

            // If a store loads before we have calculated a viewSize, it loads me.defaultViewSize records.
            // This may be larger or smaller than the final viewSize so the store needs adjusting when the view size is calculated.
            if (elCount) {
                start = me.view.all.startIndex;
                    end = Math.min(start + viewSize - 1, (store.buffered ? store.getTotalCount() : store.getCount()) - 1);

                // While rerendering our range, the locking partner must not sync
                if (lockingPartner) {
                    lockingPartner.disable();
                }
                me.renderRange(start, end);
                if (lockingPartner) {
                    lockingPartner.enable();
                }
            }
        }
        return viewSize;
    },

    getViewRange: function() {
        var me = this,
            rows = me.view.all,
            store = me.store,
            startIndex = rows.getCount() ? rows.startIndex : rows.startIndex = (store.currentPage - 1) * store.pageSize;

        // If there already is a view range, then the startIndex from that
        if (rows.getCount()) {
            startIndex = rows.startIndex;
        }
        // Otherwise use start index of current page.
        // https://sencha.jira.com/browse/EXTJSIV-10724
        // Buffered store may be primed with loadPage(n) call rather than autoLoad which starts at index 0.
        else {
            if (!store.currentPage) {
                store.currentPage = 1;
            }
            startIndex = rows.startIndex = (store.currentPage - 1) * (store.pageSize || 1);

            // The RowNumberer uses the current page to offset the record index, so when buffered, it must always be on page 1
            store.currentPage = 1;
        }

        if (store.data.getCount()) {
            return store.getRange(startIndex, startIndex + (me.viewSize || store.defaultViewSize) - 1);
        } else {
            return [];
        }
    },

    /**
     * @private
     * Handles the Store replace event, producing a correct buffered view after the replace operation.
     */
    onReplace: function(store, startIndex, oldRecords, newRecords) {
        var me = this,
            view = me.view,
            rows = view.all,
            viewSize = me.viewSize,
            overflow,
            shortfall,
            endIndex;

        // If we don't reset the scroll position when scrolling up it will continue to grow. Eventually,
        // me.position will be greater than scrollTop and the direction will be incorrectly determined in
        // the onViewScroll listener.
        // See EXTJSIV-12776.
        me.position = view.el.dom.scrollTop;

        // Determine the end of the removal range
        endIndex = startIndex + ((oldRecords && oldRecords.length) ? (oldRecords.length - 1) : 0);

        // Replace is outside the view rendered range, do nothing
        if (endIndex < rows.startIndex || startIndex > (rows.startIndex + viewSize - 1)) {
            // Ensure the scrollRange is correct
            me.stretchView(view, me.getScrollHeight());
            return;
        }

        // The removal range encompasses the view's rows, just refresh the view
        if (startIndex <= rows.startIndex && endIndex >= rows.endIndex) {
            me.refreshView();
        }

        // The removal range range starts above the view's rows
        else if (startIndex < rows.startIndex) {

            // See if the replacement records will refill the view
            shortfall = endIndex - rows.startIndex + 1;

            // The replacement rows will refill the view, just refresh the view
            if (shortfall <= 0) {
                me.refreshView();
            }

            // Replacement range leaves the view size short, recalculate and reposition the view
            else {
                startIndex = Math.max(startIndex - shortfall - me.trailingBufferZone, 0);
                endIndex = Math.min(startIndex + viewSize, store.getCount()) - 1;

                // We have to jump upwards to close the gap to any remaining records above the view.
                // We must calculate the new position because the removal range began *above* the
                // view, so we cannot measure how much height was excised.
                me.setBodyTop(startIndex * me.rowHeight);

                me.renderRange(startIndex, endIndex);
            }
        }

        // The removal range starts within the view's rows
        else {

            // See if the replacement range overflows the view size
            overflow = startIndex + newRecords.length - 1 - rows.endIndex;

            // If it does, then just replace the overlapping rows
            if (overflow > 0) {
                startIndex = Math.max(me.getFirstVisibleRowIndex() - me.trailingBufferZone, 0);
                endIndex = Math.min(startIndex + viewSize, store.getCount() - 1);
                rows.removeRange(null, null, true);
                me.renderRange(startIndex, endIndex);
                view.selModel.onLastFocusChanged(null, view.selModel.lastFocused, true);
            }

            // Replacement range leaves the view size short, recalculate the view
            else {
                // Refresh the view
                me.refreshView();
            }
        }

        // Ensure the scrollRange is correct
        me.stretchView(view, me.getScrollHeight());
    },

    /**
     * Scrolls to and optionlly selects the specified row index **in the total dataset**.
     * 
     * @param {Number} recordIdx The zero-based position in the dataset to scroll to.
     * @param {Boolean} doSelect Pass as `true` to select the specified row.
     * @param {Function} callback A function to call when the row has been scrolled to.
     * @param {Number} callback.recordIdx The resulting record index (may have changed if the passed index was outside the valid range).
     * @param {Ext.data.Model} callback.record The resulting record from the store.
     * @param {Object} scope The scope (`this` reference) in which to execute the callback. Defaults to this BufferedRenderer.
     * 
     */
    scrollTo: function(recordIdx, doSelect, callback, scope) {
        var me = this,
            view = me.view,
            viewDom = view.el.dom,
            store = me.store,
            total = store.buffered ? store.getTotalCount() : store.getCount(),
            startIdx, endIdx,
            targetRec,
            targetRow,
            tableTop,
            groupingFeature,
            group,
            record;

        // If we have a grouping summary feature rendering the view in groups,
        // first, ensure that the record's group is expanded,
        // then work out which record in the groupStore the record is at.
        if ((groupingFeature = view.dataSource.groupingFeature) && (groupingFeature.collapsible !== false)) {

            // Sanitize the requested record
            recordIdx = Math.min(Math.max(recordIdx, 0), view.store.getCount() - 1);
            record = view.store.getAt(recordIdx);
            group = groupingFeature.getGroup(record);

            if (group.isCollapsed) {
                groupingFeature.expand(group.name);
                total = store.buffered ? store.getTotalCount() : store.getCount();
            }

            // Get the index in the GroupStore
            recordIdx = groupingFeature.indexOf(record);

        } else {

            // Sanitize the requested record
            recordIdx = Math.min(Math.max(recordIdx, 0), total - 1);
        }

        // Calculate view start index
        startIdx = Math.max(Math.min(recordIdx - (Math.floor((me.leadingBufferZone + me.trailingBufferZone) / 2)), total - me.viewSize + 1), 0);
        tableTop = Math.max(startIdx * me.rowHeight - me.tableTopBorderWidth, 0);
        endIdx = Math.min(startIdx + me.viewSize - 1, total - 1);

        store.getRange(startIdx, endIdx, {
            callback: function(range, start, end) {

                me.renderRange(start, end, true);

                targetRec = store.data.getRange(recordIdx, recordIdx)[0];
                targetRow = view.getNode(targetRec, false);
                view.body.dom.style.top = tableTop + 'px';
                me.position = me.scrollTop = viewDom.scrollTop = tableTop = Math.min(Math.max(0, tableTop - view.body.getOffsetsTo(targetRow)[1]), viewDom.scrollHeight - viewDom.clientHeight);

                // https://sencha.jira.com/browse/EXTJSIV-7166 IE 6, 7 and 8 won't scroll all the way down first time
                if (Ext.isIE) {
                    viewDom.scrollTop = tableTop;
                }
                if (doSelect) {
                    view.selModel.select(targetRec);
                }
                if (callback) {
                    callback.call(scope||me, recordIdx, targetRec);
                }
            }
        });
    },

    onViewScroll: function(e, t) {
        var me = this,
            store = me.store,
            totalCount = (store.buffered ? store.getTotalCount() : store.getCount()),
            vscrollDistance,
            scrollDirection,
            scrollTop = me.scrollTop = me.view.el.dom.scrollTop,
            scrollHandled = false;

        // Flag set when the scrollTop is programatically set to zero upon cache clear.
        // We must not attempt to process that as a scroll event.
        if (me.ignoreNextScrollEvent) {
            me.ignoreNextScrollEvent = false;
            return;
        }

        // Only check for nearing the edge if we are enabled, and if there is overflow beyond our view bounds.
        // If there is no paging to be done (Store's dataset is all in memory) we will be disabled.
        if (!(me.disabled || totalCount < me.viewSize)) {

            vscrollDistance = scrollTop - me.position;
            scrollDirection = vscrollDistance > 0 ? 1 : -1;

            // Moved at leat 20 pixels, or cvhanged direction, so test whether the numFromEdge is triggered
            if (Math.abs(vscrollDistance) >= 20 || (scrollDirection !== me.lastScrollDirection)) {
                me.lastScrollDirection = scrollDirection;
                me.handleViewScroll(me.lastScrollDirection);
                scrollHandled = true;
            }
        }

        // Keep other side synced immediately if there was no rendering work to do.
        if (!scrollHandled) {
            if (me.lockingPartner && me.lockingPartner.scrollTop !== scrollTop) {
                me.lockingPartner.view.el.dom.scrollTop = scrollTop;
            }
        }
    },

    handleViewScroll: function(direction) {
        var me                = this,
            rows              = me.view.all,
            store             = me.store,
            viewSize          = me.viewSize,
            totalCount        = (store.buffered ? store.getTotalCount() : store.getCount()),
            requestStart,
            requestEnd;

        // We're scrolling up
        if (direction == -1) {

            // If table starts at record zero, we have nothing to do
            if (rows.startIndex) {
                if ((me.getFirstVisibleRowIndex() - rows.startIndex) < me.numFromEdge) {
                    requestStart = Math.max(0, me.getLastVisibleRowIndex() + me.trailingBufferZone - viewSize);
                }
            }
        }
        // We're scrolling down
        else {

            // If table ends at last record, we have nothing to do
            if (rows.endIndex < totalCount - 1) {
                if ((rows.endIndex - me.getLastVisibleRowIndex()) < me.numFromEdge) {
                    requestStart = Math.max(0, me.getFirstVisibleRowIndex() - me.trailingBufferZone);
                }
            }
        }

        // We scrolled close to the edge and the Store needs reloading
        if (requestStart != null) {
            requestEnd = Math.min(requestStart + viewSize - 1, totalCount - 1);

            // If calculated view range has moved, then render it
            if (requestStart !== rows.startIndex || requestEnd !== rows.endIndex) {
                me.renderRange(requestStart, requestEnd);
                return;
            }
        }

        // If we did not have to render, then just sync the partner's scroll position
        if (me.lockingPartner && me.lockingPartner.view.el && me.lockingPartner.scrollTop !== me.scrollTop) {
            me.lockingPartner.view.el.dom.scrollTop = me.scrollTop;
        }
    },

    /**
     * Refreshes the current rendered range if possible. If the
     *
     */
    refreshView: function() {
        var me = this,
            selModel = me.view.selModel,
            rows = me.view.all,
            store = me.store,
            maxIndex = store.getCount() - 1,

            // New start index should be current start index unless that's now too close to the end of the store
            // to yield a full view, in which case work back from the end of the store.
            // Ensure we don't go negative.
            startIndex = Math.max(0, Math.min(rows.startIndex, maxIndex - me.viewSize + 1)),

            // New end index works forward from the new start index enduring we don't walk off the end    
            endIndex = Math.min(rows.startIndex + me.viewSize - 1, maxIndex);

        rows.removeRange(null, null, true);
        me.renderRange(startIndex, endIndex, true);
        selModel.onLastFocusChanged(null, selModel.lastFocused, true);
    },

    renderRange: function(start, end, forceSynchronous) {
        var me = this,
            rows = me.view.all,
            store = me.store;

        // Skip if we are being asked to render exactly the rows that we already have.
        // This can happen if the viewSize has to be recalculated (due to either a data refresh or a view resize event)
        // but the calculated size ends up the same.
        if (!(start === rows.startIndex && end === rows.endIndex)) {

            // If range is avaliable synchronously, process it now.
            if (store.rangeCached(start, end)) {
                me.cancelLoad();

                if (me.synchronousRender || forceSynchronous) {
                    me.onRangeFetched(null, start, end);
                } else {
                    if (!me.renderTask) {
                        me.renderTask = new Ext.util.DelayedTask(me.onRangeFetched, me, null, false);
                    }
                    // Render the new range very soon after this scroll event handler exits.
                    // If scrolling very quickly, a few more scroll events may fire before
                    // the render takes place. Each one will just *update* the arguments with which
                    // the pending invocation is called.
                    me.renderTask.delay(1, null, null, [null, start, end]);
                }
            }

            // Required range is not in the prefetch buffer. Ask the store to prefetch it.
            else {
                me.attemptLoad(start, end);
            }
        }
    },

    onRangeFetched: function(range, start, end, fromLockingPartner) {
        var me = this,
            view = me.view,
            oldStart,
            rows = view.all,
            removeCount,
            increment = 0,

            // To position rows, remove table's top border
            calculatedTop = start * me.rowHeight - me.tableTopBorderWidth,
            top,
            lockingPartner = me.lockingPartner,
            newRows,
            topAdditionSize,
            i;

        // View may have been destroyed since the DelayedTask was kicked off.
        if (view.isDestroyed) {
            return;
        }

        // If called as a callback from the Store, the range will be passed, if called from renderRange, it won't
        if (!range) {
            range = me.store.getRange(start, end);

            // Store may have been cleared since the DelayedTask was kicked off.
            if (!range) {
                return;
            }
        }

        // The new range encompasses the current range. Refresh and keep the scroll position stable
        if (start < rows.startIndex && end > rows.endIndex) {

            // How many rows will be added at top. So that we can reposition the table to maintain scroll position
            topAdditionSize = rows.startIndex - start;
            rows.clear(true);
            newRows = Ext.Array.slice(view.doAdd(range, start), 0, topAdditionSize);

            for (i = 0; i < newRows.length; i++) {
                increment -= newRows[i].offsetHeight;
            }

            // We've just added a bunch of rows to the top of our range, so move upwards to keep the row appearance stable
            me.setBodyTop(me.bodyTop + increment);
            return;
        }

        // No overlapping nodes, we'll need to render the whole range
        if (start > rows.endIndex || end < rows.startIndex) {
            rows.clear(true);
            top = calculatedTop;
        }

        if (!rows.getCount()) {
            view.doAdd(range, start);
        }
        // Moved down the dataset (content moved up): remove rows from top, add to end
        else if (end > rows.endIndex) {
            removeCount = Math.max(start - rows.startIndex, 0);

            // We only have to bump the table down by the height of removed rows if rows are not a standard size
            if (me.variableRowHeight) {
                increment = rows.item(rows.startIndex + removeCount, true).offsetTop;
            }
            rows.scroll(Ext.Array.slice(range, rows.endIndex + 1 - start), 1, removeCount, start, end);

            // We only have to bump the table down by the height of removed rows if rows are not a standard size
            if (me.variableRowHeight) {
                // Bump the table downwards by the height scraped off the top
                top = me.bodyTop + increment;
            } else {
                top = calculatedTop;
            }
        }
        // Moved up the dataset: remove rows from end, add to top
        else {
            removeCount = Math.max(rows.endIndex - end, 0);
            oldStart = rows.startIndex;
            rows.scroll(Ext.Array.slice(range, 0, rows.startIndex - start), -1, removeCount, start, end);

            // We only have to bump the table up by the height of top-added rows if rows are not a standard size
            if (me.variableRowHeight) {
                // Bump the table upwards by the height added to the top
                top = me.bodyTop - rows.item(oldStart, true).offsetTop;
            } else {
                top = calculatedTop;
            }
        }
        // The position property is the scrollTop value *at which the table was last correct*
        // MUST be set at table render/adjustment time
        me.position = me.scrollTop;

        // Position the table element. top will be undefined if fixed row height, so table position will
        // be calculated.
        if (view.positionBody) {
            me.setBodyTop(top, calculatedTop);
        }

        // Sync the other side to exactly the same range from the dataset.
        // Then ensure that we are still at exactly the same scroll position.
        if (lockingPartner && !lockingPartner.disabled && !fromLockingPartner) {
            lockingPartner.onRangeFetched(range, start, end, true);
            if (lockingPartner.scrollTop !== me.scrollTop) {
                lockingPartner.view.el.dom.scrollTop = me.scrollTop;
            }
        }
    },

    setBodyTop: function(bodyTop, calculatedTop) {
        var me = this,
            view = me.view,
            store = me.store,
            body = view.body.dom,
            delta;

        bodyTop = Math.floor(bodyTop);

        // See if there's a difference between the calculated top and the requested top.
        // This can be caused by non-standard row heights introduced by features or non-standard
        // data in rows.
        if (calculatedTop !== undefined) {
            delta = bodyTop - calculatedTop;
            bodyTop = calculatedTop;
        }
        body.style.position = 'absolute';
        body.style.top = (me.bodyTop = Math.max(bodyTop, 0)) + 'px';
        if (me.isRTL && Ext.supports.xOriginBug && view.scrollFlags.y) {
            body.style.right = -Ext.getScrollbarSize().width + 'px';
        }

        // Adjust scrollTop to keep user-perceived position the same in the case of the calculated position not matching where the actual position was.
        // Set position property so that scroll handler does not fire in response.
        if (delta) {
            me.scrollTop = me.position = view.el.dom.scrollTop -= delta;
        }

        // If this is the last page, correct the scroll range to be just enough to fit.
        if (view.all.endIndex === (store.buffered ? store.getTotalCount() : store.getCount()) - 1) {
            me.stretchView(view, me.bodyTop + body.offsetHeight);
        }
    },

    getFirstVisibleRowIndex: function(startRow, endRow, viewportTop, viewportBottom) {
        var me = this,
            view = me.view,
            rows = view.all,
            elements = rows.elements,
            clientHeight = view.el.dom.clientHeight,
            target,
            targetTop;

        // If variableRowHeight, we have to search for the first row who's bottom edge is within the viewport
        if (rows.getCount() && me.variableRowHeight) {
            if (!arguments.length) {
                startRow = rows.startIndex;
                endRow = rows.endIndex;
                viewportTop = me.scrollTop;
                viewportBottom = viewportTop + clientHeight;

                // Teleported so that body is outside viewport: Use rowHeight calculation
                if (me.bodyTop > viewportBottom || me.bodyTop + view.body.getHeight() < viewportTop) {
                    return Math.floor(me.scrollTop / me.rowHeight);
                }

                // In first, non-recursive call, begin targetting the most likely first row
                target = startRow + Math.min(me.numFromEdge + ((me.lastScrollDirection == -1) ? me.leadingBufferZone : me.trailingBufferZone), Math.floor((endRow - startRow) / 2));
            } else {
                target = startRow + Math.floor((endRow - startRow) / 2);
            }
            targetTop = me.bodyTop + elements[target].offsetTop;

            // If target is entirely above the viewport, chop downwards
            if (targetTop + elements[target].offsetHeight < viewportTop) {
                return me.getFirstVisibleRowIndex(target + 1, endRow, viewportTop, viewportBottom);
            }
            
            // Target is first
            if (targetTop <= viewportTop) {
                return target;
            }
            // Not narrowed down to 1 yet; chop upwards
            else if (target !== startRow) {
                return me.getFirstVisibleRowIndex(startRow, target - 1, viewportTop, viewportBottom);
            }
        }
        return Math.floor(me.scrollTop / me.rowHeight);
    },

    getLastVisibleRowIndex: function(startRow, endRow, viewportTop, viewportBottom) {
        var me = this,
            view = me.view,
            rows = view.all,
            elements = rows.elements,
            clientHeight = view.el.dom.clientHeight,
            target,
            targetTop, targetBottom;

        // If variableRowHeight, we have to search for the first row who's bottom edge is below the bottom of the viewport
        if (rows.getCount() && me.variableRowHeight) {
            if (!arguments.length) {
                startRow = rows.startIndex;
                endRow = rows.endIndex;
                viewportTop = me.scrollTop;
                viewportBottom = viewportTop + clientHeight;

                // Teleported so that body is outside viewport: Use rowHeight calculation
                if (me.bodyTop > viewportBottom || me.bodyTop + view.body.getHeight() < viewportTop) {
                    return Math.floor(me.scrollTop / me.rowHeight) + Math.ceil(clientHeight / me.rowHeight);
                }

                // In first, non-recursive call, begin targetting the most likely last row
                target = endRow - Math.min(me.numFromEdge + ((me.lastScrollDirection == 1) ? me.leadingBufferZone : me.trailingBufferZone), Math.floor((endRow - startRow) / 2));
            } else {
                target = startRow + Math.floor((endRow - startRow) / 2);
            }
            targetTop = me.bodyTop + elements[target].offsetTop;

            // If target is entirely below the viewport, chop upwards
            if (targetTop > viewportBottom) {
                return me.getLastVisibleRowIndex(startRow, target - 1, viewportTop, viewportBottom);
            }
            targetBottom = targetTop + elements[target].offsetHeight;

            // Target is last
            if (targetBottom >= viewportBottom) {
                return target;
            }
            // Not narrowed down to 1 yet; chop downwards
            else if (target !== endRow) {
                return me.getLastVisibleRowIndex(target + 1, endRow, viewportTop, viewportBottom);
            }
        }
        return me.getFirstVisibleRowIndex() + Math.ceil(clientHeight / me.rowHeight);
    },

    getScrollHeight: function() {
        var me = this,
            view   = me.view,
            store  = me.store,
            doCalcHeight = !me.hasOwnProperty('rowHeight'),
            storeCount = me.store.getCount();

        if (!storeCount) {
            return 0;
        }
        if (doCalcHeight) {
            if (view.all.getCount()) {
                me.rowHeight = Math.floor(view.body.getHeight() / view.all.getCount());
            }
        }
        return this.scrollHeight = Math.floor((store.buffered ? store.getTotalCount() : store.getCount()) * me.rowHeight);
    },

    attemptLoad: function(start, end) {
        var me = this;
        if (me.scrollToLoadBuffer) {
            if (!me.loadTask) {
                me.loadTask = new Ext.util.DelayedTask(me.doAttemptLoad, me, []);
            }
            me.loadTask.delay(me.scrollToLoadBuffer, me.doAttemptLoad, me, [start, end]);
        } else {
            me.store.getRange(start, end, {
                callback: me.onRangeFetched,
                scope: me,
                fireEvent: false
            });
        }
    },

    cancelLoad: function() {
        if (this.loadTask) {
            this.loadTask.cancel();
        }
    },

    doAttemptLoad:  function(start, end) {
        this.store.getRange(start, end, {
            callback: this.onRangeFetched,
            scope: this,
            fireEvent: false
        });
    },

    destroy: function() {
        var me = this,
            view = me.view;

        if (view && view.el) {
            view.el.un('scroll', me.onViewScroll, me); // un does not understand the element options
        }

        // Remove listeners from old grid, view and store
        Ext.destroy(me.viewListeners, me.storeListeners, me.gridListeners);
    }
});
