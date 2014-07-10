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
 * Search panel.
 * TODO implement
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.search.SearchFeature', {
  extend: 'Ext.Panel',
  alias: 'widget.nx-searchfeature',

  layout: 'border',

  initComponent: function () {
    var me = this;

    me.items = [
      {
        xtype: 'panel',
        itemId: 'criteria',

        region: 'north',
        header: false,

        layout: {
          type: 'hbox',
          align: 'bottom'
        },
        bodyPadding: 10,

        tbar: [
          { xtype: 'button', text: 'Save', glyph: 'xf0c7@FontAwesome' /* fa-save */, action: 'save' },
        ]
      },
      {
        xtype: 'panel',
        region: 'center',
        layout: 'border',
        items: [
          {
            xtype: 'grid',

            region: 'center',
            collapsible: true,
            headerPosition: 'left',
            header: false,

            allowDeselect: true,
            viewConfig: {
              emptyText: 'No results',
              deferEmptyText: false
            },
            store: 'SearchResult',
            columns: [
              { header: 'Component', dataIndex: 'uri', flex: 1 },
              { header: 'Version', dataIndex: 'version', flex: 1 },
              { header: 'Repository', dataIndex: 'repositoryId', flex: 1 }
            ],
            dockedItems: [
              {
                xtype: 'pagingtoolbar',
                store: 'SearchResult',
                dock: 'top',
                displayInfo: true
              }
            ]
          },
          {
            xtype: 'nx-coreui-repositorybrowse-storagefilecontainer',
            region: 'east',
            collapsible: true,
            split: true,
            width: '50%',
            headerPosition: 'right',
            hidden: true
          }
        ]
      }
    ];

    me.callParent(arguments);
  }

});