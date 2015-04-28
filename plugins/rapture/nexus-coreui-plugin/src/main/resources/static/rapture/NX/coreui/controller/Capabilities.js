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
 * Capabilities controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Capabilities', {
  extend: 'NX.controller.Drilldown',
  requires: [
    'NX.Conditions',
    'NX.Dialogs',
    'NX.Messages',
    'NX.Permissions',
    'NX.I18n'
  ],

  masters: 'nx-coreui-capability-list',

  stores: [
    'Capability',
    'CapabilityType'
  ],
  models: [
    'Capability'
  ],
  views: [
    'capability.CapabilityAdd',
    'capability.CapabilityFeature',
    'capability.CapabilityList',
    'capability.CapabilitySummary',
    'capability.CapabilitySelectType',
    'capability.CapabilitySettings',
    'capability.CapabilitySettingsForm',
    'capability.CapabilityStatus',
    'capability.CapabilityAbout',
    'formfield.SettingsFieldSet'
  ],
  refs: [
    { ref: 'feature', selector: 'nx-coreui-capability-feature' },
    { ref: 'content', selector: 'nx-feature-content' },
    { ref: 'list', selector: 'nx-coreui-capability-list' },
    { ref: 'summaryTab', selector: 'nx-coreui-capability-summary' },
    { ref: 'settingsTab', selector: 'nx-coreui-capability-settings' },
    { ref: 'summaryPanel', selector: '#nx-coreui-capability-summary-subsection' },
    { ref: 'statusPanel', selector: 'nx-coreui-capability-status' },
    { ref: 'aboutPanel', selector: 'nx-coreui-capability-about' },
    { ref: 'notesPanel', selector: '#nx-coreui-capability-notes-subsection' },
    { ref: 'settingsPanel', selector: 'nx-coreui-capability-settings-form' }
  ],
  icons: {
    'capability-default': {
      file: 'brick.png',
      variants: ['x16', 'x32']
    },
    'capability-active': {
      file: 'brick_valid.png',
      variants: ['x16', 'x32']
    },
    'capability-disabled': {
      file: 'brick_grey.png',
      variants: ['x16', 'x32']
    },
    'capability-error': {
      file: 'brick_error.png',
      variants: ['x16', 'x32']
    },
    'capability-passive': {
      file: 'brick_error.png',
      variants: ['x16', 'x32']
    }
  },
  features: {
    mode: 'admin',
    path: '/System/Capabilities',
    text: NX.I18n.get('ADMIN_CAPABILITIES_TITLE'),
    description: NX.I18n.get('ADMIN_CAPABILITIES_SUBTITLE'),
    view: { xtype: 'nx-coreui-capability-feature' },
    iconConfig: {
      file: 'brick.png',
      variants: ['x16', 'x32']
    },
    visible: function() {
      return NX.Permissions.check('nexus:capabilities', 'read');
    }
  },
  permission: 'nexus:capabilities',

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.callParent();

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.onRefresh
        }
      },
      store: {
        '#Capability': {
          load: me.onCapabilityLoad
        }
      },
      component: {
        'nx-coreui-capability-list': {
          beforerender: me.onRefresh
        },
        'nx-coreui-capability-list button[action=new]': {
          click: me.showSelectTypePanel
        },
        'nx-coreui-capability-feature button[action=enable]': {
          runaction: me.enableCapability,
          afterrender: me.bindEnableButton
        },
        'nx-coreui-capability-feature button[action=disable]': {
          runaction: me.disableCapability,
          afterrender: me.bindDisableButton
        },
        'nx-coreui-capability-summary nx-settingsform': {
          submitted: function() {
            me.loadStore(Ext.emptyFn)
          }
        },
        'nx-coreui-capability-settings button[action=save]': {
          click: me.updateCapability
        },
        'nx-coreui-capability-add button[action=add]': {
          click: me.createCapability
        },
        'nx-coreui-capability-selecttype': {
          cellclick: me.showAddPanel
        }
      }
    });
  },

  /**
   * @override
   * Returns a description of capability suitable to be displayed.
   * @param {NX.coreui.model.Capability} model selected model
   */
  getDescription: function(model) {
    var description = model.get('typeName');
    if (model.get('description')) {
      description += ' - ' + model.get('description');
    }
    return description;
  },

  /**
   * @override
   * Load capability model into detail tabs.
   * @param {NX.coreui.view.capability.CapabilityList} list capability grid
   * @param {NX.coreui.model.Capability} model selected model
   */
  onSelection: function(list, model) {
    var me = this,
        capabilityTypeModel;

    if (Ext.isDefined(model)) {
      me.getFeature().setItemClass(1, NX.Icons.cls('capability-' + model.get('state'), 'x16'));

      capabilityTypeModel = me.getCapabilityTypeStore().getById(model.get('typeId'));

      me.eventuallyShowWarning(model);
      me.showSummary(model);
      me.showSettings(model);
      me.showStatus(model);
      me.showAbout(capabilityTypeModel);
    }
  },

  /**
   * @private
   * Displays a warning message if capability is enabled but is not active.
   * @param {NX.coreui.model.Capability} model capability model
   */
  eventuallyShowWarning: function(model) {
    var drilldown = this.getList().up('nx-drilldown');

    if (model.get('enabled') && !model.get('active')) {
      drilldown.showWarning(model.get('stateDescription'));
    }
    else {
      drilldown.clearWarning();
    }
  },

  /**
   * @private
   * Displays capability summary.
   * @param {NX.coreui.model.Capability} model capability model
   */
  showSummary: function(model) {
    var summary = this.getSummaryTab(),
        info = {};

    info[NX.I18n.get('ADMIN_CAPABILITIES_SUMMARY_TYPE')] = model.get('typeName');
    info[NX.I18n.get('ADMIN_CAPABILITIES_SUMMARY_DESCRIPTION')] = model.get('description');
    info[NX.I18n.get('ADMIN_CAPABILITIES_SUMMARY_STATE')] = Ext.String.capitalize(model.get('state'));

    if (Ext.isDefined(model.get('tags'))) {
      Ext.apply(info, model.get('tags'));
    }

    summary.showInfo(info);
    summary.down('form').loadRecord(model);
  },

  /**
   * @private
   * Displays capability settings.
   * @param {NX.coreui.model.Capability} model capability model
   */
  showSettings: function(model) {
    this.getSettingsTab().loadRecord(model);
  },

  /**
   * @private
   * Displays capability status.
   * @param {NX.coreui.model.Capability} model capability model
   */
  showStatus: function(model) {
    this.getStatusPanel().showStatus(model.get('status'));
  },

  /**
   * @private
   * Displays capability about.
   * @param {NX.coreui.model.CapabilityType} capabilityTypeModel capability type model
   */
  showAbout: function(capabilityTypeModel) {
    this.getAboutPanel().showAbout(capabilityTypeModel ? capabilityTypeModel.get('about') : undefined);
  },

  /**
   * @private
   */
  showSelectTypePanel: function() {
    var me = this,
        feature = me.getFeature();

    // Show the first panel in the create wizard, and set the breadcrumb
    feature.setItemName(1, NX.I18n.get('ADMIN_CAPABILITIES_SELECT_TITLE'));
    me.loadCreateWizard(1, true, Ext.widget({
      xtype: 'panel',
      layout: {
        type: 'vbox',
        align: 'stretch',
        pack: 'start'
      },
      items: [
        { xtype: 'nx-actions' },
        {
          xtype: 'nx-coreui-capability-selecttype',
          flex: 1
        }
      ]
    }));
  },

  /**
   * @private
   */
  showAddPanel: function(list, td, cellIndex, model) {
    var me = this,
        feature = me.getFeature(),
        panel;

    // Show the first panel in the create wizard, and set the breadcrumb
    feature.setItemName(2, NX.I18n.format('ADMIN_CAPABILITIES_CREATE_TITLE', model.get('name')));
    me.loadCreateWizard(2, true, panel = Ext.create('widget.nx-coreui-capability-add'));
    var m = me.getCapabilityModel().create({ typeId: model.getId(), enabled: true });
    panel.down('nx-settingsform').loadRecord(m);
  },

  /**
   * @private
   * (Re)load capability type store && reset all cached combo stores.
   */
  onRefresh: function() {
    var me = this,
        list = me.getList();

    if (list) {
      me.getCapabilityTypeStore().load(
          function() {
            me.reselect();
          }
      );
    }
  },

  /**
   * @private
   * Synchronize tags from loaded capabilities with capability/model and grid columns.
   * @param {NX.coreui.store.Capability} store capability store
   */
  onCapabilityLoad: function(store) {
    var me = this,
        list = me.getList(),
        tagTypes = [],
        capabilityModel = me.getCapabilityModel(),
        fields = capabilityModel.prototype.fields.getRange(),
        columns = Ext.Array.clone(list.originalColumns),
        tagColumns = [];

    if (list) {
      store.each(function(model) {
        Ext.Object.each(model.get('tags'), function(key) {
          if (tagTypes.indexOf(key) === -1) {
            tagTypes.push(key);
          }
        });
      });
      Ext.Array.sort(tagTypes);
      if (!Ext.Array.equals(tagTypes, capabilityModel.tagTypes || [])) {
        Ext.Array.each(tagTypes, function(entry) {
          fields.push({
            name: 'tag$' + entry
          });
          tagColumns.push({
            text: entry,
            dataIndex: 'tag$' + entry,
            flex: 1
          });
        });
        capabilityModel.setFields(fields);
        capabilityModel.tagTypes = tagTypes;
        Ext.Array.insert(columns, 2, tagColumns);
        list.reconfigure(store, columns);
      }
      store.each(function(model) {
        Ext.Array.each(tagTypes, function(entry) {
          var tags = model.get('tags') || {};
          model.set('tag$' + entry, tags[entry] || '');
        });
      });
      store.commitChanges();
    }
  },

  /**
   * @override
   * @protected
   * Enable 'New' button when user has 'create' permission and there is at least one capability type.
   */
  bindNewButton: function(button) {
    var me = this;
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted(me.permission, 'create'),
            NX.Conditions.storeHasRecords('CapabilityType')
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  /**
   * @private
   * Enable 'Enable' button when user has 'update' permission and capability is not enabled.
   */
  bindEnableButton: function(button) {
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted('nexus:capabilities', 'update'),
            NX.Conditions.gridHasSelection('nx-coreui-capability-list', function(model) {
              return !model.get('enabled');
            })
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  /**
   * @private
   * Enable 'Disable' button when user has 'update' permission and capability is enabled.
   */
  bindDisableButton: function(button) {
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted('nexus:capabilities', 'update'),
            NX.Conditions.gridHasSelection('nx-coreui-capability-list', function(model) {
              return model.get('enabled');
            })
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  /**
   * @private
   * Creates a capability.
   */
  createCapability: function(button) {
    var me = this,
        form = button.up('form'),
        values = form.getValues();

    NX.direct.capability_Capability.create(values, function(response) {
      if (Ext.isObject(response)) {
        if (response.success) {
          NX.Messages.add({
            text: NX.I18n.format('ADMIN_CAPABILITIES_CREATE_SUCCESS',
                me.getDescription(me.getCapabilityModel().create(response.data))),
            type: 'success'
          });
          me.loadStoreAndSelect(response.data.id, false);
        }
        else if (Ext.isDefined(response.errors)) {
          form.markInvalid(response.errors);
        }
      }
    });
  },

  /**
   * @private
   * Updates capability.
   */
  updateCapability: function(button) {
    var me = this,
        form = button.up('form'),
        values = form.getValues();

    me.getContent().getEl().mask(NX.I18n.get('ADMIN_CAPABILITIES_UPDATE_MASK'));
    NX.direct.capability_Capability.update(values, function(response) {
      me.getContent().getEl().unmask();
      if (Ext.isObject(response)) {
        if (response.success) {
          NX.Messages.add({
            text: NX.I18n.format('ADMIN_CAPABILITIES_UPDATE_SUCCESS',
                me.getDescription(me.getCapabilityModel().create(response.data))),
            type: 'success'
          });
          me.loadStore(Ext.emptyFn);
        }
        else if (Ext.isDefined(response.errors)) {
          form.markInvalid(response.errors);
        }
      }
    });
  },

  /**
   * @override
   * Delete capability.
   * @param {NX.coreui.model.CapabilityType} model capability to be deleted
   */
  deleteModel: function(model) {
    var me = this,
        description = me.getDescription(model);

    NX.direct.capability_Capability.remove(model.getId(), function(response) {
      me.loadStore();
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({
          text: NX.I18n.format('ADMIN_CAPABILITIES_DELETE_SUCCESS', description),
          type: 'success'
        });
      }
    });
  },

  /**
   * @private
   * Enables selected capability.
   */
  enableCapability: function() {
    var me = this,
        bookmark = NX.Bookmarks.getBookmark(),
        model, modelId, description;

    modelId = decodeURIComponent(bookmark.getSegment(1));
    model = me.getList().getStore().getById(modelId);
    description = me.getDescription(model);

    me.getContent().getEl().mask(NX.I18n.get('ADMIN_CAPABILITIES_ENABLE_MASK'));
    NX.direct.capability_Capability.enable(model.getId(), function(response) {
      me.loadStore();
      me.getContent().getEl().unmask();
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({
          text: NX.I18n.format('ADMIN_CAPABILITIES_DETAILS_ENABLE_SUCCESS', description),
          type: 'success'
        });
      }
    });
  },

  /**
   * @private
   * Disables selected capability.
   */
  disableCapability: function() {
    var me = this,
        bookmark = NX.Bookmarks.getBookmark(),
        model, modelId, description;

    modelId = decodeURIComponent(bookmark.getSegment(1));
    model = me.getList().getStore().getById(modelId);
    description = me.getDescription(model);

    me.getContent().getEl().mask(NX.I18n.get('ADMIN_CAPABILITIES_DISABLE_MASK'));
    NX.direct.capability_Capability.disable(model.getId(), function(response) {
      me.loadStore();
      me.getContent().getEl().unmask();
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({
          text: NX.I18n.format('ADMIN_CAPABILITIES_DETAILS_DISABLE_SUCCESS', description),
          type: 'success'
        });
      }
    });
  }
});
