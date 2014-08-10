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
/*global Ext, NX*/

/**
 * Repository route grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repositoryroute.RepositoryRouteList', {
  extend: 'Ext.grid.Panel',
  alias: 'widget.nx-coreui-repositoryroute-list',

  store: 'RepositoryRoute',

  columns: [
    {
      xtype: 'nx-iconcolumn',
      width: 36,
      iconVariant: 'x16',
      iconName: function () {
        return 'repositoryroute-default';
      }
    },
    { header: 'Route', dataIndex: 'pattern', flex: 1 },
    { header: 'Rule Type', dataIndex: 'mappingType', renderer: function (val) {
      return {
        BLOCKING: 'Blocking',
        INCLUSION: 'Inclusive',
        EXCLUSION: 'Exclusive'
      }[val];
    }},
    { header: 'Group', dataIndex: 'groupName' },
    { header: 'Repositories', dataIndex: 'mappedRepositoriesNames', flex: 1 }
  ],

  emptyText: 'No repository routes defined',

  tbar: [
    { xtype: 'button', text: 'New', glyph: 'xf055@FontAwesome' /* fa-plus-circle */, action: 'new', disabled: true },
    { xtype: 'button', text: 'Delete', glyph: 'xf056@FontAwesome' /* fa-minus-circle */, action: 'delete', disabled: true }
  ],

  plugins: [{ ptype: 'gridfilterbox', emptyText: 'No repository route matched criteria "$filter"' }]

});
