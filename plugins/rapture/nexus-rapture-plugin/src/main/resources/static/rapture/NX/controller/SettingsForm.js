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
 * Controls forms marked with settingsForm = true by adding save/discard/refresh functionality using form configured
 * api.
 *
 * @since 2.8
 */
Ext.define('NX.controller.SettingsForm', {
  extend: 'Ext.app.Controller',

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.onRefresh
        }
      },
      component: {
        'form[settingsForm=true]': {
          beforerender: me.loadForm
        },
        'form[settingsForm=true][editableCondition]': {
          afterrender: me.bindEditableCondition
        },
        'form[settingsForm=true] button[action=save]': {
          click: me.submitForm
        }
      }
    });
  },

  /**
   * @private
   */
  onRefresh: function () {
    var me = this,
        forms = Ext.ComponentQuery.query('form[settingsForm=true]');

    if (forms) {
      Ext.each(forms, function (form) {
        me.loadForm(form);
        if (form.settingsFormTitle) {
          NX.Messages.add({ text: form.settingsFormTitle + ' refreshed', type: 'default'});
        }
      });
    }
  },

  /**
   * @private
   */
  loadForm: function (form) {
    if (form.api && form.api.load) {
      form.load();
    }
  },

  /**
   * @private
   */
  submitForm: function (button) {
    var me = this,
        form = button.up('form');

    if (form.api && form.api.submit) {
      form.getForm().submit({
        success: function () {
          if (form.settingsFormTitle) {
            NX.Messages.add({ text: form.settingsFormTitle + ' updated', type: 'success' });
          }
          me.loadForm(form);
        }
      });
    }
  },

  /**
   * @private
   * Toggle editable on settings form hen editable condition is satisfied (if specified).
   * @param {NX.view.SettingsForm} form
   */
  bindEditableCondition: function (form) {
    if (Ext.isDefined(form.editableCondition)) {
      form.mon(
          form.editableCondition,
          {
            satisfied: function () {
              form.setEditable(true);
            },
            unsatisfied: function () {
              form.setEditable(false);
            },
            scope: form
          }
      );
    }
  }

});