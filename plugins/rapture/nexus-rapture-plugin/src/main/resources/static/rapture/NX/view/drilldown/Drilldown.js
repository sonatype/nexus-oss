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
/*global Ext*/

/**
 * A series of master/detail panels
 *
 * @since 3.0
 */

Ext.define('NX.view.drilldown.Drilldown', {
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

  /**
   * @override
   */
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
            cls: me.items.items[0].itemClass
          },
          {
            xtype: 'nx-drilldown-link',
            scale: 'large',
            text: me.items.items[0].itemName,
            handler: function() {
              me.showChild(0, true);
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
                xtype: 'nx-drilldown-link',
                disabled: (i == me.currentIndex ? true : false), // Disabled if it’s the last item in the breadcrumb
                text: me.items.items[j].itemName,
                handler: function() {
                  me.showChild(j, true);
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
        me.showChild(me.currentIndex, false);
      }
    },

    me.showChild = function (index, animate) {
      var me = this;

      if (me.items.items[index].el) {

        // Hack to prevent resize events until the animation is complete
        if (animate) {
          me.ownerCt.suspendEvents(false);
          setTimeout(function () { me.ownerCt.resumeEvents(); }, 300);
        }

        var left = me.items.items[index].el.getLeft() - me.el.getLeft();
        me.el.first().move('l', left, animate);
        me.currentIndex = index;

        me.refreshBreadcrumb();
      }
    },

    me.setItemName = function (index, text) {
      var me = this;
      me.items.items[index].setItemName(text);
    },

    me.setItemClass = function (index, cls) {
      var me = this;
      me.items.items[index].setItemClass(cls);
    },

    me.callParent(arguments);
  },

  /**
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
  }
});