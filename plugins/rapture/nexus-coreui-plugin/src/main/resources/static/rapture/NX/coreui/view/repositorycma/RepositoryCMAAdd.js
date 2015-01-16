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
 * Add repository CMA window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repositorycma.RepositoryCMAAdd', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-repositorycma-add',
  requires: [
    'NX.Conditions',
    'NX.I18n'
  ],
  ui: 'inset',

  defaultFocus: 'recipe',

  /**
   * @override
   */
  initComponent: function() {
    var me = this;

    me.items = {
      xtype: 'nx-coreui-repositorycma-settings-form',
      api: {
        submit: 'NX.direct.coreui_RepositoryCMA.create'
      },
      settingsFormSuccessMessage: function(data) {
        return NX.I18n.get('ADMIN_REPOSITORIES_CMA_CREATE_SUCCESS') + data['name'];
      },
      editableCondition: NX.Conditions.isPermitted('nexus:repositories', 'create'),
      editableMarker: NX.I18n.get('ADMIN_REPOSITORIES_CREATE_ERROR'),

      buttons: [
        { text: NX.I18n.get('ADMIN_REPOSITORIES_LIST_NEW_BUTTON'), action: 'add', formBind: true, ui: 'primary' },
        { text: NX.I18n.get('GLOBAL_DIALOG_ADD_CANCEL_BUTTON'), handler: function() {
          this.up('nx-drilldown').showChild(0, true);
        }}
      ]
    };

    me.callParent();

    me.down('#name').setReadOnly(false);
    me.down('nx-settingsform').add(0, {
      xtype: 'combo',
      name: 'recipe',
      itemId: 'recipe',
      fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_CMA_SETTINGS_RECIPE'),
      emptyText: NX.I18n.get('ADMIN_REPOSITORIES_CMA_SETTINGS_RECIPE_PLACEHOLDER'),
      editable: false,
      store: 'RepositoryCMARecipe',
      queryMode: 'local',
      displayField: 'name',
      valueField: 'id'
    });
  }
});
