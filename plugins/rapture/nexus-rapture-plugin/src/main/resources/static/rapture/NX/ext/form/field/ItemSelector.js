/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

/**
 * Extension of Ext.ux.form.ItemSelector to allow better control over button configurations.
 */
Ext.define('NX.ext.form.field.ItemSelector', {
  extend: 'Ext.ux.form.ItemSelector',
  alias: 'widget.nx-itemselector',

  width: 600,

  /**
   * Override super *private* impl so we can control the button configuration.
   *
   * @override
   * @private
   */
  createButtons: function () {
    var me = this,
        buttons = me.callSuper();

    if (!me.hideNavIcons) {
      var i = 0;
      Ext.Array.forEach(me.buttons, function (name) {
        me.customizeButton(name, buttons[i++]);
      });
    }

    return buttons;
  },

  /**
   * Replace iconCls with glyph.
   *
   * @private
   *
   * @param name
   * @param button
   */
  customizeButton: function (name, button) {
    var me = this;

    // remove icon
    delete button.iconCls;

    // replace with glyph
    switch (name) {
      case 'top':
        button.glyph = 'xf102@FontAwesome'; // fa-angle-double-up
        break;
      case 'up':
        button.glyph = 'xf106@FontAwesome'; // fa-angle-up
        break;
      case 'add':
        button.glyph = 'xf105@FontAwesome'; // fa-angle-right
        break;
      case 'remove':
        button.glyph = 'xf104@FontAwesome'; // fa-angle-left
        break;
      case 'down':
        button.glyph = 'xf107@FontAwesome'; // fa-angle-down
        break;
      case 'bottom':
        button.glyph = 'xf103@FontAwesome'; // fa-angle-double-down
        break;
    }
  },

  // HACK: Looks like original item selector forgot to unbind from store which results in NPEs in #populateFromStore
  onDestroy: function () {
    var me = this;

    if (me.store) {
      me.store.un('load', me.populateFromStore, me);
    }
    this.callParent();
  }

});
