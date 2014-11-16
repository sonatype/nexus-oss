Ext.define('NX.view.drilldown.DrilldownItem', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-drilldown-item',

  itemName: 'item',
  itemClass: null,

  initComponent: function () {
    var me = this;

    me.setItemName = function(text) {
      this.itemName = text;
    }

    me.setItemClass = function(cls) {
      this.itemClass = cls;
    }

    me.callParent(arguments);
  }
});