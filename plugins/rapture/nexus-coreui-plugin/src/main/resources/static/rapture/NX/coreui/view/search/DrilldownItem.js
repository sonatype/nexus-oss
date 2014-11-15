Ext.define('NX.coreui.view.search.DrilldownItem', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-drilldown-item',
  itemName: 'item',

  initComponent: function () {
    var me = this;

    me.setItemName = function(text) {
      this.itemName = text;
    }

    me.callParent(arguments);
  }
});