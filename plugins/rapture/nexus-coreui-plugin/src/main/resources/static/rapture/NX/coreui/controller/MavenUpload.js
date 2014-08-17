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
 * Maven upload controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.MavenUpload', {
  extend: 'Ext.app.Controller',

  stores: [
    'MavenUploadRepositoryHosted'
  ],
  views: [
    'maven.MavenUpload'
  ],
  refs: [
    { ref: 'uploadPanel', selector: 'nx-coreui-maven-upload' }
  ],

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.getApplication().getFeaturesController().registerFeature({
      mode: 'browse',
      path: '/Upload/Maven',
      description: 'Upload artifacts to Maven Hosted Repositories',
      view: { xtype: 'nx-coreui-maven-upload' },
      iconConfig: {
        file: 'upload.png',
        variants: ['x16', 'x32']
      },
      visible: function() {
        return NX.Permissions.check('nexus:artifact', 'create');
      }
    });

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.loadRepositories
        }
      },
      component: {
        'nx-coreui-maven-upload': {
          beforerender: me.loadRepositories
        }
      }
    });
  },

  /**
   * @private
   * Loads repositories if upload panel is active.
   */
  loadRepositories: function() {
    var me = this,
        uploadPanel = me.getUploadPanel();

    if (uploadPanel) {
      me.getMavenUploadRepositoryHostedStore().load();
    }
  }

});