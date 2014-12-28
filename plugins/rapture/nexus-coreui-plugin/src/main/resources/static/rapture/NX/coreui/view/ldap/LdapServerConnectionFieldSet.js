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
 * LDAP Server "Connection" field set.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ldap.LdapServerConnectionFieldSet', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-ldapserver-connection-fieldset',

  defaults: {
    xtype: 'textfield',
    allowBlank: false,
    bindGroup: 'connection'
  },

  items: [
    {
      name: 'name',
      helpText: NX.I18n.get('ADMIN_LDAP_CONNECTION_NAME_HELP'),
      itemId: 'name',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_NAME'),
      emptyText: NX.I18n.get('ADMIN_LDAP_CONNECTION_NAME_PLACEHOLDER')
    },
    {
      xtype: 'combo',
      name: 'protocol',
      itemId: 'protocol',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_PROTOCOL'),
      helpText: NX.I18n.get('ADMIN_LDAP_CONNECTION_PROTOCOL_HELP'),
      emptyText: NX.I18n.get('ADMIN_LDAP_CONNECTION_PROTOCOL_PLACEHOLDER'),
      editable: false,
      store: [
        ['ldap', NX.I18n.get('ADMIN_LDAP_CONNECTION_PROTOCOL_PLAIN_ITEM')],
        ['ldaps', NX.I18n.get('ADMIN_LDAP_CONNECTION_PROTOCOL_SECURE_ITEM')]
      ],
      queryMode: 'local',
      useTrustStore: function (combo) {
        var form = combo.up('form');
        if (combo.getValue() === 'ldaps' && form.down('#host').getValue() && form.down('#port').getValue()) {
          return {
            name: 'useTrustStore',
            host: form.down('#host'),
            port: form.down('#port')
          };
        }
        return undefined;
      }
    },
    {
      name: 'host',
      itemId: 'host',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_HOST'),
      helpText: NX.I18n.get('ADMIN_LDAP_CONNECTION_HOST_HELP'),
      listeners: {
        change: function(){
          var protocol = this.up('form').down('#protocol');
          protocol.fireEvent('change', protocol, protocol.getValue(), protocol.getValue());
        }
      }
    },
    {
      xtype: 'numberfield',
      name: 'port',
      itemId: 'port',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_PORT'),
      helpText: NX.I18n.get('ADMIN_LDAP_CONNECTION_PORT_HELP'),
      minValue: 1,
      maxValue: 65535,
      allowDecimals: false,
      allowExponential: false,
      listeners: {
        change: function(){
          var protocol = this.up('form').down('#protocol');
          protocol.fireEvent('change', protocol, protocol.getValue(), protocol.getValue());
        }
      }
    },
    {
      name: 'searchBase',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_BASE'),
      helpText: NX.I18n.get('ADMIN_LDAP_CONNECTION_BASE_HELP')
    },
    {
      xtype: 'combo',
      name: 'authScheme',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_AUTH'),
      emptyText: NX.I18n.get('ADMIN_LDAP_CONNECTION_AUTH_PLACEHOLDER'),
      editable: false,
      store: [
        ['simple', NX.I18n.get('ADMIN_LDAP_CONNECTION_AUTH_SIMPLE_ITEM')],
        ['none', NX.I18n.get('ADMIN_LDAP_CONNECTION_AUTH_ANONYMOUS_ITEM')],
        ['DIGEST-MD5', NX.I18n.get('ADMIN_LDAP_CONNECTION_AUTH_DIGEST_ITEM')],
        ['CRAM-MD5', NX.I18n.get('ADMIN_LDAP_CONNECTION_AUTH_CRAM_ITEM')]
      ],
      queryMode: 'local',
      listeners: {
        change: function (combo, newValue) {
          this.up('panel').showOrHide('authScheme', newValue);
        }
      }
    },
    {
      name: 'authRealm',
      itemId: 'authRealm',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_SASL'),
      helpText: NX.I18n.get('ADMIN_LDAP_CONNECTION_SASL_HELP'),
      allowBlank: true,
      authScheme: ['DIGEST-MD5', 'CRAM-MD5']
    },
    {
      name: 'authUsername',
      itemId: 'authUsername',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_USERNAME'),
      helpText: NX.I18n.get('ADMIN_LDAP_CONNECTION_USERNAME_HELP'),
      authScheme: ['simple', 'DIGEST-MD5', 'CRAM-MD5']
    },
    {
      xtype: 'nx-password',
      name: 'authPassword',
      itemId: 'authPassword',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_PASSWORD'),
      inputType: 'password',
      helpText: NX.I18n.get('ADMIN_LDAP_CONNECTION_PASSWORD_HELP'),
      authScheme: ['simple', 'DIGEST-MD5', 'CRAM-MD5']
    },
    {
      xtype: 'numberfield',
      name: 'connectionTimeout',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_TIMEOUT'),
      helpText: NX.I18n.get('ADMIN_LDAP_CONNECTION_TIMEOUT_HELP'),
      value: 30
    },
    {
      xtype: 'numberfield',
      name: 'connectionRetryDelay',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_RETRY'),
      helpText: NX.I18n.get('ADMIN_LDAP_CONNECTION_RETRY_HELP'),
      value: 300
    },
    {
      xtype: 'numberfield',
      name: 'cacheTimeout',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_CACHE'),
      helpText: NX.I18n.get('ADMIN_LDAP_CONNECTION_CACHE_HELP'),
      value: 600
    }
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.callParent(arguments);

    me.showOrHide('authScheme', undefined);
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
