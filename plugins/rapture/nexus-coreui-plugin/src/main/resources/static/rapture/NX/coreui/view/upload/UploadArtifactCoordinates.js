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
      fieldLabel: 'Group',
      helpText: 'Maven group id of uploaded artifacts.',
      emptyText: 'enter a group name'
    },
    {
      name: 'artifactId',
      itemId: 'artifactId',
      fieldLabel: 'Artifact',
      helpText: 'Maven artifact id of uploaded artifacts.',
      emptyText: 'enter an artifact name'
    },
    {
      name: 'version',
      itemId: 'version',
      fieldLabel: 'Version',
      helpText: 'Maven version of uploaded artifacts.',
      emptyText: 'enter a version'
    },
    {
      xtype: 'combo',
      name: 'packaging',
      itemId: 'packaging',
      fieldLabel: 'Packaging',
      helpText: 'Maven packaging.',
      emptyText: 'select or enter packaging',
      queryMode: 'local',
      displayField: 'name',
      valueField: 'id',
      store: ['pom', 'jar', 'ejb', 'war', 'ear', 'rar', 'par', 'maven-archetype', 'maven-plugin']
    }
  ]

});