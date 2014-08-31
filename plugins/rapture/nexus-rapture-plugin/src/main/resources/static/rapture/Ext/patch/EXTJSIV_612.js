/*global Ext */

/**
 * Patch for rowexpander grid stripes.  Fixed in 4.2.3
 *
 * @see https://support.sencha.com/index.php#ticket-18673
 * @see http://www.sencha.com/forum/showthread.php?128990-FIXED-EXTJSIV-612-row-expander-disables-grid-stripes
 * @see https://fiddle.sencha.com/#fiddle/lv
 */
Ext.define('Ext.patch.EXTJSIV_612', {
  override: 'Ext.view.Table',

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
        // Ensure selection class is added to selected records, and to the record *before* any selected record
        // When looking ahead to see if the next record is selected, ensure we do not look past the end!
        if (me.rowValues.recordIndex < me.store.getTotalCount() - 1 && selModel.isRowSelected(me.rowValues.recordIndex + 1) && !me.isRowStyleFirst(rowIdx + 1)) {
          rowClasses.push(me.beforeSelectedItemCls);
        }
      }

      if (me.stripeRows && rowIdx % 2 !== 0) {
        itemClasses.push(me.altRowCls);
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
  }
});