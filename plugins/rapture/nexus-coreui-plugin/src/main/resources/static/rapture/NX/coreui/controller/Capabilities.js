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
 * Capabilities controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Capabilities', {
  extend: 'NX.controller.MasterDetail',
  requires: [
    'NX.Dialogs'
  ],

  list: 'nx-coreui-capability-list',

  stores: [
    'Capability',
    'CapabilityType'
  ],
  models: [
    'Capability'
  ],
  views: [
    'capability.CapabilityFeature',
    'capability.CapabilityAdd',
    'capability.CapabilityList',
    'capability.CapabilitySummary',
    'capability.CapabilitySettings',
    'capability.CapabilityStatus',
    'capability.CapabilityAbout',
    'formfield.SettingsFieldSet'
  ],
  refs: [
    { ref: 'feature', selector: 'nx-coreui-capability-feature' },
    { ref: 'list', selector: 'nx-coreui-capability-list' },
    { ref: 'summary', selector: 'nx-coreui-capability-summary' },
    { ref: 'settings', selector: 'nx-coreui-capability-settings' },
    { ref: 'status', selector: 'nx-coreui-capability-status' },
    { ref: 'about', selector: 'nx-coreui-capability-about' }
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
    description: 'Manage capabilities',
    view: { xtype: 'nx-coreui-capability-feature' },
    iconConfig: {
      file: 'brick.png',
      variants: ['x16', 'x32']
    },
    visible: function () {
      return NX.Permissions.check('nexus:capabilities', 'read');
    }
  },
  permission: 'nexus:capabilities',

  /**
   * @override
   */
  init: function () {
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
        },
        '#CapabilityType': {
          load: me.onCapabilityTypeLoad,
          datachanged: me.onCapabilityTypeLoad
        }
      },
      component: {
        'nx-coreui-capability-list': {
          beforerender: me.onRefresh
        },
        'nx-coreui-capability-list button[action=new]': {
          click: me.showAddWindow
        },
        'nx-coreui-capability-list button[action=enable]': {
          click: me.enableCapability,
          afterrender: me.bindEnableButton
        },
        'nx-coreui-capability-list button[action=disable]': {
          click: me.disableCapability,
          afterrender: me.bindDisableButton
        },
        'nx-coreui-capability-summary nx-settingsform': {
          submitted: me.loadStore
        },
        'nx-coreui-capability-settings button[action=save]': {
          click: me.updateCapability
        },
        'nx-coreui-capability-add combo[name=typeId]': {
          select: me.changeCapabilityType
        },
        'nx-coreui-capability-add button[action=add]': {
          click: me.createCapability
        }
      }
    });
  },

  /**
   * @override
   * Returns a description of capability suitable to be displayed.
   * @param {NX.coreui.model.Capability} model selected model
   */
  getDescription: function (model) {
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
  onSelection: function (list, model) {
    var me = this,
        capabilityTypeModel;

    if (Ext.isDefined(model)) {
      me.getFeature().setDescriptionIconName('capability-' + model.get('state'));

      capabilityTypeModel = me.getCapabilityTypeStore().getById(model.get('typeId'));

      me.eventuallyShowWarning(model);
      me.showSummary(model);
      me.showSettings(model);
      me.showAbout(capabilityTypeModel);
      me.showStatus(model);
    }
  },

  /**
   * @private
   * Displays a warning message if capability is enabled but is not active.
   * @param {NX.coreui.model.Capability} model capability model
   */
  eventuallyShowWarning: function (model) {
    var masterdetail = this.getList().up('nx-masterdetail-panel');

    if (model.get('enabled') && !model.get('active')) {
      masterdetail.showWarning(model.get('stateDescription'));
    }
    else {
      masterdetail.clearWarning();
    }
  },

  /**
   * @private
   * Displays capability summary.
   * @param {NX.coreui.model.Capability} model capability model
   */
  showSummary: function (model) {
    var summary = this.getSummary(),
        info = {
          'Type': model.get('typeName'),
          'Description': model.get('description'),
          'State': Ext.String.capitalize(model.get('state'))
        };

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
  showSettings: function (model) {
    this.getSettings().loadRecord(model);
  },

  /**
   * @private
   * Displays capability status.
   * @param {NX.coreui.model.Capability} model capability model
   */
  showStatus: function (model) {
    this.getStatus().showStatus(model.get('status'));
  },

  /**
   * @private
   * Displays capability about.
   * @param {NX.coreui.model.CapabilityType} capabilityTypeModel capability type model
   */
  showAbout: function (capabilityTypeModel) {
    this.getAbout().showAbout(capabilityTypeModel ? capabilityTypeModel.get('about') : undefined);
  },

  /**
   * @private
   */
  showAddWindow: function () {
    Ext.widget('nx-coreui-capability-add', {
      capabilityTypeStore: this.getCapabilityTypeStore()
    });
  },

  /**
   * @private
   * Change about text and settings according to selected capability type (in add window).
   * @combo {Ext.form.field.ComboBox} combobox capability type combobox
   */
  changeCapabilityType: function (combobox) {
    var win = combobox.up('window'),
        capabilityTypeModel;

    capabilityTypeModel = this.getCapabilityTypeStore().getById(combobox.value);
    win.down('nx-coreui-capability-about').showAbout(capabilityTypeModel.get('about'));
    win.down('nx-coreui-formfield-settingsfieldset').setFormFields(capabilityTypeModel.get('formFields'));
  },

  /**
   * @private
   * (Re)load capability type store && reset all cached combo stores.
   */
  onRefresh: function () {
    var me = this,
        list = me.getList();

    if (list) {
      me.getCapabilityTypeStore().load();
    }
  },

  /**
   * @private
   * When capability type store re-loads, reselect current selected capability if any.
   */
  onCapabilityTypeLoad: function () {
    var me = this;
    me.reselect();
  },

  /**
   * @private
   * Synchronize tags from loaded capabilities with capability/model and grid columns.
   * @param {NX.coreui.store.Capability} store capability store
   */
  onCapabilityLoad: function (store) {
    var me = this,
        list = me.getList(),
        tagTypes = [],
        capabilityModel = me.getCapabilityModel(),
        fields = capabilityModel.prototype.fields.getRange(),
        columns = Ext.Array.clone(list.originalColumns),
        tagColumns = [];

    if (list) {
      store.each(function (model) {
        Ext.Object.each(model.get('tags'), function (key) {
          if (tagTypes.indexOf(key) === -1) {
            tagTypes.push(key);
          }
        });
      });
      Ext.Array.sort(tagTypes);
      if (!Ext.Array.equals(tagTypes, capabilityModel.tagTypes || [])) {
        Ext.Array.each(tagTypes, function (entry) {
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
      store.each(function (model) {
        Ext.Array.each(tagTypes, function (entry) {
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
  bindNewButton: function (button) {
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
  bindEnableButton: function (button) {
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted('nexus:capabilities', 'update'),
            NX.Conditions.gridHasSelection('nx-coreui-capability-list', function (model) {
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
  bindDisableButton: function (button) {
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted('nexus:capabilities', 'update'),
            NX.Conditions.gridHasSelection('nx-coreui-capability-list', function (model) {
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
  createCapability: function (button) {
    var me = this,
        win = button.up('window'),
        form = button.up('form'),
        values = form.getValues();

    NX.direct.capability_Capability.create(values, function (response) {
      if (Ext.isObject(response)) {
        if (response.success) {
          win.close();
          NX.Messages.add({
            text: 'Capability created: ' + me.getDescription(me.getCapabilityModel().create(response.data)),
            type: 'success'
          });
          me.loadStoreAndSelect(response.data.id);
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
  updateCapability: function (button) {
    var me = this,
        form = button.up('form'),
        values = form.getValues();

    NX.direct.capability_Capability.update(values, function (response) {
      if (Ext.isObject(response)) {
        if (response.success) {
          NX.Messages.add({
            text: 'Capability updated: ' + me.getDescription(me.getCapabilityModel().create(response.data)),
            type: 'success'
          });
          me.loadStore();
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
  deleteModel: function (model) {
    var me = this,
        description = me.getDescription(model);

    NX.direct.capability_Capability.delete_(model.getId(), function (response) {
      me.loadStore();
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({
          text: 'Capability deleted: ' + description, type: 'success'
        });
      }
    });
  },

  /**
   * @private
   * Enables selected capability.
   */
  enableCapability: function (button) {
    var me = this,
        list = button.up('grid'),
        model = list.getSelectionModel().getSelection()[0],
        description = me.getDescription(model);

    NX.direct.capability_Capability.enable(model.getId(), function (response) {
      me.loadStore();
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({
          text: 'Capability enabled: ' + description, type: 'success'
        });
      }
    });
  },

  /**
   * @private
   * Disables selected capability.
   */
  disableCapability: function (button) {
    var me = this,
        list = button.up('grid'),
        model = list.getSelectionModel().getSelection()[0],
        description = me.getDescription(model);

    NX.direct.capability_Capability.disable(model.getId(), function (response) {
      me.loadStore();
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({
          text: 'Capability disabled: ' + description, type: 'success'
        });
      }
    });
  }

});