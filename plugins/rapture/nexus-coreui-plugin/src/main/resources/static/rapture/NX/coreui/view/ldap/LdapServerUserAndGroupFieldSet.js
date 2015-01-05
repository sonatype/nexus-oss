/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
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
 * LDAP Server "User & Group" field set.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ldap.LdapServerUserAndGroupFieldSet', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-ldapserver-userandgroup-fieldset',
  requires: [
    'NX.I18n'
  ],

  defaults: {
    xtype: 'textfield',
    allowBlank: false
  },

  items: [
    {
      xtype: 'combo',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_GROUP_TEMPLATE'),
      emptyText: NX.I18n.get('ADMIN_LDAP_GROUP_TEMPLATE_PLACEHOLDER'),
      editable: false,
      store: 'LdapSchemaTemplate',
      displayField: 'name',
      queryMode: 'local',
      listeners: {
        select: function (combo, selected) {
          var data = Ext.apply({}, selected[0].getData());
          delete data.name;
          combo.up('form').getForm().setValues(data);
        }
      },
      allowBlank: true
    },

    //user
    {
      name: 'userBaseDn',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_USER_DN'),
      helpText: NX.I18n.get('ADMIN_LDAP_USER_DN_HELP'),
      allowBlank: true
    },
    {
      xtype: 'checkbox',
      name: 'userSubtree',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_USER_SUBTREE'),
      helpText: NX.I18n.get('ADMIN_LDAP_USER_SUBTREE_HELP')
    },
    {
      name: 'userObjectClass',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_USER_CLASS'),
      helpText: NX.I18n.get('ADMIN_LDAP_USER_CLASS_HELP')
    },
    {
      name: 'userLdapFilter',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_USER_FILTER'),
      helpText: NX.I18n.get('ADMIN_LDAP_USER_FILTER_HELP'),
      allowBlank: true
    },
    {
      name: 'userIdAttribute',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_USER_ID')
    },
    {
      name: 'userRealNameAttribute',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_USER_REAL')
    },
    {
      name: 'userEmailAddressAttribute',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_USER_EMAIL')
    },
    {
      name: 'userPasswordAttribute',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_USER_PASSWORD'),
      helpText: NX.I18n.get('ADMIN_LDAP_USER_PASSWORD_HELP'),
      allowBlank: true
    },
    // group
    {
      xtype: 'checkbox',
      name: 'ldapGroupsAsRoles',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_GROUP_MAP'),
      value: true,
      listeners: {
        change: function (checkbox, newValue) {
          var panel = checkbox.up('panel');

          panel.showOrHide('ldapGroupsAsRoles', newValue);
          panel.showOrHide('groupType', newValue ? panel.down('#groupType').getValue() : undefined);
        }
      }
    },
    {
      xtype: 'combo',
      name: 'groupType',
      itemId: 'groupType',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_GROUP_TYPE'),
      emptyText: NX.I18n.get('ADMIN_LDAP_GROUP_TYPE_PLACEHOLDER'),
      editable: false,
      store: [
        ['dynamic', NX.I18n.get('ADMIN_LDAP_GROUP_TYPE_DYNAMIC_ITEM')],
        ['static', NX.I18n.get('ADMIN_LDAP_GROUP_TYPE_STATIC_ITEM')]
      ],
      queryMode: 'local',
      listeners: {
        change: function (combo, newValue) {
          combo.up('panel').showOrHide('groupType', newValue);
        }
      },
      ldapGroupsAsRoles: [true]
    },
    {
      name: 'groupBaseDn',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_GROUP_DN'),
      helpText: NX.I18n.get('ADMIN_LDAP_GROUP_DN_HELP'),
      allowBlank: true,
      groupType: ['static']
    },
    {
      xtype: 'checkbox',
      name: 'groupSubtree',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_GROUP_SUBTREE'),
      helpText: NX.I18n.get('ADMIN_LDAP_GROUP_SUBTREE_HELP'),
      groupType: ['static']
    },
    {
      name: 'groupObjectClass',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_GROUP_CLASS'),
      helpText: NX.I18n.get('ADMIN_LDAP_GROUP_CLASS_HELP'),
      groupType: ['static']
    },
    {
      name: 'groupIdAttribute',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_GROUP_ID'),
      groupType: ['static']
    },
    {
      name: 'groupMemberAttribute',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_GROUP_ATTRIBUTE'),
      helpText: NX.I18n.get('ADMIN_LDAP_GROUP_ATTRIBUTE_HELP'),
      groupType: ['static']
    },
    {
      name: 'groupMemberFormat',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_GROUP_FORMAT'),
      helpText: NX.I18n.get('ADMIN_LDAP_GROUP_FORMAT_HELP'),
      groupType: ['static']
    },
    {
      name: 'userMemberOfAttribute',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_GROUP_MEMBER'),
      helpText: NX.I18n.get('ADMIN_LDAP_GROUP_MEMBER_HELP'),
      groupType: ['dynamic']
    }
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.callParent(arguments);

    me.showOrHide('ldapGroupsAsRoles', true);
    me.showOrHide('groupType', undefined);
  },

  /**
   * @private
   * Show & enable or hide and disable components that have attributes that matches the specified value.
   * @param attribute name of attribute
   * @param value to be matched in order to show
   */
  showOrHide: function (attribute, value) {
    var me = this,
        form = me.up('form'),
        components = me.query('component[' + attribute + ']');

    Ext.iterate(components, function (component) {
      if (value && component[attribute].indexOf(value) > -1) {
        component.enable();
        component.show();
      }
      else {
        component.disable();
        component.hide();
      }
    });
    if (form) {
      form.isValid();
    }
  }

});
