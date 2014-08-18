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
    allowBlank: false
  },

  items: [
    {
      name: 'name',
      helpText: 'The name of the LDAP Server.',
      itemId: 'name',
      fieldLabel: 'Name',
      emptyText: 'enter a name'
    },
    {
      xtype: 'combo',
      name: 'protocol',
      fieldLabel: 'Protocol',
      helpText: 'Use plain text (ldap://) or secure (ldaps://) connection.',
      emptyText: 'select a connection type',
      editable: false,
      store: [
        ['ldap', 'Plain connection (ldap)'],
        ['ldaps', 'Secure Connection (ldaps)']
      ],
      queryMode: 'local',
      useTrustStore: function (combo) {
        var form = combo.up('form');
        if (combo.getValue() === 'ldaps') {
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
      fieldLabel: 'Host',
      helpText: 'The host name of the LDAP server.'
    },
    {
      xtype: 'numberfield',
      name: 'port',
      itemId: 'port',
      fieldLabel: 'Port',
      helpText: 'The port the LDAP server is listening on (ldap - 389, ldaps - 636).',
      minValue: 1,
      maxValue: 65535,
      allowDecimals: false,
      allowExponential: false
    },
    {
      name: 'searchBase',
      fieldLabel: 'Search Base',
      helpText: 'LDAP location to be added to the connection URL, e.g. "dc=sonatype,dc=com".'
    },
    {
      xtype: 'combo',
      name: 'authScheme',
      fieldLabel: 'Authentication Method',
      emptyText: 'select an authentication method',
      editable: false,
      store: [
        ['simple', 'Simple Authentication'],
        ['none', 'Anonymous Authentication'],
        ['DIGEST-MD5', 'DIGEST-MD5'],
        ['CRAM-MD5', 'CRAM-MD5']
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
      fieldLabel: 'SASL Realm',
      helpText: 'The SASL realm to bind to, e.g. "mydomain.com".',
      allowBlank: true,
      authScheme: ['DIGEST-MD5', 'CRAM-MD5']
    },
    {
      name: 'authUsername',
      itemId: 'authUsername',
      fieldLabel: 'Username',
      helpText: 'The username or DN to bind with. If simple authentication is used, this has to be a fully qualified user name.',
      authScheme: ['simple', 'DIGEST-MD5', 'CRAM-MD5']
    },
    {
      xtype: 'nx-password',
      name: 'authPassword',
      itemId: 'authPassword',
      fieldLabel: 'Password',
      inputType: 'password',
      helpText: 'The password to bind with.',
      authScheme: ['simple', 'DIGEST-MD5', 'CRAM-MD5']
    },
    {
      xtype: 'numberfield',
      name: 'connectionTimeout',
      fieldLabel: 'Connection Timeout',
      helpText: 'The number of seconds to wait before timeout on connection to LDAP Server.',
      value: 30
    },
    {
      xtype: 'numberfield',
      name: 'connectionRetryDelay',
      fieldLabel: 'Retry Delay',
      helpText: 'The number of seconds to wait before retrying a request to the LDAP Server.',
      value: 300
    },
    {
      xtype: 'numberfield',
      name: 'cacheTimeout',
      fieldLabel: 'Cache Duration',
      helpText: 'The number of seconds to keep items in the cache.',
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
