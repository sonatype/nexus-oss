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
 * Plugin to add header resizing functionality to a HeaderContainer.
 * Always resizing header to the left of the splitter you are resizing.
 */
Ext.define('Ext.grid.plugin.HeaderResizer', {
    extend: 'Ext.AbstractPlugin',
    requires: ['Ext.dd.DragTracker', 'Ext.util.Region'],
    alias: 'plugin.gridheaderresizer',

    disabled: false,

    config: {
        /**
         * @cfg {Boolean} dynamic
         * True to resize on the fly rather than using a proxy marker.
         * @accessor
         */
        dynamic: false
    },

    colHeaderCls: Ext.baseCSSPrefix + 'column-header',

    minColWidth: 40,
    maxColWidth: 1000,
    wResizeCursor: 'col-resize',
    eResizeCursor: 'col-resize',
    // not using w and e resize bc we are only ever resizing one
    // column
    //wResizeCursor: Ext.isWebKit ? 'w-resize' : 'col-resize',
    //eResizeCursor: Ext.isWebKit ? 'e-resize' : 'col-resize',

    init: function(headerCt) {
        this.headerCt = headerCt;
        headerCt.on('render', this.afterHeaderRender, this, {single: true});
    },

    /**
     * @private
     * AbstractComponent calls destroy on all its plugins at destroy time.
     */
    destroy: function() {
        var tracker = this.tracker;
        if (tracker) {
            delete tracker.onBeforeStart;
            delete tracker.onStart;
            delete tracker.onDrag;
            delete tracker.onEnd;
            tracker.destroy();
            this.tracker = null;
        }
    },

    afterHeaderRender: function() {
        var me = this,
            headerCt = this.headerCt,
            el = headerCt.el;

        headerCt.mon(el, 'mousemove', this.onHeaderCtMouseMove, this);
        me.markerOwner = me.ownerGrid = me.headerCt.up('tablepanel');
        if (me.markerOwner.ownerLockable) {
            me.markerOwner = me.markerOwner.ownerLockable;
        }

        me.tracker = new Ext.dd.DragTracker({
            disabled: me.disabled,
            onBeforeStart: Ext.Function.bind(me.onBeforeStart, me),
            onStart: Ext.Function.bind(me.onStart, me),
            onDrag: Ext.Function.bind(me.onDrag, me),
            onEnd: Ext.Function.bind(me.onEnd, me),
            tolerance: 3,
            autoStart: 300,
            el: el
        });
    },

    // As we mouse over individual headers, change the cursor to indicate
    // that resizing is available, and cache the resize target header for use
    // if/when they mousedown.
    onHeaderCtMouseMove: function(e) {
        var me = this,
            headerEl, overHeader, resizeHeader,
            headers;

        if (me.headerCt.dragging || me.disabled) {
            if (me.activeHd) {
                me.activeHd.el.dom.style.cursor = '';
                delete me.activeHd;
            }
        } else {
            headerEl = e.getTarget('.' + me.colHeaderCls, 3, true);

            if (headerEl) {
                overHeader = Ext.getCmp(headerEl.id);

                // If near the right edge, we're resizing the column we are over.
                if (overHeader.isOnRightEdge(e)) {
                    
                    // Cannot resize the only column in a forceFit grid.
                    if (me.headerCt.visibleColumnManager.getColumns().length === 1 && me.headerCt.forceFit) {
                        return;
                    }

                    resizeHeader = overHeader;
                }
                // Else... we might be near the right edge
                else if (overHeader.isOnLeftEdge(e)) {
                    // Extract previous visible leaf header
                    headers = me.headerCt.visibleColumnManager.getColumns();
                    resizeHeader = headers[Ext.Array.indexOf(headers, overHeader) - 1];

                    // If there wasn't one, and we are the normal side of a lockable assembly then
                    // use the last visible leaf header of the locked side.
                    if (!resizeHeader && me.ownerGrid.ownerLockable && !me.ownerGrid.isLocked) {
                        headers = me.ownerGrid.ownerLockable.lockedGrid.headerCt.visibleColumnManager.getColumns();
                        resizeHeader = headers[headers.length - 1];
                    }
                }
                // We *are* resizing
                if (resizeHeader) {

                    // If we're attempting to resize a group header, that cannot be resized,
                    // so find its last visible leaf header; Group headers are sized
                    // by the size of their child headers.
                    if (resizeHeader.isGroupHeader) {
                        headers = resizeHeader.getGridColumns();
                        resizeHeader = headers[headers.length - 1];
                    }

                    // Check if the header is resizable. Continue checking the old "fixed" property, bug also
                    // check whether the resizable property is set to false.
                    if (resizeHeader && !(resizeHeader.fixed || (resizeHeader.resizable === false))) {
                        me.activeHd = resizeHeader;
                        overHeader.el.dom.style.cursor = me.eResizeCursor;
                        if (overHeader.triggerEl) {
                            overHeader.triggerEl.dom.style.cursor = me.eResizeCursor;
                        }
                    }
                // reset
                } else {
                    overHeader.el.dom.style.cursor = '';
                    if (overHeader.triggerEl) {
                        overHeader.triggerEl.dom.style.cursor = '';
                    }
                    me.activeHd = null;
                }
            }
        }
    },

    // only start when there is an activeHd
    onBeforeStart : function(e) {
        var me = this;

        // cache the activeHd because it will be cleared.
        me.dragHd = me.activeHd;

        if (!!me.dragHd && !me.headerCt.dragging) {

            // Calculate how far off the right marker line the mouse pointer is.
            // This will be the xDelta during the following drag operation.
            me.xDelta = me.dragHd.getX() + me.dragHd.getWidth() - me.tracker.getXY()[0];
            this.tracker.constrainTo = this.getConstrainRegion();
            return true;
        } else {
            me.headerCt.dragging = false;
            return false;
        }
    },

    // get the region to constrain to, takes into account max and min col widths
    getConstrainRegion: function() {
        var me       = this,
            dragHdEl = me.dragHd.el,
            rightAdjust = 0,
            nextHd,
            lockedGrid,
            maxColWidth = me.headerCt.getWidth() - me.headerCt.visibleColumnManager.getColumns().length * me.minColWidth;

        // If forceFit, then right constraint is based upon not being able to force the next header
        // beyond the minColWidth. If there is no next header, then the header may not be expanded.
        if (me.headerCt.forceFit) {
            nextHd = me.dragHd.nextNode('gridcolumn:not([hidden]):not([isGroupHeader])');
            if (nextHd && me.headerInSameGrid(nextHd)) {
                rightAdjust = nextHd.getWidth() - me.minColWidth;
            }
        }

        // If resize header is in a locked grid, the maxWidth has to be 30px within the available locking grid's width
        else if ((lockedGrid = me.dragHd.up('tablepanel')).isLocked) {
            rightAdjust = me.dragHd.up('[scrollerOwner]').getTargetEl().getWidth() - lockedGrid.getWidth() - (lockedGrid.ownerLockable.normalGrid.visibleColumnManager.getColumns().length * me.minColWidth + Ext.getScrollbarSize().width);
        }

        // Else ue our default max width
        else {
            rightAdjust = maxColWidth - dragHdEl.getWidth();
        }

        return me.adjustConstrainRegion(
            dragHdEl.getRegion(),
            0,
            rightAdjust - me.xDelta,
            0,
            me.minColWidth - me.xDelta
        );
    },

    // initialize the left and right hand side markers around
    // the header that we are resizing
    onStart: function(e){
        var me       = this,
            dragHd   = me.dragHd,
            width    = dragHd.el.getWidth(),
            headerCt = dragHd.getOwnerHeaderCt(),
            x, y, markerOwner, lhsMarker, rhsMarker, markerHeight;

        me.headerCt.dragging = true;
        me.origWidth = width;

        // setup marker proxies
        if (!me.dynamic) {
            markerOwner = me.markerOwner;

            // https://sencha.jira.com/browse/EXTJSIV-11299
            // In Neptune (and other themes with wide frame borders), resize handles are embedded in borders,
            // *outside* of the outer element's content area, therefore the outer element is set to overflow:visible.
            // During column resize, we should not see the resize markers outside the grid, so set to overflow:hidden.
            if (markerOwner.frame && markerOwner.resizable) {
                me.gridOverflowSetting = markerOwner.el.dom.style.overflow;
                markerOwner.el.dom.style.overflow = 'hidden';
            }
            x            = me.getLeftMarkerX(markerOwner);
            lhsMarker    = markerOwner.getLhsMarker();
            rhsMarker    = markerOwner.getRhsMarker();
            markerHeight = me.ownerGrid.body.getHeight() + headerCt.getHeight();
            y            = headerCt.getOffsetsTo(markerOwner)[1] - markerOwner.el.getBorderWidth('t');

            lhsMarker.setLocalY(y);
            rhsMarker.setLocalY(y);
            lhsMarker.setHeight(markerHeight);
            rhsMarker.setHeight(markerHeight);
            me.setMarkerX(lhsMarker, x);
            me.setMarkerX(rhsMarker, x + width);
        }
    },

    // synchronize the rhsMarker with the mouse movement
    onDrag: function(e){
        var me = this;
            
        if (me.dynamic) {
            me.doResize();
        } else {
            me.setMarkerX(me.getMovingMarker(me.markerOwner), me.calculateDragX(me.markerOwner));
        }
    },
    
    getMovingMarker: function(markerOwner){
        return markerOwner.getRhsMarker();
    },

    onEnd: function(e) {
        this.headerCt.dragging = false;
        if (this.dragHd) {
            if (!this.dynamic) {
                var markerOwner = this.headerCt.up('tablepanel');

                // hide markers
                if (markerOwner.ownerLockable) {
                    markerOwner = markerOwner.ownerLockable;
                }
                // If we had saved the gridOverflowSetting, restore it
                if ('gridOverflowSetting' in this) {
                    markerOwner.el.dom.style.overflow = this.gridOverflowSetting;
                }

                this.setMarkerX(markerOwner.getLhsMarker(), -9999);
                this.setMarkerX(markerOwner.getRhsMarker(), -9999);
            }
            this.doResize();
        }
        // If the mouse is still within the handleWidth, then we must be ready to drag again
        this.onHeaderCtMouseMove(e);
    },

    doResize: function() {
        var me = this,
            dragHd = me.dragHd,
            nextHd,
            offset = me.tracker.getOffset('point');

        // Only resize if we have dragged any distance in the X dimension...
        if (dragHd && offset[0]) {
            // resize the dragHd
            if (dragHd.flex) {
                delete dragHd.flex;
            }

            Ext.suspendLayouts();

            // Set the new column width.
            me.adjustColumnWidth(offset[0]);
 
            // In the case of forceFit, change the following Header width.
            // Constraining so that neither neighbour can be sized to below minWidth is handled in getConstrainRegion
            if (me.headerCt.forceFit) {
                nextHd = dragHd.nextNode('gridcolumn:not([hidden]):not([isGroupHeader])');
                if (nextHd && !me.headerInSameGrid(nextHd)) {
                    nextHd = null;
                }
                if (nextHd) {
                    delete nextHd.flex;
                    nextHd.setWidth(nextHd.getWidth() - offset[0]);
                }
            }

            // Apply the two width changes by laying out the owning HeaderContainer
            Ext.resumeLayouts(true);
        }
    },
    
    // nextNode can traverse out of this grid, possibly to others on the page, so limit it here
    headerInSameGrid: function(header) {
        var grid = this.dragHd.up('tablepanel');
        
        return !!header.up(grid);
    },

    disable: function() {
        this.disabled = true;
        if (this.tracker) {
            this.tracker.disable();
        }
    },

    enable: function() {
        this.disabled = false;
        if (this.tracker) {
            this.tracker.enable();
        }
    },

    calculateDragX: function(markerOwner) {
        return this.tracker.getXY('point')[0] + this.xDelta - markerOwner.getX() - markerOwner.el.getBorderWidth('l');
    },

    getLeftMarkerX: function(markerOwner) {
        return this.dragHd.getX() - markerOwner.getX() - markerOwner.el.getBorderWidth('l') - 1;
    },

    setMarkerX: function(marker, x) {
        marker.setLocalX(x);
    },

    adjustConstrainRegion: function(region, t, r, b, l) {
        return region.adjust(t, r, b, l);
    },

    adjustColumnWidth: function(offsetX) {
        this.dragHd.setWidth(this.origWidth + offsetX);
    }
});