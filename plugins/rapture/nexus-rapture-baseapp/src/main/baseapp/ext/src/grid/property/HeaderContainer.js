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
 * A custom HeaderContainer for the {@link Ext.grid.property.Grid}.
 * Generally it should not need to be used directly.
 */
Ext.define('Ext.grid.property.HeaderContainer', {

    extend: 'Ext.grid.header.Container',

    alternateClassName: 'Ext.grid.PropertyColumnModel',

    nameWidth: 115,

    // @private strings used for locale support
    //<locale>
    nameText : 'Name',
    //</locale>
    //<locale>
    valueText : 'Value',
    //</locale>
    //<locale>
    dateFormat : 'm/j/Y',
    //</locale>
    //<locale>
    trueText: 'true',
    //</locale>
    //<locale>
    falseText: 'false',
    //</locale>

    // @private
    nameColumnCls: Ext.baseCSSPrefix + 'grid-property-name',
    nameColumnInnerCls: Ext.baseCSSPrefix + 'grid-cell-inner-property-name',

    /**
     * Creates new HeaderContainer.
     * @param {Ext.grid.property.Grid} grid The grid this store will be bound to
     * @param {Object} source The source data config object
     */
    constructor : function(grid, store) {
        var me = this;

        me.grid = grid;
        me.store = store;
        me.callParent([{
            isRootHeader: true,
            enableColumnResize: Ext.isDefined(grid.enableColumnResize) ? grid.enableColumnResize : me.enableColumnResize,
            enableColumnMove: Ext.isDefined(grid.enableColumnMove) ? grid.enableColumnMove : me.enableColumnMove,
            items: [{
                header: me.nameText,
                width: grid.nameColumnWidth || me.nameWidth,
                sortable: grid.sortableColumns,
                dataIndex: grid.nameField,
                renderer: Ext.Function.bind(me.renderProp, me),
                itemId: grid.nameField,
                menuDisabled: true,
                tdCls: me.nameColumnCls,
                innerCls: me.nameColumnInnerCls
            }, {
                header: me.valueText,
                renderer: Ext.Function.bind(me.renderCell, me),
                getEditor: Ext.Function.bind(me.getCellEditor, me),
                sortable: grid.sortableColumns,
                flex: 1,
                fixed: true,
                dataIndex: grid.valueField,
                itemId: grid.valueField,
                menuDisabled: true
            }]
        }]);

        // PropertyGrid needs to know which column is the editable "value" column.
        me.grid.valueColumn = me.items.items[1];
    },

    getCellEditor: function(record){
        return this.grid.getCellEditor(record, this);
    },

    // @private
    // Render a property name cell
    renderProp : function(v) {
        return this.getPropertyName(v);
    },

    // @private
    // Render a property value cell
    renderCell : function(val, meta, rec) {
        var me = this,
            grid = me.grid,
            renderer = grid.getConfig(rec.get(grid.nameField), 'renderer'),
            result = val;

        if (renderer) {
            return renderer.apply(me, arguments);
        }
        if (Ext.isDate(val)) {
            result = me.renderDate(val);
        } else if (Ext.isBoolean(val)) {
            result = me.renderBool(val);
        }
        return Ext.util.Format.htmlEncode(result);
    },

    // @private
    renderDate : Ext.util.Format.date,

    // @private
    renderBool : function(bVal) {
        return this[bVal ? 'trueText' : 'falseText'];
    },

    // @private
    // Renders custom property names instead of raw names if defined in the Grid
    getPropertyName : function(name) {
        return this.grid.getConfig(name, 'displayName', name);
    }
});
