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
 * A small abstract class that contains the shared behaviour for any summary
 * calculations to be used in the grid.
 */
Ext.define('Ext.grid.feature.AbstractSummary', {

    extend: 'Ext.grid.feature.Feature',

    alias: 'feature.abstractsummary',

    summaryRowCls: Ext.baseCSSPrefix + 'grid-row-summary',
    summaryTableCls: Ext.plainTableCls + ' ' + Ext.baseCSSPrefix + 'grid-table',
    summaryRowSelector: '.' + Ext.baseCSSPrefix + 'grid-row-summary',

    // High priority rowTpl interceptor which sees summary rows early, and renders them correctly and then aborts the row rendering chain.
    // This will only see action when summary rows are being updated and Table.onUpdate->Table.bufferRender renders the individual updated sumary row.
    summaryRowTpl: {
        before: function(values, out) {
            // If a summary record comes through the rendering pipeline, render it simply, and return false from the
            // before method which aborts the tpl chain
            if (values.record.isSummary && this.summaryFeature.showSummaryRow) {
                this.summaryFeature.outputSummaryRecord(values.record, values, out);
                return false;
            }
        },
        priority: 1000
    },

   /**
    * @cfg {Boolean}
    * True to show the summary row.
    */
    showSummaryRow: true,

    // Listen for store updates. Eg, from an Editor.
    init: function() {
        var me = this;
        me.view.summaryFeature = me;
        me.rowTpl = me.view.self.prototype.rowTpl;

        // Add a high priority interceptor which renders summary records simply
        // This will only see action ona bufferedRender situation where summary records are updated.
        me.view.addRowTpl(me.summaryRowTpl).summaryFeature = me;

        // Define on the instance to store info needed by summary renderers.
        me.summaryData = {};
    },

    /**
     * Toggle whether or not to show the summary row.
     * @param {Boolean} visible True to show the summary row
     */
    toggleSummaryRow: function(visible) {
        this.showSummaryRow = !!visible;
    },

    createRenderer: function (column, record) {
        var me = this,
            ownerGroup = record.ownerGroup,
            summaryData = ownerGroup ? me.summaryData[ownerGroup] : me.summaryData,
            // Use the column.id for columns without a dataIndex. The populateRecord method does the same.
            dataIndex = column.dataIndex || column.id;

        return function () {
             return column.summaryRenderer ?
                column.summaryRenderer(record.get(dataIndex), summaryData, dataIndex) :
                // For no summaryRenderer, return the field value in the Feature record.
                record.get(dataIndex);
        };
    },

    outputSummaryRecord: function(summaryRecord, contextValues, out) {
        var view = contextValues.view,
            savedRowValues = view.rowValues,
            columns = contextValues.columns || view.headerCt.getVisibleGridColumns(),
            colCount = columns.length, i, column,
            // Set up a row rendering values object so that we can call the rowTpl directly to inject
            // the markup of a grid row into the output stream.
            values = {
                view: view,
                record: summaryRecord,
                rowStyle: '',
                rowClasses: [ this.summaryRowCls ],
                itemClasses: [],
                recordIndex: -1,
                rowId: view.getRowId(summaryRecord),
                columns: columns
            };

        // Because we are using the regular row rendering pathway, temporarily swap out the renderer for the summaryRenderer
        for (i = 0; i < colCount; i++) {
            column = columns[i];
            column.savedRenderer = column.renderer;

            if (column.summaryType || column.summaryRenderer) {
                column.renderer = this.createRenderer(column, summaryRecord);
            } else {
                column.renderer = Ext.emptyFn;
            }
        }

        // Use the base template to render a summary row
        view.rowValues = values;
        view.self.prototype.rowTpl.applyOut(values, out);
        view.rowValues = savedRowValues;

        // Restore regular column renderers
        for (i = 0; i < colCount; i++) {
            column = columns[i];
            column.renderer = column.savedRenderer;
            column.savedRenderer = null;
        }
    },

    /**
     * Get the summary data for a field.
     * @private
     * @param {Ext.data.Store} store The store to get the data from
     * @param {String/Function} type The type of aggregation. If a function is specified it will
     * be passed to the stores aggregate function.
     * @param {String} field The field to aggregate on
     * @param {Boolean} group True to aggregate in grouped mode 
     * @return {Number/String/Object} See the return type for the store functions.
     * if the group parameter is `true` An object is returned with a property named for each group who's
     * value is the summary value.
     */
    getSummary: function (store, type, field, group) {
        // Note `group` will either be an instance of Ext.data.Group or a list of records.
        var records = group.records;

        if (type) {
            if (Ext.isFunction(type)) {
                return store.getAggregate(type, null, records, [field]);
            }

            switch (type) {
                case 'count':
                    return records.length;
                case 'min':
                    return store.getMin(records, field);
                case 'max':
                    return store.getMax(records, field);
                case 'sum':
                    return store.getSum(records, field);
                case 'average':
                    return store.getAverage(records, field);
                default:
                    return '';

            }
        }
    },

    /**
     * Used by the Grouping Feature when {@link #showSummaryRow} is `true`.
     * 
     * Generates group summary data for the whole store.
     * @private
     * @return {Object} An object hash keyed by group name containing summary records.
     */
    generateSummaryData: function(){
        var me = this,
            store = me.view.store,
            groups = store.groups.items,
            reader = store.proxy.reader,
            len = groups.length,
            groupField = me.getGroupField(),
            data = {},
            lockingPartner = me.lockingPartner,
            i, group, record,
            root, summaryRows, hasRemote,
            convertedSummaryRow, remoteData;

        /**
         * @cfg {String} [remoteRoot=undefined]
         * The name of the property which contains the Array of summary objects.
         * It allows to use server-side calculated summaries.
         */
        if (me.remoteRoot && reader.rawData) {
            hasRemote = true;
            remoteData = {};
            // reset reader root and rebuild extractors to extract summaries data
            root = reader.root;
            reader.root = me.remoteRoot;
            reader.buildExtractors(true);
            summaryRows = reader.getRoot(reader.rawData)||[];
            len = summaryRows.length;

            // Ensure the Reader has a data conversion function to convert a raw data row into a Record data hash
            if (!reader.convertRecordData) {
                reader.buildExtractors();
            }

            for (i = 0; i < len; ++i) {
                convertedSummaryRow = {};

                // Convert a raw data row into a Record's hash object using the Reader
                reader.convertRecordData(convertedSummaryRow, summaryRows[i]);
                remoteData[convertedSummaryRow[groupField]] = convertedSummaryRow;
            }

            // restore initial reader configuration
            reader.root = root;
            reader.buildExtractors(true);
        }

        for (i = 0; i < len; ++i) {
            group = groups[i];
            // Something has changed or it doesn't exist, populate it
            if (hasRemote || group.isDirty() || !group.hasAggregate()) {
                record = me.populateRecord(group, remoteData);

                // Clear the dirty state of the group if this is the only Summary, or this is the right hand (normal grid's) summary
                if (!lockingPartner || (me.view.ownerCt === me.view.ownerCt.ownerLockable.normalGrid)) {
                    group.commit();
                }
            } else {
                record = group.getAggregateRecord();
            }

            data[group.key] = record;
        }

        return data;
    },

    setSummaryData: function (record, colId, summaryValue, groupName) {
        if (groupName) {
            if (!this.summaryData[groupName]) {
                this.summaryData[groupName] = {};
            }
            this.summaryData[groupName][colId] = summaryValue;
        } else {
            this.summaryData[colId] = summaryValue;
        }
    },

    populateRecord: function (group, remoteData) {
        var me = this,
            view = me.grid.ownerLockable ? me.grid.ownerLockable.view : me.view,
            store = me.view.store,
            record = group.getAggregateRecord(),
            // Use the full column set, regardless of locking
            columns = view.headerCt.getGridColumns(),
            len = columns.length,
            groupName = group.key,
            groupData, field, i, column, fieldName, summaryValue;

        record.beginEdit();

        if (remoteData) {
            // Remote summary grouping provides the grouping totals so there's no need to
            // iterate throught the columns to map the column's dataIndex to the field name.
            // Instead, enumerate the grouping record and set the field in the aggregate
            // record for each one.
            groupData = remoteData[groupName];
            for (field in groupData) {
                if (groupData.hasOwnProperty(field)) {
                    if (field !== record.idProperty) {
                        record.set(field, groupData[field]);
                    }
                }
            }
        }

        // Here we iterate through the columns with two objectives:
        //    1. For local grouping, get the summary for each column and update the record.
        //    2. For both local and remote grouping, set the summary data object
        //       which is passed to the summaryRenderer (if defined).
        for (i = 0; i < len; ++i) {
            column = columns[i];
            // Use the column id if there's no mapping, could be a calculated field.
            fieldName = column.dataIndex || column.id;

            // We need to capture the summary value because it could get overwritten when
            // setting on the model if there is a convert() method on the model.
            if (!remoteData) {
                summaryValue = me.getSummary(store, column.summaryType, fieldName, group);
                record.set(fieldName, summaryValue);
            } else {
                // For remote groupings, just get the value from the model.
                summaryValue = record.get(fieldName);
            }

            // Capture the columnId:value for the summaryRenderer in the summaryData object.
            me.setSummaryData(record, column.id, summaryValue, groupName);
        }

        // Poke on the owner group for easy lookup in this.createRenderer().
        record.ownerGroup = groupName;

        record.endEdit(true);
        record.commit();

        return record;
    }
});
