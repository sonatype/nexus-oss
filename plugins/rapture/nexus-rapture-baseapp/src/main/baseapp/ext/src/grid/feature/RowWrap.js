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
            '<td class="', Ext.baseCSSPrefix, 'grid-rowwrap ', Ext.baseCSSPrefix, 'grid-td" colSpan="{columns.length}" {ariaCellAttr}>',
                '<table class="', Ext.baseCSSPrefix, '{view.id}-table ', Ext.baseCSSPrefix, 'grid-table" border="0" cellspacing="0" cellpadding="0" style="width:100%" {ariaCellInnerTableAttr}>',
                    '{[values.view.renderColumnSizer(out)]}',
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

    init: function(grid) {
        var me = this;
        me.view.addTableTpl(me.tableTpl);
        me.view.addRowTpl(Ext.XTemplate.getTpl(me, 'wrapTpl'));
        me.view.headerCt.on({
            columnhide: me.onColumnHideShow,
            columnshow: me.onColumnHideShow,
            scope: me
        });
    },

    // Keep row wtap colspan in sync with number of *visible* columns.
    onColumnHideShow: function() {
        var view = this.view,
            items = view.el.query(this.rowWrapTd),
            colspan = view.headerCt.getVisibleGridColumns().length,
            len = items.length,
            i;
            
        for (i = 0; i < len; ++i) {
            items[i].colSpan = colspan;
        }
    }
});