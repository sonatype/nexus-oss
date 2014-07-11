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
 * Browse repository storage tree panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repositorybrowse.BrowseRepositoryTree', {
  extend: 'Ext.tree.Panel',
  alias: 'widget.nx-coreui-repositorybrowse-tree',

  viewConfig: {
    markDirty: false
  },

  rootVisible: false,

  columns: [
    {
      xtype: 'treecolumn',
      text: 'Storage File',
      flex: 2,
      sortable: true,
      dataIndex: 'text'
    },
    {
      xtype: 'nx-iconcolumn',
      dataIndex: 'inStorage',
      text: 'Storage',
      sortable: false,
      menuDisabled: true,
      width: 80,
      iconNamePrefix: 'repositorybrowse-',
      iconVariant: 'x16',
      iconName: function(value, meta, record) {
        return value && record.get('path') !== '/' ? 'inStorage' : undefined;
      }
    },
    {
      xtype: 'nx-iconcolumn',
      dataIndex: 'inIndex',
      text: 'Index',
      sortable: false,
      menuDisabled: true,
      width: 80,
      iconNamePrefix: 'repositorybrowse-',
      iconVariant: 'x16',
      iconName: function(value, meta, record) {
        return value && record.get('path') !== '/' ? 'inIndex' : undefined;
      }
    }
  ],

  /**
   * @override
   */
  initComponent: function() {
    var me = this,
        icons = NX.getApplication().getIconController();

    me.store = Ext.create('Ext.data.TreeStore', {
      fields: [
        'repositoryId',
        'path',
        'text',
        'type',
        'processed',
        'indexLoaded',
        'storageLoaded',
        { name: 'inIndex', defaultValue: false },
        { name: 'inStorage', defaultValue: false },
        {
          name: 'iconCls',
          convert: function(val, row) {
            var iconCls = val;
            if (row.data.type) {
              if (icons.findIcon('repository-item-type-' + row.data.type, 'x16')) {
                iconCls = NX.Icons.cls('repository-item-type-' + row.data.type, 'x16');
              }
              else {
                iconCls = NX.Icons.cls('repository-item-type-default', 'x16');
              }
            }
            return iconCls;
          }
        }
      ],

      root: {
        expanded: true,
        text: 'Repositories',
        children: []
      }
    });

    me.callParent(arguments);

    me.addEvents(
        /**
         * @event beforecontextmenushow
         * Fires before showing the context menu.
         * @param {Ext.menu.Menu} to be shown
         * @param {NX.coreui.model.Repository} repository
         * param {Ext.data.NodeInterface} clicked node
         */
        'beforecontextmenushow'
    );
  }

});
