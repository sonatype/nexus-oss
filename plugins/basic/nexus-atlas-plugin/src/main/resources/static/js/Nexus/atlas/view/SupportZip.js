/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

/**
 * Support ZIP panel.
 *
 * @since 2.7
 */
NX.define('Nexus.atlas.view.SupportZip', {
  extend: 'Ext.Panel',

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  xtype: 'nx-atlas-view-supportzip',
  title: 'Support ZIP',
  id: 'nx-atlas-view-supportzip',
  cls: 'nx-atlas-view-supportzip',

  border: false,
  layout: 'fit',

  /**
   * @override
   */
  initComponent: function() {
    var me = this;

    Ext.apply(me, {
      items: [
        {
          xtype: 'container',
          items: [
            {
              cls: 'nx-atlas-view-supportzip-description',
              border: false,
              html: 'Generate a ZIP file containing useful information about your server. ' +
                  'No information will be sent to Sonatype when creating the ZIP file.' +
                  '<br/><br/> Select the contents and options for generating the support ZIP file:'
            },
            {
              xtype: 'form',
              itemId: 'form',
              cls: 'nx-atlas-view-supportzip-form',
              layoutConfig: {
                labelSeparator: '',
                labelWidth: 150
              },
              border: false,
              items: [
                {
                  xtype: 'checkbox',
                  name: 'applicationProperties',
                  fieldLabel: 'Application Properties',
                  helpText: 'Includes memory and disk statistics, applications properties, system properties and environment variables'
                },
                {
                  xtype: 'checkbox',
                  name: 'threadDump',
                  fieldLabel: 'Thread Dump',
                  helpText: 'Include a thread-dump'
                },
                {
                  xtype: 'checkbox',
                  name: 'configurationFiles',
                  fieldLabel: 'Configuration Files',
                  helpText: 'Include Nexus configuration files'
                },
                {
                  xtype: 'checkbox',
                  name: 'logFiles',
                  fieldLabel: 'Log Files',
                  helpText: 'Include Nexus log files'
                },
                {
                  xtype: 'checkbox',
                  name: 'limitSize',
                  fieldLabel: 'Limit Zip File Size',
                  helpText: 'Limit the size of the generate zip to no more than 30 MB.'
                }
              ],
              buttons: [
                { text: 'Create', id: 'nx-atlas-button-create-zip' }
              ],
              buttonAlign: 'left'
            }
          ]
        }
      ]
    });

    me.constructor.superclass.initComponent.apply(me, arguments);
  }
});