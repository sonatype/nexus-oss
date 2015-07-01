/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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
 * The foundation class for new drilldowns. Extend this.
 *
 * @since 3.0
 */
Ext.define('NX.view.drilldown.Drilldown', {
  extend: 'Ext.container.Container',
  alias: 'widget.nx-drilldown',
  itemId: 'nx-drilldown',

  requires: [
    'NX.Icons'
  ],

  // List of masters to use (xtype objects)
  masters: null,

  // List of actions to use in the detail view
  actions: null,

  // Constants which represent card indexes
  BROWSE_INDEX: 0,
  CREATE_INDEX: 1,
  BLANK_INDEX: 2,

  /**
   * @override
   */
  initComponent: function () {
    var me = this,
      items = [];

    // Normalize the list of masters
    if (!me.masters) {
      me.masters = [];
    } else if (!Ext.isArray(me.masters)) {
      me.masters = [me.masters];
    }

    // Add the detail panel to the masters array
    if (me.detail) {
      // Use a custom detail panel
      me.masters.push(me.detail);
    }
    else {
      // Use the default tab panel
      me.masters.push(
        {
          xtype: 'nx-drilldown-details',
          ui: 'nx-drilldown-tabs',
          header: false,
          plain: true,

          layout: {
            type: 'vbox',
            align: 'stretch',
            pack: 'start'
          },

          tabs: Ext.clone(me.tabs),
          actions: Ext.isArray(me.actions) ? Ext.Array.clone(me.actions) : me.actions
        }
      );
    }

    // Stack all panels onto the items array
    for (var i = 0; i < me.masters.length; ++i) {
      items.push(me.createDrilldownItem(i, me.masters[i], undefined));
    }

    // Initialize this component’s items
    me.items = {
      xtype: 'container',

      defaults: {
        flex: 1
      },

      layout: {
        type: 'hbox',
        align: 'stretch'
      },

      items: items
    };

    me.callParent(arguments);
  },

  /**
   * @private
   * Create a new drilldown item
   */
  createDrilldownItem: function(index, browsePanel, createPanel) {
    return {
      xtype: 'nx-drilldown-item',
      itemClass: NX.Icons.cls(this.iconName) + (index === 0 ? '-x32' : '-x16'),
      items: [
        {
          xtype: 'container',
          layout: 'fit',
          itemId: 'browse' + index,
          items: browsePanel
        },
        {
          xtype: 'container',
          layout: 'fit',
          itemId: 'create' + index,
          items: createPanel
        },
        {
          type: 'container',
          layout: 'fit',
          itemId: 'nothin' + index
        }
      ]
    }
  },

  /**
   * @public
   * Set the name of the referenced drilldown item
   */
  setItemName: function (index, text) {
    var items = this.padItems(index);

    items[index].setItemName(text);
  },

  /**
   * @public
   * Set the icon class of the referenced drilldown item
   */
  setItemClass: function (index, cls) {
    var items = this.padItems(index);

    items[index].setItemClass(cls);
  },

  /**
   * @public
   * Set the bookmark of the breadcrumb segment associated with the referenced drilldown item
   */
  setItemBookmark: function (index, bookmark, scope) {
    var items = this.padItems(index);

    items[index].setItemBookmark(bookmark, scope);
  },

  showInfo: function (message) {
    this.down('nx-drilldown-details').showInfo(message);
  },

  clearInfo: function () {
    this.down('nx-drilldown-details').clearInfo();
  },

  showWarning: function (message) {
    this.down('nx-drilldown-details').showWarning(message);
  },

  clearWarning: function () {
    this.down('nx-drilldown-details').clearWarning();
  },

  /**
   * Add a tab to the default detail panel
   *
   * Note: this will have no effect if a custom detail panel has been specified
   */
  addTab: function (tab) {
    var me = this;
    if (!me.detail) {
      me.down('nx-drilldown-details').addTab(tab);
    }
  },

  /**
   * Remove a panel from the default detail panel
   *
   * Note: this will have no effect if a custom detail panel has been specified
   */
  removeTab: function (tab) {
    var me = this;
    if (!me.detail) {
      me.down('nx-drilldown-details').removeTab(tab);
    }
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
        ownerresize: me.syncSizeToOwner,
        afterrender: me.syncSizeToOwner
      });
    }

    me.callParent(arguments);
  },

  /**
   * @private
   * Given N drilldown items, this panel should have a width of N times the current screen width
   */
  syncSizeToOwner: function () {
    var me = this,
      owner = me.ownerCt.body.el,
      container = me.down('container');

    container.setSize(owner.getWidth() * container.items.length, owner.getHeight());
    me.slidePanels(me.currentIndex, false);
  },

  /**
   * @private
   * Hide all except the specified panel. Focus on a default form field, if available.
   *
   * This is needed to restrict focus to the visible panel only.
   */
  hideAllExceptAndFocus: function (index) {
    var me = this,
      items = me.query('nx-drilldown-item'),
      form;

    // Hide everything that’s not the specified panel, reset forms, etc…
    for (var i = 0; i < items.length; ++i) {
      if (i != index) {
        items[i].getLayout().setActiveItem(me.BLANK_INDEX);
      }

      // Reset forms and filters on successive drilldown items
      if (i > index) {
        Ext.each(items[i].query('nx-settingsform'), function(panel) {
          if (panel.getForm().isDirty()) {
            panel.getForm().reset();
          }
        });
      }
    }

    // Set focus on the default field (if available) or the panel itself
    form = items[index].down('nx-addpanel[defaultFocus]');
    if (form) {
      form.down('[name=' + form.defaultFocus + ']').focus();
    } else {
      me.focus();
    }
  },

  /**
   * @private
   * Slide the drilldown to reveal the specified panel
   */
  slidePanels: function (index, animate) {
    var me = this,
      feature = me.up('nx-feature-content'),
      items = me.query('nx-drilldown-item'),
      item = items[index],
      createContainer;

    // Destroy any create wizard panels after current
    for (var i = index + 1; i < items.length; ++i) {
      createContainer = items[i].down('#create' + i);
      createContainer.removeAll();
    }

    if (item.el) {

      // Restore the current card
      items[index].getLayout().setActiveItem(items[index].cardIndex);

      // Hack to suppress resize events until the animation is complete
      if (animate) {
        me.ownerCt.suspendEvents(false);
        setTimeout(function () {
          me.ownerCt.resumeEvents();
          me.hideAllExceptAndFocus(index);
        }, 300);
      } else {
        me.hideAllExceptAndFocus(index);
      }

      // Slide the requested panel into view
      var left = feature.el.getX() - (index * feature.el.getWidth());
      if (animate) {
        me.animate({
          easing: 'easeInOut',
          duration: 200,
          to: {
            x: left
          }
        });
      } else {
        me.setX(left, false);
      }
      me.currentIndex = index;

      // Update the breadcrumb
      me.refreshBreadcrumb();
      me.resizeBreadcrumb();
    }
  },

  /**
   * @private
   * Pad the number of items in this drilldown to the specified index
   */
  padItems: function (index) {
    var me = this,
      items = me.query('nx-drilldown-item'),
      itemContainer;

    // Create new drilldown items (if needed)
    if (index > items.length - 1) {
      itemContainer = me.down('container');

      // Create empty panels if index > items.length
      for (var i = items.length; i <= index; ++i) {
        itemContainer.add(me.createDrilldownItem(i, undefined, undefined));
      }

      // Resize the panel
      me.syncSizeToOwner();
    }

    return me.query('nx-drilldown-item');
  },

  /**
   * @public
   * Shift this panel to display the referenced step in the create wizard
   *
   * @param index The index of the create wizard to display
   * @param animate Set to “true” if the view should slide into place, “false” if it should just appear
   * @param cmp An optional component to load into the panel
   */
  showCreateWizard: function (index, animate, cmp) {
    var me = this,
      items = me.padItems(index), // Pad the drilldown
      createContainer;

    // Add a component to the specified drilldown item (if specified)
    if (cmp) {
      createContainer = me.down('#create' + index);
      createContainer.removeAll();
      createContainer.add(cmp);
    }

    // Show the proper card
    items[index].setCardIndex(me.CREATE_INDEX);

    me.slidePanels(index, animate);
  },

  /**
   * @public
   * Shift this panel to display the referenced master or detail panel
   *
   * @param index The index of the master/detail panel to display
   * @param animate Set to “true” if the view should slide into place, “false” if it should just appear
   */
  showChild: function (index, animate) {
    var me = this,
      items = me.query('nx-drilldown-item'),
      item = items[index],
      createContainer;

    // Show the proper card
    item.setCardIndex(me.BROWSE_INDEX);

    // Destroy any create wizard panels
    for (var i = 0; i < items.length; ++i) {
      createContainer = items[i].down('#create' + i);
      createContainer.removeAll();
    }

    me.slidePanels(index, animate);
  },

  /**
   * @private
   * Update the breadcrumb based on the itemName and itemClass of drilldown items
   */
  refreshBreadcrumb: function() {
    var me = this,
      content = me.up('#feature-content'),
      root = content.down('#feature-root'),
      breadcrumb = content.down('#breadcrumb'),
      items = me.query('nx-drilldown-item');

    if (me.currentIndex == 0) {
      // Feature's home page, no breadcrumb required
      breadcrumb.hide();
      root.show();
    } else {
      breadcrumb.removeAll();

      // Make a breadcrumb (including icon and 'home' link)
      breadcrumb.add(
        {
          xtype: 'button',
          scale: 'large',
          ui: 'nx-drilldown',
          text: content.currentTitle,
          handler: function() {
            me.slidePanels(0, true);

            // Set the bookmark
            var bookmark = items[0].itemBookmark;
            if (bookmark) {
              NX.Bookmarks.bookmark(bookmark.obj, bookmark.scope);
            }
          }
        }
      );

      // Create the rest of the links
      for (var i = 1; i <= me.currentIndex && i < items.length; ++i) {
        breadcrumb.add([
          // Separator
          {
            xtype: 'label',
            cls: 'nx-breadcrumb-separator',
            text: '/'
          },
          {
            xtype: 'image',
            height: 16,
            width: 16,
            cls: 'nx-breadcrumb-icon ' + items[i].itemClass
          },

          // Create a closure within a closure to decouple 'i' from the current context
          (function(j) {
            return {
              xtype: 'button',
              scale: 'medium',
              ui: 'nx-drilldown',
              disabled: (i === me.currentIndex ? true : false), // Disabled if it’s the last item in the breadcrumb
              text: items[j].itemName,
              handler: function() {
                var bookmark = items[j].itemBookmark;
                if (bookmark) {
                  NX.Bookmarks.bookmark(bookmark.obj, bookmark.scope);
                }
                me.slidePanels(j, true);
              }
            }
          })(i)
        ]);
      }

      root.hide();
      breadcrumb.show();
    }
  },

  /*
   * @private
   * Resize the breadcrumb, truncate individual elements with ellipses as needed
   */
  resizeBreadcrumb: function() {
    var me = this,
      padding = 60, // Prevent truncation from happening too late
      parent = me.ownerCt,
      breadcrumb = me.up('#feature-content').down('#breadcrumb'),
      buttons, availableWidth, minimumWidth;

    // Is the breadcrumb clipped?
    if (parent && breadcrumb.getWidth() + padding > parent.getWidth()) {

      // Yes. Take measurements and get a list of buttons sorted by length (longest first)
      buttons = breadcrumb.query('button').splice(1);
      availableWidth = parent.getWidth();

      // What is the width of the breadcrumb, sans buttons?
      minimumWidth = breadcrumb.getWidth() + padding;
      for (var i = 0; i < buttons.length; ++i) {
        minimumWidth -= buttons[i].getWidth();
      }

      // Reduce the size of the longest button, until all buttons fit in the specified width
      me.reduceButtonWidth(buttons, availableWidth - minimumWidth);
    }
  },

  /*
   * @private
   * Reduce the width of a set of buttons, longest first, to a specified width
   *
   * @param buttons The list of buttons to resize
   * @param width The desired resize width (sum of all buttons)
   * @param minPerButton The minimum to resize each button (until all buttons are at this minimum)
   */
  reduceButtonWidth: function(buttons, width, minPerButton) {
    var currentWidth = 0,
      setToWidth;

    // Sort the buttons by width
    buttons = buttons.sort(function(a,b) { return b.getWidth() - a.getWidth() });

    // Calculate the current width of the buttons
    for (var i = 0; i < buttons.length; ++i) {
      currentWidth += buttons[i].getWidth();
    }

    // Find the next button to resize
    for (var i = 0; i < buttons.length; ++i) {

      // Shorten the longest button
      if (i < buttons.length - 1 && buttons[i].getWidth() > buttons[i+1].getWidth()) {

        // Will resizing this button make it fit?
        if (currentWidth - (buttons[i].getWidth() - buttons[i+1].getWidth()) <= width) {

          // Yes.
          setToWidth = width;
          for (var j = i + 1; j < buttons.length; ++j) {
            setToWidth -= buttons[j].getWidth();
          }
          buttons[i].setWidth(setToWidth);

          // Exit the algorithm
          break;
        }
        else {
          // No. Set the width of this button to that of the next button, and re-run the algorithm.
          buttons[i].setWidth(buttons[i+1].getWidth());
          this.reduceButtonWidth(buttons, width, minPerButton);
        }
      }
      else {
        // All buttons are the same length, shorten all by the same length
        setToWidth = Math.floor(width / buttons.length);
        for (var j = 0; j < buttons.length; ++j) {
          buttons[j].setWidth(setToWidth);
        }

        // Exit the algorithm
        break;
      }
    }
  }
});
