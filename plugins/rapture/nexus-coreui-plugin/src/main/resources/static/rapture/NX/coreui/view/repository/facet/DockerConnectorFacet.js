/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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
 * @since 3.0
 */
Ext.define('NX.coreui.view.repository.facet.DockerConnectorFacet', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-coreui-repository-docker-connector-facet',
  requires: [
    'NX.I18n'
  ],

  /**
   * @override
   */
  initComponent: function() {
    var me = this;

    me.items = [
      {
        xtype: 'fieldset',
        cls: 'nx-form-section',
        title: NX.I18n.get('Repository_Facet_DockerConnectorFacet_Title'),

        items: [
          {
            xtype: 'fieldcontainer',
            fieldLabel: NX.I18n.get('Repository_Facet_DockerConnectorFacet_HttpPort_FieldLabel'),
            helpText: NX.I18n.get('Repository_Facet_DockerConnectorFacet_HttpPort_HelpText'),
            layout: 'hbox',
            items: [
              {
                xtype: 'checkbox',
                itemId: 'httpEnabled',
                listeners: {
                  /**
                   * Enable/Disable the port.
                   */
                  change: function() {
                    var form = this.up('form'),
                        port = form.down('#httpPort');
                    if (this.getValue()) {
                      port.enable();
                    }
                    else {
                      port.disable();
                    }
                    form.isValid();
                  }
                }
              },
              {
                xtype: 'numberfield',
                name: 'attributes.docker.httpPort',
                itemId: 'httpPort',
                minValue: 1,
                maxValue: 65536,
                allowDecimals: false,
                allowExponential: false,
                allowBlank: false,
                itemCls: 'required-field',
                disabled: true,
                width: 585,
                style: {
                  marginLeft: '5px'
                },
                listeners: {
                  /**
                   * Check the checkbox if port has value.
                   */
                  change: function() {
                    var checkbox = this.up('form').down('#httpEnabled');
                    if (this.getValue() && !checkbox.getValue()) {
                      checkbox.setValue(true);
                    }
                  }
                }
              }
            ]
          },
          {
            xtype: 'fieldcontainer',
            fieldLabel: NX.I18n.get('Repository_Facet_DockerConnectorFacet_HttpsPort_FieldLabel'),
            helpText: NX.I18n.get('Repository_Facet_DockerConnectorFacet_HttpsPort_HelpText'),
            layout: 'hbox',
            items: [
              {
                xtype: 'checkbox',
                itemId: 'httpsEnabled',
                listeners: {
                  /**
                   * Enable/Disable the port.
                   */
                  change: function() {
                    var form = this.up('form'),
                        port = form.down('#httpsPort');
                    if (this.getValue()) {
                      port.enable();
                    }
                    else {
                      port.disable();
                    }
                    form.isValid();
                  }
                }
              },
              {
                xtype: 'numberfield',
                name: 'attributes.docker.httpsPort',
                itemId: 'httpsPort',
                minValue: 1,
                maxValue: 65536,
                allowDecimals: false,
                allowExponential: false,
                allowBlank: false,
                itemCls: 'required-field',
                disabled: true,
                width: 585,
                style: {
                  marginLeft: '5px'
                },
                listeners: {
                  /**
                   * Check the checkbox if port has value.
                   */
                  change: function() {
                    var checkbox = this.up('form').down('#httpsEnabled');
                    if (this.getValue() && !checkbox.getValue()) {
                      checkbox.setValue(true);
                    }
                  }
                }
              }
            ]
          }
        ]
      }
    ];

    me.callParent(arguments);
  }

});
