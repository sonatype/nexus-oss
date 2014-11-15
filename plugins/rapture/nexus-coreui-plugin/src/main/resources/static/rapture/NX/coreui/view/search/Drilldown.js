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

  rootName: 'default',

  initComponent: function () {
    var me = this;

    me.refreshBreadcrumb = function() {
      var me = this;
      var content = me.up('#feature-content');
      var toolbar = content.down('#breadcrumb');

      toolbar.removeAll();

      if (me.currentIndex == 0) {
        // Feature's home page, no breadcrumb required
        toolbar.add(
          '',
          {
            xtype: 'image',
            itemId: 'icon',
            height: 32,
            width: 32
          },
          {
            xtype: 'tbtext',
            cls: 'feature-name',
            itemId: 'title'
          },
          {
            xtype: 'tbtext',
            cls: 'feature-description',
            itemId: 'description'
          }
        );

        content.setIconCls(NX.Icons.cls('search-default', 'x32'));
        content.setTitle('Search');
        content.setDescription('Search for components');
      } else {
        // Make a breadcrumb
        toolbar.add(
          '',
          {
            xtype: 'image',
            itemId: 'icon',
            height: 32,
            width: 32
          }
        );

        // Create the 'home' link
        toolbar.add(
          {
            xtype: 'nx-drilldown-link',
            scale: 'large',
            text: me.items.items[0].itemName,
            handler: function() {
              me.showChild(0);
            }
          }
        );

        // Create the rest of the links
        for (var i = 1; i <= me.currentIndex && i < me.items.items.length; ++i) {
          toolbar.add(
            // Separator
            {
              xtype: 'tbtext',
              cls: 'breadcrumb-separator',
              text: '/'
            },

            // Create a closure within a closure to decouple 'i' from the current context
            (function(j) {
              return {
                xtype: 'nx-drilldown-link',
                disabled: (i == me.currentIndex ? true : false), // Disabled if it’s the last item in the breadcrumb
                text: me.items.items[j].itemName,
                handler: function() {
                  me.showChild(j);
                }
              }
            })(i)
          );
        }
        content.setIconCls(NX.Icons.cls('search-default', 'x32'));
      }
    },

    me.syncSizeToOwner = function () {
      var me = this;
      if (me.ownerCt) {
        me.setSize(me.ownerCt.el.getWidth() * me.items.items.length, me.ownerCt.el.getHeight());
      }
    },

    me.showChild = function (index) {
      var me = this;
      var left = me.items.items[index].el.getLeft() - me.el.getLeft();
      me.el.first().move('l', left, true);
      me.currentIndex = index;

      me.refreshBreadcrumb();
    },

    me.setItemName = function (index, text) {
      var me = this;
      me.items.items[index].setItemName(text);
    },

    me.callParent(arguments);
  },

  onRender: function () {
    var me = this;

    me.currentIndex = 0;

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