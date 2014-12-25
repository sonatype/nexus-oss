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
 * Support Zip panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.support.SupportZip', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-support-supportzip',
  requires: [
    'NX.Conditions'
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = {
      xtype: 'nx-settingsform',
      settingsFormSubmitMessage: 'Creating support ZIP...',
      settingsFormSuccessMessage: 'Support ZIP created',
      api: {
        submit: 'NX.direct.atlas_SupportZip.create'
      },
      editableCondition: NX.Conditions.isPermitted('nexus:atlas', 'create'),
      editableMarker: 'You do not have permission to create a support ZIP',
      items: [
        {
          xtype: 'label',
          html: '<div>No information will be sent to Sonatype when creating the support ZIP file.' +
              '<br/><br/>Select the contents and options for support ZIP creation:<br/><br/></div>'
        },
        {
          xtype: 'checkboxgroup',
          fieldLabel: 'Contents',
          columns: 1,
          allowBlank: false,
          items: [
            {
              xtype: 'checkbox',
              name: 'systemInformation',
              boxLabel: 'Includes system information report',
              checked: true
            },
            {
              xtype: 'checkbox',
              name: 'threadDump',
              boxLabel: 'Include a JVM thread-dump',
              checked: true
            },
            {
              xtype: 'checkbox',
              name: 'configuration',
              boxLabel: 'Include configuration files',
              checked: true
            },
            {
              xtype: 'checkbox',
              name: 'security',
              boxLabel: 'Include security configuration files',
              checked: true
            },
            {
              xtype: 'checkbox',
              name: 'log',
              boxLabel: 'Include log files',
              checked: true
            },
            {
              xtype: 'checkbox',
              name: 'metrics',
              boxLabel: 'Includes system and component metrics',
              checked: true
            }
          ]
        },
        {
          xtype: 'checkboxgroup',
          fieldLabel: 'Options',
          allowBlank: true,
          columns: 1,
          items: [
            {
              xtype: 'checkbox',
              name: 'limitFileSizes',
              boxLabel: 'Limit the size of files included in the support ZIP to no more than 30 MB each.',
              checked: true
            },
            {
              xtype: 'checkbox',
              name: 'limitZipSize',
              boxLabel: 'Limit the maximum size of the support ZIP file to no more than 20 MB.',
              checked: true
            }
          ]
        }
      ],

      buttonAlign: 'left',

      buttons: [
        {
          text: 'Create',
          formBind: true,
          glyph: 'xf019@FontAwesome' /* fa-download */,
          action: 'save',
          ui: 'primary'
        }
      ]
    };

    me.callParent();
  }
});
