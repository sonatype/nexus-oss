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
 * Upload artifact coordinates.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.upload.UploadArtifactCoordinates', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-coreui-upload-artifact-coordinates',

  defaults: {
    xtype: 'textfield',
    allowBlank: false
  },

  items: [
    {
      name: 'groupId',
      itemId: 'groupId',
      fieldLabel: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_GROUP'),
      helpText: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_GROUP_HELP'),
      emptyText: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_GROUP_PLACEHOLDER')
    },
    {
      name: 'artifactId',
      itemId: 'artifactId',
      fieldLabel: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_ID'),
      helpText: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_ID_HELP'),
      emptyText: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_ID_PLACEHOLDER')
    },
    {
      name: 'version',
      itemId: 'version',
      fieldLabel: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_VERSION'),
      helpText: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_VERSION_HELP'),
      emptyText: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_VERSION_PLACEHOLDER')
    },
    {
      xtype: 'combo',
      name: 'packaging',
      itemId: 'packaging',
      fieldLabel: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_PACKAGING'),
      helpText: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_PACKAGING_HELP'),
      emptyText: NX.I18n.get('BROWSE_MAVEN_ARTIFACT_PACKAGING_PLACEHOLDER'),
      queryMode: 'local',
      displayField: 'name',
      valueField: 'id',
      store: ['pom', 'jar', 'ejb', 'war', 'ear', 'rar', 'par', 'maven-archetype', 'maven-plugin']
    }
  ]

});