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
 * Select repository template window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repository.RepositorySelectTemplate', {
  extend: 'NX.view.drilldown.Master',
  alias: 'widget.nx-coreui-repository-selecttemplate',
  requires: [
    'NX.I18n'
  ],

  store: 'RepositoryTemplate',
  columns: [
    {
      xtype: 'nx-iconcolumn',
      width: 36,
      iconVariant: 'x16',
      iconName: function() {
        return 'repository-default';
      }
    },
    { header: NX.I18n.get('ADMIN_REPOSITORIES_SELECT_PROVIDER_COLUMN'), dataIndex: 'providerName', flex: 2 },
    { header: NX.I18n.get('ADMIN_REPOSITORIES_SELECT_TYPE_COLUMN'), dataIndex: 'type', flex: 1 }
  ]

});
