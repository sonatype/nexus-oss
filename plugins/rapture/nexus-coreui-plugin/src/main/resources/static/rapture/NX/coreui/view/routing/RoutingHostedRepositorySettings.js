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
 * Routing hosted repository settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.routing.RoutingHostedRepositorySettings', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-routing-hosted-repository-settings',
  requires: [
    'NX.util.Url'
  ],

  config: {
    active: false,
    repository: undefined
  },

  items: [
    {
      xtype: 'nx-settingsform',
      paramOrder: ['repositoryId'],
      api: {
        load: 'NX.direct.coreui_RoutingRepositorySettings.read'
      },
      settingsFormSuccessMessage: 'Routing Repository Settings $action',
      buttons: undefined,
      hidden: true
    },
    {
      xtype: 'form',
      itemId: 'publishStatusForm',
      title: 'Publish Status',
      frame: true,
      hidden: true,

      bodyPadding: 10,
      margin: 10,

      items: [
        {
          xtype: 'displayfield',
          name: 'publishStatus',
          hideIfUndefined: true,
          hidden: true
        },
        {
          xtype: 'displayfield',
          name: 'publishMessage',
          hideIfUndefined: true,
          hidden: true
        },
        {
          xtype: 'nx-datedisplayfield',
          name: 'publishTimestamp',
          fieldLabel: 'Published On',
          helpText: 'Time when routing data was published.',
          hideIfUndefined: true,
          hidden: true
        },
        {
          xtype: 'displayfield',
          name: 'publishUrl',
          hideIfUndefined: true,
          hidden: true
        }
      ]
    }
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.callParent(arguments);

    Ext.override(me.down('nx-settingsform'), {
      /**
       * @override
       * Block Ext.Direct load call if we do not have a repository id.
       */
      load: function () {
        var me = this;
        if (me.getForm().baseParams.repositoryId) {
          me.callParent(arguments);
        }
      }
    });

    Ext.override(me.down('nx-settingsform').getForm(), {
      /**
       * @override
       * Show publish status when settings form is loaded.
       */
      setValues: function (values) {
        var publishStatusForm = me.down('#publishStatusForm');

        this.callParent(arguments);

        if (values && values.publishStatus) {
          if (values.publishUrl) {
            values.publishUrl = NX.util.Url.asLink(values.publishUrl, 'View prefix file');
          }
          publishStatusForm.getForm().setValues(values);
          publishStatusForm.show();
        }
        else {
          publishStatusForm.hide();
        }
      }
    });
  },

  /**
   * @private
   * Preset form base params to repository id.
   */
  applyRepository: function (repositoryModel) {
    var me = this,
        form = me.down('nx-settingsform');

    form.getForm().baseParams = {
      repositoryId: repositoryModel ? repositoryModel.getId() : undefined
    };

    return repositoryModel;
  }

})
;
