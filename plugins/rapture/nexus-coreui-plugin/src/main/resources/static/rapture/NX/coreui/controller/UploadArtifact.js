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
 * Artifact upload controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.UploadArtifact', {
  extend: 'Ext.app.Controller',

  views: [
    'upload.UploadArtifact',
    'upload.UploadArtifactCoordinates',
    'upload.UploadArtifactFile'
  ],

  /**
   * @private
   */
  counter: 0,

  artifactPanelXType: 'nx-coreui-upload-artifact-file',

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.listen({
      component: {
        'nx-coreui-upload-artifact': {
          afterrender: me.afterRender
        },
        'nx-coreui-upload-artifact button[action=upload]': {
          click: me.upload
        },
        'nx-coreui-upload-artifact button[action=discard]': {
          click: me.discard
        },
        'nx-coreui-upload-artifact button[action=add]': {
          click: me.addArtifact
        },
        'nx-coreui-upload-artifact-file button[action=delete]': {
          click: me.removeArtifact
        },
        'nx-coreui-upload-artifact-file fileuploadfield': {
          change: me.onFileSelected
        },
        'nx-coreui-upload-artifact-file field': {
          change: me.onFieldChange
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
      waitMsg: 'Uploading your artifacts...',
      success: function() {
        NX.Messages.add({ text: 'Artifacts uploaded', type: 'success' });
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
    artifactPanel = form.down(me.artifactPanelXType);
    while (artifactPanel) {
      form.remove(artifactPanel);
      artifactPanel = form.down(me.artifactPanelXType);
    }
    me.refreshAddButton(form);
    me.showOrHideCoordinates(form);
  },

  /**
   * @private
   * Add an artifact selection panel.
   */
  addArtifact: function(button) {
    var me = this,
        form = button.up('form'),
        name = 'a.' + me.counter++;

    form.add({ xtype: me.artifactPanelXType, name: name });

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

    form.remove(button.up(me.artifactPanelXType));
    me.refreshAddButton(form);
    me.showOrHideCoordinates(form);
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
      text: form.down(me.artifactPanelXType) ? 'Add another artifact' : 'Add an artifact',
      margin: '5 0 10 0',
      glyph: 'xf016@FontAwesome' /* fa-file-o */
    });
  },

  /**
   * @private
   * Update coordinates if possible.
   */
  onFileSelected: function(button, fileName) {
    var me = this,
        form = button.up('form'),
        artifactPanel = button.up(me.artifactPanelXType),
        coordinates = me.guessCoordinates(fileName),
        artifactId, version, packaging;

    artifactPanel.classifier.setValue(coordinates.classifier);
    artifactPanel.extension.setValue(coordinates.extension);

    me.showOrHideCoordinates(form);

    if (!(coordinates.extension === 'pom')) {
      artifactId = form.down('#artifactId');
      if (artifactId && !artifactId.getValue()) {
        artifactId.setValue(coordinates.artifact);
      }
      version = form.down('#version');
      if (version && !version.getValue()) {
        version.setValue(coordinates.version);
      }
      packaging = form.down('#packaging');
      if (packaging && !packaging.getValue()) {
        packaging.setValue(coordinates.packaging);
      }
    }
  },

  /**
   * @private
   */
  onFieldChange: function(field) {
    var me = this;

    me.showOrHideCoordinates(field.up('form'));
  },

  /**
   * @private
   * Show or Hide coordinates if a pom was selected to be uploaded or not.
   */
  showOrHideCoordinates: function(form) {
    var me = this,
        coordinatesPanel = form.down('nx-coreui-upload-artifact-coordinates'),
        fileWithPomExtension = form.down(me.artifactPanelXType + ' field[extension=true][value=pom]');

    if (!form.down(me.artifactPanelXType) ||
        (fileWithPomExtension && !fileWithPomExtension.up('panel').classifier.value)) {
      coordinatesPanel.disable();
      coordinatesPanel.hide();
    }
    else {
      coordinatesPanel.enable();
      coordinatesPanel.show();
      form.isValid();
    }
  },

  /**
   * @private
   * Try to guess coordinates out of a file name.
   */
  guessCoordinates: function(fileName) {
    var g = '', a = '', v = '', c = '', p = '', e = '';

    if (Ext.String.endsWith(fileName, 'pom.xml', true)) {
      return { extension: 'pom' };
    }

    // match extension to guess the packaging
    var extensionIndex = fileName.lastIndexOf('.');
    if (extensionIndex > 0) {
      p = fileName.substring(extensionIndex + 1);
      e = fileName.substring(extensionIndex + 1);
      fileName = fileName.substring(0, extensionIndex);

      if (e === 'asc') {
        var primaryExtensionIndex = fileName.substring(0, extensionIndex).lastIndexOf('.');
        var primaryExtension = '';
        if (primaryExtensionIndex >= 0) {
          primaryExtension = fileName.substring(primaryExtensionIndex + 1);
        }

        if (/^[a-z]*$/.test(primaryExtension)) {
          e = primaryExtension + '.' + e;
          fileName = fileName.substring(0, primaryExtensionIndex);
        }
      }
    }

    // match the path to guess the group
    if (fileName.indexOf('\\') >= 0) {
      fileName = fileName.replace(/\\/g, '\/');
    }
    var slashIndex = fileName.lastIndexOf('/');
    if (slashIndex) {
      g = fileName.substring(0, slashIndex);

      fileName = fileName.substring(slashIndex + 1);
    }

    // separate the artifact name and version
    var versionIndex = fileName.search(/\-[\d]/);
    if (versionIndex === -1) {
      versionIndex = fileName.search(/-LATEST-/i);
      if (versionIndex === -1) {
        versionIndex = fileName.search(/-CURRENT-/i);
      }
    }
    if (versionIndex >= 0) {
      a = fileName.substring(0, versionIndex).toLowerCase();

      // guess the version
      fileName = fileName.substring(versionIndex + 1);
      var classifierIndex = fileName.lastIndexOf('-');
      if (classifierIndex >= 0) {
        var classifier = fileName.substring(classifierIndex + 1);
        if (classifier && !(/^SNAPSHOT$/i.test(classifier) || /^\d/.test(classifier)
            || /^LATEST$/i.test(classifier)
            || /^CURRENT$/i.test(classifier))) {
          c = classifier;
          fileName = fileName.substring(0, classifierIndex);
          // dont guess packaging when there is a classifier
          p = '';
          extensionIndex = c.indexOf('.');
          if (extensionIndex >= 0) {
            e = c.substring(extensionIndex + 1) + '.' + e;
            c = c.substring(0, extensionIndex);
          }
        }
      }
      v = fileName;

      if (g) {
        // if group ends with version and artifact name, strip those parts
        // (useful if uploading from a local maven repo)
        var i = g.search(new RegExp('\/' + v + '$'));
        if (i > -1) {
          g = g.substring(0, i);
        }
        i = g.search(new RegExp('\/' + a + '$'));
        if (i > -1) {
          g = g.substring(0, i);
        }

        // strip extra path parts, leave only com.* or org.* or net.* or the
        // last element
        i = g.lastIndexOf('/com/');
        if (i === -1) {
          i = g.lastIndexOf('/org/');
          if (i === -1) {
            i = g.lastIndexOf('/net/');
            if (i === -1) {
              i = g.lastIndexOf('/');
            }
          }
        }
        g = g.substring(i + 1).replace(/\//g, '.').toLowerCase();
      }
    }
    else {
      g = '';
    }

    return { group: g, artifact: a, version: v, packaging: p, extension: e, classifier: c };
  }

});