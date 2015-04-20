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
 * Configuration specific to Http connections for repositories.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repository.facet.NugetProxyFacet', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-coreui-repository-nugetproxy-facet',
  requires: [
    'NX.I18n'
  ],

  defaults: {
    itemCls: 'required-field'
  },

  /**
   * @override
   */
  initComponent: function() {
    var me = this;

    me.items = [
      {
        xtype: 'numberfield',
        name: 'nugetProxy.queryCacheSize',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_QUERY_CACHE_SIZE'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_QUERY_CACHE_SIZE_HELP'),
        minValue: 0,
        value: 300
      },
      {
        xtype: 'numberfield',
        name: 'nugetProxy.queryCacheItemMaxAge',
        fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_QUERY_CACHE_ITEM_MAX_AGE'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_QUERY_CACHE_ITEM_MAX_AGE_HELP'),
        minValue: 0,
        value: 3600
      }
    ];

    me.callParent(arguments);
  }

});
