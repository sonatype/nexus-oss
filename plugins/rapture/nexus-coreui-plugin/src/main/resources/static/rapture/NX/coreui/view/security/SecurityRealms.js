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
 * Security realms settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.security.SecurityRealms', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-security-realms',
  requires: [
    'NX.ext.form.field.ItemSelector'
  ],

  items: [
    // basic settings
    {
      xtype: 'nx-settingsform',
      settingsFormSuccessMessage: 'Security Realms settings $action',
      api: {
        load: 'NX.direct.coreui_SecurityRealm.read',
        submit: 'NX.direct.coreui_SecurityRealm.update'
      },
      editableCondition: NX.Conditions.isPermitted('nexus:settings', 'update'),
      editableMarker: 'You do not have permission to configure realms',

      items: [
        {
          xtype: 'label',
          html: '<p>Security realm settings.</p>'
        },
        {
          xtype: 'nx-itemselector',
          name: 'realms',
          buttons: ['up', 'add', 'remove', 'down'],
          fromTitle: 'Available',
          toTitle: 'Selected',
          store: 'RealmType',
          valueField: 'id',
          displayField: 'name',
          delimiter: null
        }
      ]
    }
  ]

});