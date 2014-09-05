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
 * The rowbody feature enhances the grid's markup to have an additional
 * tr -> td -> div which spans the entire width of the original row.
 *
 * This is useful to to associate additional information with a particular
 * record in a grid.
 *
 * Rowbodies are initially hidden unless you override setupRowData.
 *
 * Will expose additional events on the gridview with the prefix of 'rowbody'.
 * For example: 'rowbodyclick', 'rowbodydblclick', 'rowbodycontextmenu'.
 *
 * # Example
 *
 *     @example
 *     Ext.define('Animal', {
 *         extend: 'Ext.data.Model',
 *         fields: ['name', 'latin', 'desc']
 *     });
 * 
 *     Ext.create('Ext.grid.Panel', {
 *         width: 400,
 *         height: 300,
 *         renderTo: Ext.getBody(),
 *         store: {
 *             model: 'Animal',
 *             data: [
 *                 {name: 'Tiger', latin: 'Panthera tigris',
 *                  desc: 'The largest cat species, weighing up to 306 kg (670 lb).'},
 *                 {name: 'Roman snail', latin: 'Helix pomatia',
 *                  desc: 'A species of large, edible, air-breathing land snail.'},
 *                 {name: 'Yellow-winged darter', latin: 'Sympetrum flaveolum',
 *                  desc: 'A dragonfly found in Europe and mid and Northern China.'},
 *                 {name: 'Superb Fairy-wren', latin: 'Malurus cyaneus',
 *                  desc: 'Common and familiar across south-eastern Australia.'}
 *             ]
 *         },
 *         columns: [{
 *             dataIndex: 'name',
 *             text: 'Common name',
 *             width: 125
 *         }, {
 *             dataIndex: 'latin',
 *             text: 'Scientific name',
 *             flex: 1
 *         }],
 *         features: [{
 *             ftype: 'rowbody',
 *             setupRowData: function(record, rowIndex, rowValues) {
 *                 var headerCt = this.view.headerCt,
 *                     colspan = headerCt.getColumnCount();
 *
 *                 // Usually you would style the my-body-class in CSS file
 *                 Ext.apply(rowValues, {
 *                     rowBody: '<div style="padding: 1em">'+record.get("desc")+'</div>',
 *                     rowBodyCls: "my-body-class",
 *                     rowBodyColspan: colspan
 *                 });
 *             }
 *         }]
 *     });
 *
 *  # Cell Editing and Cell Selection Model
 *
 * Note that if {@link Ext.grid.plugin.CellEditing cell editing} or the {@link Ext.selection.CellModel cell selection model} are going
 * to be used, then the {@link Ext.grid.feature.RowWrap RowWrap} feature, or {@link Ext.grid.plugin.RowExpander RowExpander} plugin MUST
 * be used for intra-cell navigation to be correct.
 *
 */
