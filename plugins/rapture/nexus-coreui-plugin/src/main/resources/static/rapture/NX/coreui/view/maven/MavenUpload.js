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
/**
 * Maven upload panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.maven.MavenUpload', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-maven-upload',

  initComponent: function () {
    var me = this,
        store;

    store = Ext.create('NX.coreui.store.RepositoryReference', { remoteFilter: true });
    store.filter(
        { property: 'type', value: 'hosted' },
        { property: 'format', value: 'maven2' }
    );

    me.items = {
      xtype: 'nx-coreui-upload-artifact',
      api: {
        submit: 'NX.direct.maven_Maven.upload'
      }
    };

    me.callParent();

    me.down('form').insert(0, [
      {
        xtype: 'label',
        html: '<p>Select the repository where to upload and artifacts to be uploaded.</p>'
      },
      {
        xtype: 'combo',
        name: 'repository',
        fieldLabel: 'Repository',
        emptyText: 'select repository to upload to',
        queryMode: 'local',
        displayField: 'name',
        valueField: 'id',
        store: store,
        editable: false
      }
    ]);
  }

});