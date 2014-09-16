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
 * NuGet repository settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.nuget.NuGetRepositorySettings', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-nuget-repository-settings',
  requires: [
    'NX.Conditions',
    'NX.util.Url'
  ],

  config: {
    active: false,
    repository: undefined
  },

  /**
   * @override
   */
  initComponent: function() {
    var me = this;

    me.items = [
      {
        xtype: 'form',
        margin: 10,
        items: [
          {
            xtype: 'textfield',
            name: 'packageSource',
            itemId: 'packageSource',
            fieldLabel: 'Package Source',
            helpText: 'You can register this source with the following command:',
            readOnly: true,
            submitValue: false,
            allowBlank: true,
            selectOnFocus: true
          }
        ]
      },
      {
        xtype: 'form',
        title: 'Access',
        frame: true,

        bodyPadding: 10,
        margin: 10,

        items: [
          {
            xtype: 'label',
            html: '<p>A new user token will be created the first time it is accessed.</p>'
          }
        ],

        buttonAlign: 'left',
        buttons: [
          { text: 'Access API Key', action: 'access', glyph: 'xf023@FontAwesome' /* fa-lock */, disabled: true }
        ]
      },
      {
        xtype: 'form',
        title: 'Reset',
        frame: true,

        bodyPadding: 10,
        margin: 10,

        items: [
          {
            xtype: 'label',
            html: '<p>Resetting your API Key will invalidate the current key.</p>'
          }
        ],

        buttonAlign: 'left',
        buttons: [
          { text: 'Reset API Key', action: 'reset', ui: 'danger', glyph: 'xf023@FontAwesome' /* fa-lock */, disabled: true }
        ]
      }
    ]

    me.callParent(arguments);
  },

  /**
   * @private
   * Sets value of package source.
   */
  applyRepository: function(repositoryModel) {
    var me = this,
        packageSource = me.down('#packageSource'),
        url = NX.util.Url.urlOf('service/local/nuget/' + repositoryModel.getId() + '/');

    packageSource.setValue('nuget sources add -name ' + repositoryModel.getId() + ' -source ' + url);

    return repositoryModel;
  }

});
