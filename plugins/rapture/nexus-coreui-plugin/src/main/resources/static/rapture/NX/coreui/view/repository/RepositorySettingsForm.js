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
 * Repository "Settings" form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repository.RepositorySettingsForm', {
  extend: 'NX.view.SettingsForm',
  alias: 'widget.nx-coreui-repository-settings-form',
  requires: [
    'NX.Conditions',
    'NX.I18n',
    'NX.coreui.service.RepositoryFormService'
  ],

  settingsFormSuccessMessage: function(data) {
    return NX.I18n.get('ADMIN_REPOSITORIES_UPDATE_SUCCESS') + data['name'];
  },

  editableMarker: NX.I18n.get('ADMIN_REPOSITORIES_UPDATE_ERROR'),

  items: [
    {
      xtype: 'textfield',
      name: 'name',
      itemId: 'name',
      fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_NAME'),
      readOnly: true
    },
    {
      xtype: 'textfield',
      name: 'format',
      itemId: 'format',
      fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_FORMAT'),
      allowBlank: true,
      readOnly: true
    },
    {
      xtype: 'textfield',
      name: 'type',
      itemId: 'type',
      fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_TYPE'),
      allowBlank: true,
      readOnly: true
    },
    {
      xtype: 'hiddenfield',
      name: 'attributes'
    },
    {xtype: 'nx-coreui-formfield-settingsfieldset'}
  ],

  /**
   * @override
   * Imports repository into settings field set.
   * @param {NX.model.Repository} model repository model
   */
  loadRecord: function(model) {
    var me = this,
        settingsFieldSet = me.down('nx-coreui-formfield-settingsfieldset');
    me.callParent(arguments);

    settingsFieldSet.importProperties(NX.coreui.service.RepositoryFormService.propertiesForModel(model),
        NX.coreui.service.RepositoryFormService.fieldsForModel(model));
  },

  /**
   * @override
   * Exports repository from settings field set.
   * @returns {Object} form values
   */
  getValues: function() {
    var me = this,
        values = me.getForm().getFieldValues(),
        record = me.getForm().getRecord(),
        repository = {
          name: values.name,
          recipe: record ? record.get('format') + '-' + record.get('type') : values.recipe,
          attributes: null
        };
    var properties = me.down('nx-coreui-formfield-settingsfieldset').exportProperties(values);
    repository.attributes =  NX.coreui.service.RepositoryFormService.mapAttributes(repository.recipe, properties);
    return repository;
  }
});
