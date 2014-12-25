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
 * LDAP Server "User & Group" field set.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ldap.LdapServerUserAndGroupFieldSet', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-ldapserver-userandgroup-fieldset',

  defaults: {
    xtype: 'textfield',
    allowBlank: false
  },

  items: [
    {
      xtype: 'combo',
      fieldLabel: 'Template',
      emptyText: 'select a template to apply',
      helpText : 'Select a template to apply some default values to the configuration.',
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
      fieldLabel: 'Base DN',
      helpText: 'Base location in the LDAP that the users are found, relative to the search base ("ou=people").',
      allowBlank: true
    },
    {
      xtype: 'checkbox',
      name: 'userSubtree',
      fieldLabel: 'User Subtree',
      helpText: 'Check this box if users are located in structures below the user Base DN.'
    },
    {
      name: 'userObjectClass',
      fieldLabel: 'Object Class',
      helpText: 'LDAP class for user objects ("inetOrgPerson").'
    },
    {
      name: 'userLdapFilter',
      fieldLabel: 'User Filter',
      helpText: 'LDAP search filter to additionally limit user search (for example "attribute=foo" or "(|(mail=*@domain.com)(uid=dom*))".',
      allowBlank: true
    },
    {
      name: 'userIdAttribute',
      fieldLabel: 'User ID Attribute',
      helpText: 'LDAP attribute containing user id ("userIdAttribute").'
    },
    {
      name: 'userRealNameAttribute',
      fieldLabel: 'Real Name Attribute',
      helpText: 'LDAP attribute containing the real name of the user ("cn").'
    },
    {
      name: 'userEmailAddressAttribute',
      fieldLabel: 'E-Mail Attribute',
      helpText: 'LDAP attribute containing e-mail address ("emailAddressAttribute").'
    },
    {
      name: 'userPasswordAttribute',
      fieldLabel: 'Password Attribute',
      helpText: 'LDAP attribute containing the password ("userPassword").  If this field is blank the user will be authenticated against a bind with the LDAP server.',
      allowBlank: true
    },
    // group
    {
      xtype: 'checkbox',
      name: 'ldapGroupsAsRoles',
      fieldLabel: 'Map LDAP groups as Roles',
      helpText: 'Check this box if LDAP groups should be mapped as roles.',
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
      fieldLabel: 'Group Type',
      emptyText: 'select a group type',
      editable: false,
      store: [
        ['dynamic', 'Dynamic Groups'],
        ['static', 'Static Groups']
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
      fieldLabel: 'Group Base DN',
      helpText: 'Base location in the LDAP that the groups are found, relative to the search base ("ou=Group").',
      allowBlank: true,
      groupType: ['static']
    },
    {
      xtype: 'checkbox',
      name: 'groupSubtree',
      fieldLabel: 'Group Subtree',
      helpText: 'Check this box if groups are located in structures below the group Base DN.',
      groupType: ['static']
    },
    {
      name: 'groupObjectClass',
      fieldLabel: 'Group Object Class',
      helpText: 'LDAP class for group objects ("posixGroup").',
      groupType: ['static']
    },
    {
      name: 'groupIdAttribute',
      fieldLabel: 'Group ID Attribute',
      helpText: 'LDAP attribute containing group id ("cn").',
      groupType: ['static']
    },
    {
      name: 'groupMemberAttribute',
      fieldLabel: 'Group Member Attribute',
      helpText: 'LDAP attribute containing the usernames for the group.',
      groupType: ['static']
    },
    {
      name: 'groupMemberFormat',
      fieldLabel: 'Group Member Format',
      helpText: 'The format of User ID stored in the Group Member Attribute. A token "${dn}" can be used to lookup the FQDN of the user or use something like "uid=${username},ou=people,o=sonatype" where "${username}" is replaced with the Username value.',
      groupType: ['static']
    },
    {
      name: 'userMemberOfAttribute',
      fieldLabel: 'Group Member of Attribute',
      helpText: 'Groups are generally one of two types in LDAP systems -'
          + ' static or dynamic. A static group maintains its own'
          + ' membership list. A dynamic group records its membership on a'
          + ' user entry. If dynamic groups this should be set to the'
          + ' attribute used to store the attribute that holds groups DN in the user object.',
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
