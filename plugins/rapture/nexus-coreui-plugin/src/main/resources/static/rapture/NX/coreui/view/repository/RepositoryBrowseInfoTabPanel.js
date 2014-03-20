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
/**
 * Browse repository info panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repository.RepositoryBrowseInfoTabPanel', {
  extend: 'Ext.Panel',
  alias: 'widget.nx-coreui-repository-browse-info-tabpanel',

  layout: 'card',

  header: {
    items: [
      {
        xtype: 'panel',
        layout: 'vbox',
        itemId: 'buttons',
        defaults: {
          scale: 'large'
        }
      }
    ]
  },

  add: function (component) {
    var me = this,
        added = me.callParent(arguments);

    Ext.Array.each(Ext.Array.from(added), function (cmp) {
      var btnConfig = {
        xtype: 'button',
        text: cmp.title,
        toggleGroup: me.getId(),
        panel: cmp,
        handler: function () {
          me.getLayout().setActiveItem(this.panel);
        }
      };

      if (cmp.buttonConfig) {
        btnConfig = Ext.apply(btnConfig, cmp.buttonConfig);
      }
      cmp.button = me.getHeader().down('#buttons').add(btnConfig);
    });

    me.getLayout().getActiveItem().button.toggle(true, true);

    return added;
  },

  remove: function (component) {
    var me = this,
        removed = me.callParent(arguments);

    if (component.button) {
      me.getHeader().down('#buttons').remove(component.button);
    }

    return removed;
  }

});
