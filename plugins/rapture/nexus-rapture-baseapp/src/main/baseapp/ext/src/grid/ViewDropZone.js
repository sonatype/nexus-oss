/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2014 Sencha Inc

Contact:  http://www.sencha.com/contact

Pre-release code in the Ext repository is intended for development purposes only and will
not always be stable. 

Use of pre-release code is permitted with your application at your own risk under standard
Ext license terms. Public redistribution is prohibited.

For early licensing, please contact us at licensing@sencha.com

Build date: 2014-09-02 11:12:40 (ef1fa70924f51a26dacbe29644ca3f31501a5fce)
*/
/**
 * @private
 */
Ext.define('Ext.grid.ViewDropZone', {
    extend: 'Ext.view.DropZone',

    indicatorHtml: '<div class="' + Ext.baseCSSPrefix + 'grid-drop-indicator-left" role="presentation"></div><div class="' + Ext.baseCSSPrefix + 'grid-drop-indicator-right" role="presentation"></div>',
    indicatorCls: Ext.baseCSSPrefix + 'grid-drop-indicator',

    handleNodeDrop : function(data, record, position) {
        var view = this.view,
            store = view.getStore(),
            index, records, i, len;

        // If the copy flag is set, create a copy of the models
        if (data.copy) {
            records = data.records;
            data.records = [];
            for (i = 0, len = records.length; i < len; i++) {
                data.records.push(records[i].copy());
            }
        } else {
            /*
             * Remove from the source store. We do this regardless of whether the store
             * is the same bacsue the store currently doesn't handle moving records
             * within the store. In the future it should be possible to do this.
             * Here was pass the isMove parameter if we're moving to the same view.
             */
            data.view.store.remove(data.records, data.view === view);
        }

        if (record && position) {
            index = store.indexOf(record);

            // 'after', or undefined (meaning a drop at index -1 on an empty View)...
            if (position !== 'before') {
                index++;
            }
            store.insert(index, data.records);
        }
        // No position specified - append.
        else {
            store.add(data.records);
        }

        view.getSelectionModel().select(data.records);
    }
});