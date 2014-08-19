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
 * Routing proxy repository settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.routing.RoutingProxyRepositorySettings', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-routing-proxy-repository-settings',
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
  initComponent: function () {
    var me = this;

    me.items = [
      {
        xtype: 'nx-settingsform',
        paramOrder: ['repositoryId'],
        api: {
          load: 'NX.direct.coreui_RoutingRepositorySettings.read',
          submit: 'NX.direct.coreui_RoutingRepositorySettings.update'
        },
        settingsFormSuccessMessage: 'Routing Repository Settings $action',
        editableCondition: NX.Conditions.isPermitted('nexus:repositories', 'update'),
        editableMarker: 'You do not have permission to update routing repository settings',

        items: [
          {
            xtype: 'hiddenfield',
            name: 'repositoryId'
          },
          {
            xtype: 'checkbox',
            name: 'discoveryEnabled',
            itemId: 'discoveryEnabled',
            fieldLabel: 'Enable discovery',
            helpText: 'Enable routing discovery.',
            value: true
          },
          {
            xtype: 'combo',
            name: 'discoveryInterval',
            itemId: 'discoveryInterval',
            fieldLabel: 'Update interval',
            helpText: 'Interval between routing discovery runs.',
            emptyText: 'Select...',
            editable: false,
            value: 24,
            store: [
              [1, '1 hr'],
              [2, '2 hr'],
              [3, '3 hr'],
              [6, '6 hr'],
              [9, '9 hr'],
              [12, '12 hr'],
              [24, 'Daily'],
              [168, 'Weekly']
            ],
            queryMode: 'local'
          }
        ]
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
      },
      {
        xtype: 'form',
        itemId: 'discoveryStatusForm',
        title: 'Discovery Status',
        frame: true,
        hidden: true,

        bodyPadding: 10,
        margin: 10,

        items: [
          {
            xtype: 'displayfield',
            name: 'discoveryStatus',
            hideIfUndefined: true,
            hidden: true
          },
          {
            xtype: 'displayfield',
            name: 'discoveryMessage',
            hideIfUndefined: true,
            hidden: true
          },
          {
            xtype: 'nx-datedisplayfield',
            name: 'discoveryTimestamp',
            fieldLabel: 'Last Run',
            helpText: 'Time when discovery process was last run.',
            hideIfUndefined: true,
            hidden: true
          }
        ]
      }
    ];

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
       * Show publish/discovery status when settings form is loaded.
       */
      setValues: function (values) {
        var publishStatusForm = me.down('#publishStatusForm'),
            discoveryStatusForm = me.down('#discoveryStatusForm'),
            updateNowButton = me.down('nx-settingsform button[action=update]');

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

        if (values && values.discoveryStatus) {
          discoveryStatusForm.getForm().setValues(values);
          discoveryStatusForm.show();
        }
        else {
          discoveryStatusForm.hide();
        }

        if (values && values.discoveryEnabled) {
          updateNowButton.show();
        }
        else {
          updateNowButton.hide();
        }
      }
    });

    me.items.get(0).getDockedItems('toolbar[dock="bottom"]')[0].add({
      xtype: 'button', text: 'Update now', formBind: true, action: 'update', hidden: true
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
