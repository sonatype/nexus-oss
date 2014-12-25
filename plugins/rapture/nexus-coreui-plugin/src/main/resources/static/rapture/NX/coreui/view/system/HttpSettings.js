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
 * HTTP System Settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.system.HttpSettings', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-system-http-settings',
  requires: [
    'NX.Conditions',
    'NX.coreui.view.AuthenticationSettings',
    'NX.coreui.view.HttpRequestSettings'
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = [
      {
        xtype: 'nx-settingsform',
        settingsFormSuccessMessage: 'HTTP system settings $action',
        api: {
          load: 'NX.direct.coreui_HttpSettings.read',
          submit: 'NX.direct.coreui_HttpSettings.update'
        },
        editableCondition: NX.Conditions.isPermitted('nexus:settings', 'update'),
        editableMarker: 'You do not have permission to configure http',
        items: [
          // request settings
          {
            xtype: 'label',
            html: '<p>Nexus uses HTTP to fetch content from remote servers.  In some cases customization of HTTP request configuration may be required.</p>'
          },
          {
            xtype: 'nx-coreui-httprequestsettings'
          },
          {
            xtype: 'label',
            html: '<p>When Nexus is behind a firewall or otherwise needs to have a proxy server configured to access remote servers via HTTP proxy server configuration may be required.</p>'
          },
          {
            xtype: 'nx-optionalfieldset',
            title: 'HTTP Proxy',
            checkboxToggle: true,
            checkboxName: 'httpEnabled',
            items: [
              {
                xtype: 'label',
                html: '<p>HTTP proxy settings.</p>'
              },
              {
                xtype: 'textfield',
                name: 'httpHost',
                fieldLabel: 'Host',
                helpText: 'This is the host name of the HTTP proxy used for remote connections. (no HTTP required...just the host or ip)',
                allowBlank: false
              },
              {
                xtype: 'numberfield',
                name: 'httpPort',
                fieldLabel: 'Port',
                helpText: 'This is the port number of the HTTP proxy used for remote connections.',
                minValue: 1,
                maxValue: 65535,
                allowDecimals: false,
                allowExponential: false,
                allowBlank: false
              },
              {
                xtype: 'nx-optionalfieldset',
                title: 'Authentication',
                checkboxToggle: true,
                checkboxName: 'httpAuthEnabled',
                collapsed: true,
                items: {
                  xtype: 'nx-coreui-authenticationsettings',
                  namePrefix: 'http'
                }
              },
              {
                xtype: 'nx-valueset',
                name: 'nonProxyHosts',
                fieldLabel: 'Non Proxy Hosts',
                helpText: "List of host names to exclude from http proxy. Regular expressions are supported, e.g. '.*\\.somecompany\\.com'.",
                emptyText: 'enter a hostname',
                sorted: true
              }
            ]
          },

          {
            xtype: 'nx-optionalfieldset',
            title: 'HTTPS Proxy',
            itemId: 'httpsProxy',
            checkboxToggle: true,
            checkboxName: 'httpsEnabled',
            collapsed: true,
            items: [
              {
                xtype: 'label',
                html: '<p>HTTPS proxy settings.</p>'
              },
              {
                xtype: 'textfield',
                name: 'httpsHost',
                fieldLabel: 'Host',
                helpText: 'This is the host name of the HTTPS proxy used for remote connections. (no HTTPS required...just the host or ip)',
                allowBlank: false
              },
              {
                xtype: 'numberfield',
                name: 'httpsPort',
                fieldLabel: 'Port',
                helpText: 'This is the port number of the HTTPS proxy used for remote connections.',
                minValue: 1,
                maxValue: 65535,
                allowDecimals: false,
                allowExponential: false,
                allowBlank: false
              },
              {
                xtype: 'nx-optionalfieldset',
                title: 'Authentication',
                checkboxToggle: true,
                checkboxName: 'httpsAuthEnabled',
                collapsed: true,
                items: {
                  xtype: 'nx-coreui-authenticationsettings',
                  namePrefix: 'http'
                }
              }
            ]
          }
        ]
      }
    ];

    me.callParent();
  }
});