Ext.define('Ext.grid.feature.RowBody', {
    extend: 'Ext.grid.feature.RowWrap',
    alias: 'feature.rowbody',

    rowBodyCls: Ext.baseCSSPrefix + 'grid-row-body',
    rowBodyHiddenCls: Ext.baseCSSPrefix + 'grid-row-body-hidden',
    rowBodyTdSelector: 'td.' + Ext.baseCSSPrefix + 'grid-cell-rowbody',
    eventPrefix: 'rowbody',
    eventSelector: 'tr.' + Ext.baseCSSPrefix + 'grid-rowbody-tr',

    // Turn on feature events so the 'rowbodyxxx' events will be fired in Ext.view.Table:processSpecialEvent().
    hasFeatureEvent: true,

    colSpanDecrement: 0,

    tableTpl: {
        before: function(values, out) {
            // Since RowBody now extends RowWrap, we must only proceed if we're in the correct tpl.
            if (!this.rowBody) {
                return;
            }

            var view = values.view,
                rowValues = view.rowValues;

            this.rowBody.setup(values.rows, rowValues);
        },
        after: function(values, out) {
            // Since RowBody now extends RowWrap, we must only proceed if we're in the correct tpl.
            if (!this.rowBody) {
                return;
            }

            var view = values.view,
                rowValues = view.rowValues;

            this.rowBody.cleanup(values.rows, rowValues);
        },
        priority: 100
    },

    extraRowTpl: [
        '{%',
            'values.view.rowBodyFeature.setupRowData(values.record, values.recordIndex, values);',
            'this.nextTpl.applyOut(values, out, parent);',
        '%}',
        '<tr class="', Ext.baseCSSPrefix, 'grid-rowbody-tr {rowBodyCls}" {ariaRowAttr}>',
            '<td class="', Ext.baseCSSPrefix, 'grid-cell-rowbody', '" colspan="{rowBodyColspan}" {ariaCellAttr}>',
                '<div class="', Ext.baseCSSPrefix, 'grid-rowbody', ' {rowBodyDivCls}" {ariaCellInnerAttr}>{rowBody}</div>',
            '</td>',
        '</tr>', {
            priority: 100,

            syncRowHeights: function(firstRow, secondRow) {
                var owner = this.owner,
                    firstRowBody = Ext.fly(firstRow).down(owner.eventSelector, true),
                    secondRowBody,
                    firstHeight, secondHeight;

                // Sync the heights of row body elements in each row if they need it.
                if (firstRowBody && (secondRowBody = Ext.fly(secondRow).down(owner.eventSelector, true))) {
                    if ((firstHeight = firstRowBody.offsetHeight) > (secondHeight = secondRowBody.offsetHeight)) {
                        Ext.fly(secondRowBody).setHeight(firstHeight);
                    }
                    else if (secondHeight > firstHeight) {
                        Ext.fly(firstRowBody).setHeight(secondHeight);
                    }
                }
            },

            syncContent: function(destRow, sourceRow) {
                var owner = this.owner,
                    destRowBody = Ext.fly(destRow).down(owner.eventSelector, true),
                    sourceRowBody;

                // Sync the heights of row body elements in each row if they need it.
                if (destRowBody && (sourceRowBody = Ext.fly(sourceRow).down(owner.eventSelector, true))) {
                    Ext.fly(destRowBody).syncContent(sourceRowBody);
                }
            }
        }
    ],

    init: function(grid) {
        var me = this,
            view = me.view = grid.getView();

        view.rowBodyFeature = me;

        // Need to remove the body row on removing a record.
        me.mon(grid.getStore(), 'remove', me.onStoreRemove, me);

        view.headerCt.on({
            columnschanged: me.onColumnsChanged,
            scope: me
        });

        view.addTableTpl(me.tableTpl).rowBody = me;
        view.addRowTpl(Ext.XTemplate.getTpl(this, 'extraRowTpl'));

        // Don't buffer the mouse event callbacks, doing so can cause a focus class to be applied after mouseout.
        view.mouseOverOutBuffer = 0;

        me.callParent(arguments);
    },
    
    onStoreRemove: function(store, model, index){
        var view = this.view,
            node;
            
        if (view.rendered) {
            node = view.getNode(index);
            if (node) {
                node = Ext.fly(node).next(this.eventSelector);
                if (node) {
                    node.remove();
                }
            }
        }
    },

    getSelectedRow: function(view, rowIndex) {
        var selectedRow = view.getNode(rowIndex, false);
        if (selectedRow) {
            return Ext.fly(selectedRow).down(this.eventSelector);
        }
        return null;
    },

    // When columns added/removed, keep row body colspan in sync with number of columns.
    onColumnsChanged: function(headerCt) {
        var items = this.view.el.query(this.rowBodyTdSelector),
            colspan = headerCt.getVisibleGridColumns().length,
            len = items.length,
            i;

        for (i = 0; i < len; ++i) {
            items[i].colSpan = colspan;
        }
    },
    
    /**
     * @method getAdditionalData
     * Provides additional data to the prepareData call within the grid view.
     * The rowbody feature adds 3 additional variables into the grid view's template.
     * These are rowBodyCls, rowBodyColspan, and rowBody.
     * @param {Object} data The data for this particular record.
     * @param {Number} idx The row index for this record.
     * @param {Ext.data.Model} record The record instance
     * @param {Object} orig The original result from the prepareData call to massage.
     */
    setupRowData: function(record, rowIndex, rowValues) {
        if (this.getAdditionalData) {
            Ext.apply(rowValues, this.getAdditionalData(record.data, rowIndex, record, rowValues));
        }
    },

    setup: function(rows, rowValues) {
        rowValues.rowBodyCls = this.rowBodyCls;
        rowValues.rowBodyColspan = rowValues.view.getGridColumns().length - this.colSpanDecrement;
    },

    cleanup: function(rows, rowValues) {
        rowValues.rowBodyCls = rowValues.rowBodyColspan = rowValues.rowBody = null;    
    }
});
