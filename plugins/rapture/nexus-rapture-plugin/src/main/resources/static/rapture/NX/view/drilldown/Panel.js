/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global Ext, NX*/

/**
 * Abstract Master/Detail panel.
 *
 * @since 3.0
 */
Ext.define('NX.view.drilldown.Panel', {
  extend: 'Ext.container.Container',
  alias: 'widget.nx-drilldown-panel',
  itemId: 'nx-drilldown-panel',

  layout: {
    type: 'hbox',
    align: 'stretch'
  },

  defaults: {
    flex: 1
  },

  requires: [
    'NX.Icons'
  ],

  /**
   * Set the name of the referenced drilldown item (appears in the breadcrumb)
   */
  setItemName: function (index, text) {
    var me = this;
    me.query('nx-drilldown-item')[index].setItemName(text);
  },

  /**
   * Set the icon class of the references drilldown item (appears in the breadcrumb)
   */
  setItemClass: function (index, cls) {
    var me = this;
    me.query('nx-drilldown-item')[index].setItemClass(cls);
  },

  /**
   * Create an event handler so the panel will resize correctly when the window does
   *
   * @override
   */
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
  },

  /**
   * Given N drilldown items, this panel should have a width of N times the current screen width
   */
  syncSizeToOwner: function () {
    var me = this;
    if (me.ownerCt) {
      me.setSize(me.ownerCt.el.getWidth() * me.items.items.length, me.ownerCt.el.getHeight());
      me.showChild(me.currentIndex, false);
    }
  },

  /**
   * Shift this panel to display the referenced drilldown item
   *
   * @param index The index of the drilldown item to display
   * @param animate Set to “true” if the view should slide into place, “false” if it should just appear
   */
  showChild: function (index, animate) {
    var me = this;

    if (me.items.items[index].el) {

      // Hack to prevent resize events until the animation is complete
      if (animate) {
        me.ownerCt.suspendEvents(false);
        setTimeout(function () { me.ownerCt.resumeEvents(); }, 300);
      }

      // Show the new panel
      var left = me.items.items[index].el.getLeft() - me.el.getLeft();
      me.el.first().move('l', left, animate);
      me.currentIndex = index;

      me.refreshBreadcrumb();
    }
  },

  /**
   * Update the breadcrumb based on the itemName and itemClass of drilldown items
   */
  refreshBreadcrumb: function() {
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
          cls: content.currentIconCls
        },
        {
          xtype: 'button',
          scale: 'large',
          ui: 'drilldown',
          text: content.currentTitle,
          handler: function() {
            me.showChild(0, true);

            // Set the bookmark
            var bookmark = me.items.items[0].itemBookmark;
            if (bookmark) {
              NX.Bookmarks.bookmark(bookmark.obj, bookmark.scope);
            }
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
          {
            xtype: 'image',
            height: 16,
            width: 16,
            cls: 'breadcrumb-icon ' + me.items.items[i].itemClass
          },

          // Create a closure within a closure to decouple 'i' from the current context
          (function(j) {
            return {
              xtype: 'button',
              scale: 'medium',
              ui: 'drilldown',
              disabled: (i === me.currentIndex ? true : false), // Disabled if it’s the last item in the breadcrumb
              text: me.items.items[j].itemName,
              handler: function() {
                me.showChild(j, true);

                // Set the bookmark
                var bookmark = me.items.items[j].itemBookmark;
                if (bookmark) {
                  NX.Bookmarks.bookmark(bookmark.obj, bookmark.scope);
                }
              }
            }
          })(i)
        );
      }

      root.hide();
      breadcrumb.show();
    }
  }
});
