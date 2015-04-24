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
 * Configuration specific to Http connections for repositories.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repository.facet.HttpFacet', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-coreui-repository-http-facet',
  requires: [
    'NX.I18n'
  ],
  /**
   * @override
   */
  initComponent: function() {
    var me = this;

    me.updateAuthenticationFields = function(combo, record) {
      var me = this, ntlmFields = me.up('form').down('#ntlmFields');
      if(combo.getValue() === 'ntlm') {
        ntlmFields.show();  
      }
      else {
        ntlmFields.hide();
      }
    };
    me.items = [
      {
        xtype: 'nx-optionalfieldset',
        title: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_AUTHENTICATION'),
        checkboxToggle: true,
        checkboxName: 'authEnabled',
        collapsed: true,
        items: [
          {
            xtype: 'combo',
            name: 'attributes.httpclient.authentication.type',
            fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_AUTHENTICATION_TYPE'),
            store: [
              ['username', NX.I18n.get('ADMIN_REPOSITORIES_AUTHENTICATION_TYPE_USERNAME')],
              ['ntlm', NX.I18n.get('ADMIN_REPOSITORIES_AUTHENTICATION_TYPE_NTLM')]
            ],
            value: 'username' ,
            listeners: {
              'select': me.updateAuthenticationFields,
              'beforerender': me.updateAuthenticationFields
              }
          },
          {
            xtype:'textfield',
            name: 'attributes.httpclient.authentication.username',
            fieldLabel: NX.I18n.get('ADMIN_AUTHENTICATION_USERNAME'),
            allowBlank: false
          },
          {
            xtype: 'nx-password',
            name: 'attributes.httpclient.authentication.password',
            fieldLabel: NX.I18n.get('ADMIN_AUTHENTICATION_PASSWORD')
          },
          {
            xtype: 'fieldcontainer',
            itemId: 'ntlmFields',
            hidden: true,
            items:[
              {
                xtype:'textfield',
                name: 'attributes.httpclient.authentication.ntlmHost',
                fieldLabel: NX.I18n.get('ADMIN_AUTHENTICATION_HOST')
              },
              {
                xtype:'textfield',
                name: 'attributes.httpclient.authentication.ntlmDomain',
                fieldLabel: NX.I18n.get('ADMIN_AUTHENTICATION_DOMAIN')
              }
            ]
          }
        ]
      },
      {
        xtype: 'nx-optionalfieldset',
        title: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_HTTP'),
        checkboxToggle: true,
        checkboxName: 'httpRequestSettings',
        collapsed: true,
        items: [
          {
            xtype: 'textfield',
            name: 'attributes.httpclient.connection.userAgentCustomisation',
            fieldLabel: NX.I18n.get('ADMIN_HTTP_CUSTOMIZATION'),
            helpText: NX.I18n.get('ADMIN_HTTP_CUSTOMIZATION_HELP')
          },
          {
            xtype: 'textfield',
            name: 'attributes.httpclient.connection.urlParameters',
            fieldLabel: NX.I18n.get('ADMIN_HTTP_PARAMETERS'),
            helpText: NX.I18n.get('ADMIN_HTTP_PARAMETERS_HELP')
          },
          {
            xtype: 'numberfield',
            name: 'attributes.httpclient.connection.retries',
            fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_CONNECTION_RETRIES'),
            helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_CONNECTION_RETRIES_HELP'),
            allowDecimals: false,
            allowExponential: false,
            minValue: 0,
            maxValue: 10
          },
          {
            xtype: 'numberfield',
            name: 'attributes.httpclient.connection.timeout',
            fieldLabel: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_CONNECTION_TIMEOUT'),
            helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_CONNECTION_TIMEOUT_HELP'),
            allowDecimals: false,
            allowExponential: false,
            minValue: 0,
            maxValue: 3600
          }
        ]
      }
    ];

    me.callParent(arguments);
  }

});
