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
      var root = content.down('#feature-root');
      var breadcrumb = content.down('#breadcrumb');

      if (me.currentIndex == 0) {
        // Feature's home page, no breadcrumb required
        breadcrumb.hide();
        root.show();
      } else {
        breadcrumb.removeAll();

        // Make a breadcrumb (including icon and 'home' link)
        breadcrumb.add(
          '',
          {
            xtype: 'image',
            height: 32,
            width: 32,
            cls: 'nx-icon-search-default-x32'
          },
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
          breadcrumb.add(
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

        root.hide();
        breadcrumb.show();
      }
    },

    me.syncSizeToOwner = function () {
      var me = this;
      if (me.ownerCt) {
        me.setSize(me.ownerCt.el.getWidth() * me.items.items.length, me.ownerCt.el.getHeight());
        me.showChild(me.currentIndex);
      }
    },

    me.showChild = function (index) {
      var me = this;

      if (me.items.items[index].el) {
        var left = me.items.items[index].el.getLeft() - me.el.getLeft();
        if (index == me.currentIndex) {
          me.el.first().move('l', left, false);
        } else {
          me.el.first().move('l', left, true);
        }
        me.currentIndex = index;

        me.refreshBreadcrumb();
      }
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