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
    'NX.I18n'
  ],

  api: {
    submit: 'NX.direct.coreui_Repository.update'
  },
  settingsFormSuccessMessage: function(data) {
    return NX.I18n.get('ADMIN_REPOSITORIES_UPDATE_SUCCESS') + data['name'];
  },

  editableMarker: NX.I18n.get('ADMIN_REPOSITORIES_UPDATE_ERROR'),

  initComponent: function() {
    var me = this;

    me.editableCondition = me.editableCondition || NX.Conditions.isPermitted('nexus:repositories', 'update');

    me.items = me.items || [];
    Ext.Array.insert(me.items, 0, [
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
    ]);

    me.callParent(arguments);

    //map repository attributes raw map structure to/from a flattened representation
    Ext.override(me.getForm(), {
      getValues: function() {
        var processed = { attributes: {} },
            values = this.callParent(arguments);

        Ext.Object.each(values, function(key, value) {
          var segments = key.split('.'),
              parent = segments.length == 1 ? processed : processed['attributes'];

          Ext.each(segments, function(segment, pos) {
            if (pos === segments.length - 1) {
              parent[segment] = value;
            }
            else {
              if (!parent[segment]) {
                parent[segment] = {};
              }
              parent = parent[segment];
            }
          });
        });

        return processed;
      },

      setValues: function(values) {
        var process = function(child, prefix) {
              Ext.Object.each(child, function(key, value) {
                var newPrefix = (prefix ? prefix + '.' : '') + key;
                if (Ext.isObject(value)) {
                  process(value, newPrefix);
                }
                else {
                  values[newPrefix] = value;
                }
              });
            };

        if (values['attributes']) {
          process(values['attributes']);
        }

        this.callParent(arguments);
      }
    });
  }

});
