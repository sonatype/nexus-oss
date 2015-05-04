/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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
 * Blobstore grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.blobstore.BlobstoreList', {
  extend: 'NX.view.drilldown.Master',
  alias: 'widget.nx-coreui-blobstore-list',
  requires: [
    'NX.I18n'
  ],

  config: {
    stateful: true,
    stateId: 'nx-coreui-blobstore-list'
  },

  store: 'NX.coreui.store.Blobstore',
  
  columns: [
    {
      xtype: 'nx-iconcolumn',
      width: 36,
      iconVariant: 'x16',
      iconName: function() {
        return 'blobstore-default';
      }
    },
    { header: NX.I18n.get('ADMIN_BLOBSTORES_LIST_NAME_COLUMN'), dataIndex: 'name', stateId: 'name', flex: 1 },
    { header: NX.I18n.get('ADMIN_BLOBSTORES_LIST_TYPE_COLUMN'), dataIndex: 'type', stateId: 'type' },
    { header: NX.I18n.get('ADMIN_BLOBSTORES_LIST_BLOB_COUNT_COLUMN'), dataIndex: 'blobCount', stateId: 'blobCount' },
    { header: NX.I18n.get('ADMIN_BLOBSTORES_LIST_TOTAL_SIZE_COLUMN'), dataIndex: 'totalSize', stateId: 'totalSize', 
      renderer:Ext.util.Format.fileSize 
    },
    { header: NX.I18n.get('ADMIN_BLOBSTORES_LIST_AVAILABLE_SPACE_COLUMN'), dataIndex: 'availableSpace', 
      stateId: 'availableSpace', renderer: Ext.util.Format.fileSize, flex: 1 
    }
  ],

  viewConfig: {
    emptyText: NX.I18n.get('ADMIN_BLOBSTORES_LIST_EMPTY_STATE'),
    deferEmptyText: false
  },

  dockedItems: [{
    xtype: 'toolbar',
    dock: 'top',
    cls: 'nx-actions nx-borderless',
    items: [
      {
        xtype: 'button',
        text: NX.I18n.get('ADMIN_BLOBSTORES_LIST_NEW_BUTTON'),
        glyph: 'xf055@FontAwesome' /* fa-plus-circle */,
        action: 'new',
        disabled: true
      }
    ]
  }],

  plugins: [
    { ptype: 'gridfilterbox', emptyText: NX.I18n.get('ADMIN_BLOBSTORES_LIST_FILTER_ERROR') }
  ]

});
