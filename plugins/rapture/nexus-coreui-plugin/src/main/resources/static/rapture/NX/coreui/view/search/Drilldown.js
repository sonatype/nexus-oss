Ext.define('NX.coreui.view.search.Drilldown', {
  extend: 'Ext.container.Container',
  alias: 'widget.nx-drilldown',
  itemId: 'nx-drilldown',

  layout: {
    type: 'hbox',
    align: 'stretch'
  },

  defaults: {
    flex: 1
  },

  initComponent: function () {
    var me = this;

    //me.addEvents('carouselchange');

    me.callParent(arguments);
  },

  syncSizeToOwner: function () {
    var me = this;
    if (me.ownerCt) {
      me.setSize(me.ownerCt.el.getWidth() * me.items.items.length, me.ownerCt.el.getHeight());
    }
  },

  showChild: function (index) {
    var me = this
      , left = me.items.items[index].el.getLeft() - me.el.getLeft();
    me.el.first().move('l', left, true);
    me.currentItem = me.items.items[index];
    //me.fireEvent('carouselchange', me, item);
  },

  onRender: function () {
    var me = this;

    me.currentItem = me.items.items[0];

    if (me.ownerCt) {
      me.relayEvents(me.ownerCt, ['resize'], 'owner');
      me.on({
        ownerresize: me.syncSizeToOwner
      });
      me.syncSizeToOwner();
    }

    me.callParent(arguments);
  }
});