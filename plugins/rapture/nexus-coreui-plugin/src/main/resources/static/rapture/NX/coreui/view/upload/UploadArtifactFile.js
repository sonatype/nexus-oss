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
 * Upload artifact file panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.upload.UploadArtifactFile', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-upload-artifact-file',
  requires: [
    'NX.I18n'
  ],

  initComponent: function() {
    var me = this;

    me.items = [
      {
        xtype: 'panel',
        layout: {
          type: 'hbox',
          align: 'bottom'
        },
        style: {
          borderTop: '1px solid #F0F0F0'
        },
        margin: '0 0 5 0',
        defaults: {
          margin: '5 5 0 0'
        },
        items: [
          me.uploader = {
            xtype: 'fileuploadfield',
            fieldLabel: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_FILE'),
            name: me.name,
            allowBlank: false,
            clearOnSubmit: false,
            buttonConfig: {
              text: undefined, glyph: 'xf016@FontAwesome' /* fa-file-o */
            }
          },
          { xtype: 'button', glyph: 'xf056@FontAwesome' /* fa-minus-circle */, action: 'delete' }
        ]
      },
      {
        xtype: 'textfield',
        fieldLabel: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_CLASSIFIER'),
        helpText: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_CLASSIFIER_HELP'),
        name: me.name + '.classifier',
        classifier: true
      },
      {
        xtype: 'textfield',
        fieldLabel: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_EXTENSION'),
        helpText: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_EXTENSION_HELP'),
        name: me.name + '.extension',
        extension: true,
        allowBlank: false
      }
    ];

    me.callParent();

    me.classifier = me.down('field[name=' + me.name + '.classifier]');
    me.extension = me.down('field[name=' + me.name + '.extension]');
  }

});
