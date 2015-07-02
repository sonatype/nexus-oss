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
/*global Ext*/

/**
 * Content panel.
 *
 * @since 3.0
 */
Ext.define('NX.view.feature.Content', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-feature-content',

  itemId: 'feature-content',
  ui: 'nx-feature-content',
  cls: 'nx-feature-content',
  layout: 'fit',

  /**
   * @private
   * If false, show a warning modal when you’re about to discard unsaved changes by navigating away
   */
  discardUnsavedChanges: false,

  header: {
    items: [
      {
        xtype: 'panel',
        layout: { type: 'hbox' },
        itemId: 'breadcrumb',
        hidden: true
      },
      {
        xtype: 'panel',
        layout: { type: 'hbox' },
        itemId: 'feature-root',
        items: [
          {
            xtype: 'label',
            cls: 'nx-feature-name',
            itemId: 'title'
          },
          {
            xtype: 'label',
            cls: 'nx-feature-description',
            itemId: 'description'
          }
        ]
      }
    ]
  },

  listeners: {
    afterRender: function(){
      var me = this,
          title = me.down('#title'),
          description = me.down('#description');

      me.setTitle(title.text);
      me.setDescription(description.text);
    }
  },

  /**
   * Show the feature root (hide the breadcrumb)
   */
  showRoot: function() {
    var me = this;
    var root = me.down('#feature-root');
    var breadcrumb = me.down('#breadcrumb');

    root.show();
    breadcrumb.hide();
  },

  /**
   * The currently set title, so subpanels can access it
   * @param text
   */
  currentTitle: undefined,

  /**
   * Custom handling for title since we are using custom header component.
   *
   * @override
   * @param text
   */
  setTitle: function(text) {
    var me = this,
        label = me.down('#title');

    me.callParent(arguments);

    label.setText(text);
    me.currentTitle = text;
  },

  /**
   * Set description text.
   *
   * @public
   * @param text
   */
  setDescription: function(text) {
    var label = this.down('#description');

    label.setText(text);
  },

  /**
   * The currently set iconCls, so we can remove it when changed.
   *
   * @private
   */
  currentIconCls: undefined,

  /**
   * @public
   * Reset the discardUnsavedChanges flag (false by default)
   */
  resetUnsavedChangesFlag: function(enable) {
    var me = this;

    if (enable) {
      me.discardUnsavedChanges = true;
    }
    else {
      me.discardUnsavedChanges = false;
    }
  }
});
