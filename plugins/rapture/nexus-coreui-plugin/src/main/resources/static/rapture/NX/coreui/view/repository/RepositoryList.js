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
 * Repository grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repository.RepositoryList', {
  extend: 'Ext.grid.Panel',
  alias: 'widget.nx-coreui-repository-list',

  store: 'Repository',

  columns: [
    {
      xtype: 'iconcolumn',
      width: 36,
      iconVariant: 'x16',
      iconName: function () {
        return 'repository-default';
      }
    },
    { header: 'Name', dataIndex: 'name', flex: 1 },
    { header: 'Type', dataIndex: 'type',
      renderer: function (value) {
        return Ext.String.capitalize(value);
      }
    },
    { header: 'Format', dataIndex: 'formatName' },
    { header: 'Provider', dataIndex: 'providerName' }
  ],

  emptyText: 'No repositories defined',

  tbar: [
    { xtype: 'button', text: 'New', glyph: 'xf055@FontAwesome' /* fa-plus-circle */, action: 'new', disabled: true,
      menu: []
    },
    { xtype: 'button', text: 'Delete', glyph: 'xf056@FontAwesome' /* fa-minus-circle */, action: 'delete', disabled: true },
    '-',
    { xtype: 'button', text: 'Browse', glyph: 'xf0e8@FontAwesome' /* fa-sitemap */, action: 'browse', disabled: true }
  ],

  plugins: [
    { ptype: 'gridfilterbox', emptyText: 'No repositories matched criteria "$filter"' }
  ]

});
