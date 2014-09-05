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
 * @private
 */
Ext.define('Ext.grid.feature.RowWrap', {
    extend: 'Ext.grid.feature.Feature',
    alias: 'feature.rowwrap',
    
    rowWrapTd: 'td.' + Ext.baseCSSPrefix + 'grid-rowwrap',
    
    // turn off feature events.
    hasFeatureEvent: false,
    
    tableTpl: {
        before: function(values, out) {
            if (values.view.bufferedRenderer) {
                values.view.bufferedRenderer.variableRowHeight = true;
            }
        },
        priority: 200
    },

    wrapTpl: [
        '<tr data-boundView="{view.id}" data-recordId="{record.internalId}" data-recordIndex="{recordIndex}" class="{[values.itemClasses.join(" ")]} ', Ext.baseCSSPrefix, 'grid-wrap-row" {ariaRowAttr}>',
            '<td class="', Ext.baseCSSPrefix, 'grid-rowwrap ', Ext.baseCSSPrefix, 'grid-td" colspan="{columns.length}" {ariaCellAttr}>',
                '<table class="', Ext.baseCSSPrefix, '{view.id}-table ', Ext.baseCSSPrefix, 'grid-table" border="0" cellspacing="0" cellpadding="0" style="width:100%" {ariaCellInnerTableAttr}>',
                    '{[values.view.renderRowWrapColumnSizer(out)]}',
                    '{%',
                        'values.itemClasses.length = 0;',
                        'this.nextTpl.applyOut(values, out, parent)',
                    '%}',
                '</table>',
            '</td>',
        '</tr>', {
            priority: 200
        }
    ],

    getTargetSelector: function () {
        return this.itemSelector;
    },

    init: function(grid) {
        var me = this,
            view = me.view;

        view.addTableTpl(me.tableTpl);
        view.addRowTpl(Ext.XTemplate.getTpl(me, 'wrapTpl'));
        view.renderRowWrapColumnSizer = me.view.renderColumnSizer;
        view.renderColumnSizer = Ext.emptyFn;

        // Let the view know that it should use the itemSelector ancestor to retrieve the node
        // rather than the datarow selector.
        view.isRowWrapped = true;

        // When looking up the target selector, wrapped rows should always use the itemSelector.
        view.getTargetSelector = me.getTargetSelector;
    }});
