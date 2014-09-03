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
 * File upload controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.UploadFile', {
  extend: 'Ext.app.Controller',

  views: [
    'upload.UploadFile',
    'upload.UploadFileEntry'
  ],

  /**
   * @private
   */
  counter: 0,

  fileEntryPanelXType: 'nx-coreui-upload-file-entry',

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.listen({
      component: {
        'nx-coreui-upload-file': {
          afterrender: me.afterRender
        },
        'nx-coreui-upload-file button[action=upload]': {
          click: me.upload
        },
        'nx-coreui-upload-file button[action=discard]': {
          click: me.discard
        },
        'nx-coreui-upload-file button[action=add]': {
          click: me.addArtifact
        },
        'nx-coreui-upload-file-entry button[action=delete]': {
          click: me.removeArtifact
        }
      }
    });
  },

  /**
   * @private
   */
  afterRender: function(form) {
    var me = this;

    me.refreshAddButton(form);
    form.isValid();
  },

  /**
   * @private
   * Uploads artifacts by submitting the form.
   */
  upload: function(button) {
    var me = this,
        form = button.up('form');

    form.submit({
      waitMsg: 'Uploading your files...',
      success: function() {
        NX.Messages.add({ text: Ext.String.capitalize(form.entryName) + 's uploaded', type: 'success' });
        me.discardForm(form);
      }
    });
  },

  /**
   * @private
   */
  discard: function(button) {
    var me = this;
    me.discardForm(button.up('form'));
  },

  /**
   * @private
   * Resets form to initial state.
   */
  discardForm: function(form) {
    var me = this,
        artifactPanel;

    me.counter = 0;
    form.getForm().reset();
    artifactPanel = form.down(me.fileEntryPanelXType);
    while (artifactPanel) {
      form.remove(artifactPanel);
      artifactPanel = form.down(me.fileEntryPanelXType);
    }
    me.refreshAddButton(form);
  },

  /**
   * @private
   * Add an artifact selection panel.
   */
  addArtifact: function(button) {
    var me = this,
        form = button.up('form'),
        name = 'a.' + me.counter++;

    form.add({ xtype: me.fileEntryPanelXType, name: name, entryName: form.entryName });

    // HACK: avoid 'Access Denied' in IE which does not like the fact that we are programmatic clicking the button
    if (!Ext.isIE) {
      form.down('field[name=' + name + ']').fileInputEl.dom.click();
    }

    me.refreshAddButton(form);
  },

  /**
   * @private
   * Remove an artifact selection panel.
   */
  removeArtifact: function(button) {
    var me = this,
        form = button.up('form');

    form.remove(button.up(me.fileEntryPanelXType));
    me.refreshAddButton(form);
  },

  /**
   * @private
   * Move "Add" button by the end of form.
   */
  refreshAddButton: function(form) {
    var me = this,
        addButton = form.down('button[action=add]');

    if (addButton) {
      form.remove(addButton);
    }
    form.add({
      xtype: 'button',
      action: 'add',
      text: form.down(me.fileEntryPanelXType) ? 'Add another ' + form.entryName : 'Add a ' + form.entryName,
      margin: '5 0 10 0',
      glyph: 'xf016@FontAwesome' /* fa-file-o */
    });
  }

});
