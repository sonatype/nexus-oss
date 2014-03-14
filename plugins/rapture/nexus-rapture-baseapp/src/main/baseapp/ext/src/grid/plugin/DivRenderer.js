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
Ext.define('Ext.grid.plugin.DivRenderer', {
    alias: 'plugin.divrenderer',
    extend: 'Ext.AbstractPlugin',

    tableTpl: [
        '<div id="{view.id}-table" class="', Ext.baseCSSPrefix, '{view.id}-table ', Ext.baseCSSPrefix, 'grid-table" style="{tableStyle}" {ariaTableAttr}>',
            '{%',
                'values.view.renderRows(values.rows, values.viewStartIndex, out);',
            '%}',
        '</div>',
        {
            priority: 0
        }
    ],

    rowTpl: [
        '{%',
            'var dataRowCls = values.recordIndex === -1 ? "" : " ' + Ext.baseCSSPrefix + 'grid-data-row";',
        '%}',
        '<dl {[values.rowId ? ("id=\\"" + values.rowId + "\\"") : ""]} ',
            'data-boundView="{view.id}" ',
            'data-recordId="{record.internalId}" ',
            'data-recordIndex="{recordIndex}" ',
            'class="{[values.itemClasses.join(" ")]} {[values.rowClasses.join(" ")]}{[dataRowCls]}" ',
            'style="position:relative" ',
            '{rowAttr:attributes} {ariaRowAttr}>',
            '<tpl for="columns">' +
                '{%',
                    'parent.view.renderCell(values, parent.record, parent.recordIndex, xindex - 1, out, parent)',
                 '%}',
            '</tpl>',
        '</dl>',
        {
            priority: 0
        }
    ],

    cellTpl: [
        '<dt class="{tdCls}" {tdAttr} data-cellIndex="{cellIndex}" {ariaCellAttr}>',
            '<div {unselectableAttr} class="' + Ext.baseCSSPrefix + 'grid-cell-inner"',
                'style="text-align:{align};<tpl if="style">{style}</tpl>" {ariaCellInnerAttr}>{value}</div>',
        '</dt>', {
            priority: 0
        }
    ],

    selectors: {
        // Outer table
        bodySelector: 'div',

        // Element which contains rows
        nodeContainerSelector: 'div',

        // view item (may be a wrapper)
        itemSelector: 'dl.' + Ext.baseCSSPrefix + 'grid-row',

        // row which contains cells as opposed to wrapping rows
        dataRowSelector: 'dl.' + Ext.baseCSSPrefix + 'grid-data-row',

        // cell
        cellSelector: 'dt.' + Ext.baseCSSPrefix + 'grid-cell',

        innerSelector: 'div.' + Ext.baseCSSPrefix + 'grid-cell-inner',

        getNodeContainerSelector: function() {
            return this.getBodySelector();
        },

        getNodeContainer: function() {
            return this.el.getById(this.id + '-table', true);
        }
    },

    init: function(grid) {
        var view = grid.getView();
        view.tableTpl = Ext.XTemplate.getTpl(this, 'tableTpl');
        view.rowTpl   = Ext.XTemplate.getTpl(this, 'rowTpl');
        view.cellTpl  = Ext.XTemplate.getTpl(this, 'cellTpl');
        Ext.apply(view, this.selectors);
    }
});