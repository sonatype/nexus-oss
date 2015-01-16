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
 * Commons repository settings fields.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repository.RepositorySettingsCommon', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-coreui-repository-settings-common',
  requires: [
    'NX.I18n'
  ],

  defaults: {
    xtype: 'textfield',
    allowBlank: false
  },

  items: [
    {
      name: 'id',
      itemId: 'id',
      fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_ID'),
      helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_ID_HELP'),
      readOnly: true,
      validator: function(value) {
        if (/^[a-zA-Z0-9_\-\.]+$/.test(value)) {
          return true;
        }
        return NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_ID_ERROR');
      }
    },
    {
      name: 'name',
      fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_NAME'),
      helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_NAME_HELP'),
    },
    {
      name: 'providerName',
      itemId: 'providerName',
      fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_PROVIDER'),
      readOnly: true,
      submitValue: false,
      allowBlank: true
    },
    {
      name: 'formatName',
      itemId: 'formatName',
      fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_FORMAT'),
      readOnly: true,
      submitValue: false,
      allowBlank: true
    }
  ]

});
