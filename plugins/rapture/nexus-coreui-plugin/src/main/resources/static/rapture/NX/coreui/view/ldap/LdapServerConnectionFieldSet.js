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
 * LDAP Server "Connection" field set.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ldap.LdapServerConnectionFieldSet', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-ldapserver-connection-fieldset',
  requires: [
    'NX.I18n'
  ],

  defaults: {
    xtype: 'textfield',
    allowBlank: false,
    bindGroup: 'connection'
  },

  items: [
    {
      xtype: 'hiddenfield',
      name: 'id'
    },
    {
      name: 'name',
      itemId: 'name',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_NAME')
    },
    {
      xtype: 'label',
      text: NX.I18n.get('ADMIN_LDAP_CONNECTION_ADDRESS'),
      style: {
        fontWeight: 'bold',
        display: 'block',
        marginTop: '10px',
        marginBottom: '5px'
      }
    },
    {
      xtype: 'label',
      text: NX.I18n.get('ADMIN_LDAP_CONNECTION_ADDRESS_HELP'),
      style: {
        fontSize: '10px',
        display: 'block',
        marginBottom: '1px'
      }
    },
    {
      xtype: 'combo',
      name: 'protocol',
      itemId: 'protocol',
      cls: 'nx-float-left',
      blankText: 'Required',
      width: 85,
      emptyText: NX.I18n.get('ADMIN_LDAP_CONNECTION_PROTOCOL_PLACEHOLDER'),
      editable: false,
      store: [
        ['ldap', NX.I18n.get('ADMIN_LDAP_CONNECTION_PROTOCOL_PLAIN_ITEM')],
        ['ldaps', NX.I18n.get('ADMIN_LDAP_CONNECTION_PROTOCOL_SECURE_ITEM')]
      ],
      queryMode: 'local',
      listeners: {
        change: function(){
          var protocol = this.up('form').down('#port');
          protocol.fireEvent('change', protocol, protocol.getValue(), protocol.getValue());
        }
      }
    },
    {
      xtype: 'label',
      cls: 'nx-float-left nx-interstitial-label',
      text: '://'
    },
    {
      name: 'host',
      itemId: 'host',
      cls: 'nx-float-left',
      blankText: 'Required',
      width: 405,
      emptyText: NX.I18n.get('ADMIN_LDAP_CONNECTION_HOST_PLACEHOLDER'),
      listeners: {
        change: function(){
          var protocol = this.up('form').down('#port');
          protocol.fireEvent('change', protocol, protocol.getValue(), protocol.getValue());
        }
      }
    },
    {
      xtype: 'label',
      cls: 'nx-float-left nx-interstitial-label',
      text: ':'
    },
    {
      xtype: 'numberfield',
      name: 'port',
      itemId: 'port',
      cls: 'nx-float-left',
      blankText: 'Required',
      width: 75,
      emptyText: NX.I18n.get('ADMIN_LDAP_CONNECTION_PORT_PLACEHOLDER'),
      minValue: 1,
      maxValue: 65535,
      allowDecimals: false,
      allowExponential: false,
      useTrustStore: function (field) {
        var form = field.up('form');
        if (form.down('#protocol').getValue() === 'ldaps' && form.down('#host').getValue() && field.getValue()) {
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
      name: 'searchBase',
      fieldLabel: NX.I18n.get('ADMIN_LDAP_CONNECTION_BASE'),
      helpText: NX.I18n.get('ADMIN_LDAP_CONNECTION_BASE_HELP'),
      cls: 'nx-clear-both'
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
      xtype: 'label',
      text: NX.I18n.get('ADMIN_LDAP_CONNECTION_RULES'),
      style: {
        fontWeight: 'bold',
        display: 'block',
        marginTop: '10px',
        marginBottom: '5px'
      }
    },
    {
      xtype: 'label',
      text: NX.I18n.get('ADMIN_LDAP_CONNECTION_RULES_HELP'),
      style: {
        fontSize: '10px',
        display: 'block',
        marginBottom: '1px'
      }
    },
    {
      xtype: 'label',
      cls: 'nx-float-left nx-interstitial-label',
      text: NX.I18n.get('ADMIN_LDAP_CONNECTION_RULES_1')
    },
    {
      xtype: 'numberfield',
      name: 'connectionTimeout',
      cls: 'nx-float-left',
      width: 70,
      value: 30
    },
    {
      xtype: 'label',
      cls: 'nx-float-left nx-interstitial-label',
      text: NX.I18n.get('ADMIN_LDAP_CONNECTION_RULES_2')
    },
    {
      xtype: 'numberfield',
      name: 'connectionRetryDelay',
      cls: 'nx-float-left',
      width: 70,
      value: 300
    },
    {
      xtype: 'label',
      cls: 'nx-float-left nx-interstitial-label',
      text: NX.I18n.get('ADMIN_LDAP_CONNECTION_RULES_3')
    },
    {
      xtype: 'numberfield',
      name: 'maxIncidentsCount',
      cls: 'nx-float-left',
      width: 55,
      value: 3
    },
    {
      xtype: 'label',
      cls: 'nx-float-left nx-interstitial-label',
      text: NX.I18n.get('ADMIN_LDAP_CONNECTION_RULES_4')
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
