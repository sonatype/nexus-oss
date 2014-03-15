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
 * Browse repository storage panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repository.RepositoryBrowseStorage', {
  extend: 'Ext.Panel',
  alias: ['widget.nx-coreui-repository-browse-storage', 'widget.nx-coreui-repository-itemcontainer'],

  layout: 'border',

  items: [
    {
      xtype: 'nx-coreui-repository-browse-storage-tree',
      region: 'center',
      collapsible: true,
      headerPosition: 'left',
      header: false
    },
    {
      xtype: 'nx-coreui-repository-browse-info-tabpanel',
      region: 'east',
      collapsible: true,
      split: true,
      width: '50%',
      headerPosition: 'right',
      hidden: true
    }
  ],

  addTab: function (tab) {
    var me = this,
        tabpanel = me.down('nx-coreui-repository-browse-info-tabpanel'),
        added = tabpanel.add(tab);

    tabpanel.show();

    return added;
  },

  removeTab: function (tab) {
    var me = this,
        tabpanel = me.down('nx-coreui-repository-browse-info-tabpanel');

    tabpanel.remove(tab);
    if (tabpanel.items.length === 0) {
      tabpanel.hide();
    }
  }

});
