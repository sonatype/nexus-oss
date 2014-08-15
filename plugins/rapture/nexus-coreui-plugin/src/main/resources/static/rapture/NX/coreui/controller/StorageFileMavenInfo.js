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
 * Storage file Maven info controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.StorageFileMavenInfo', {
  extend: 'Ext.app.Controller',

  views: [
    'repositorybrowse.StorageFileMavenInfo'
  ],

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.listen({
      component: {
        'nx-coreui-repositorybrowse-storagefilecontainer': {
          updated: me.onUpdated
        }
      }
    });
  },

  /**
   * @private
   * Loads & eventual shows Maven information about selected file.
   */
  onUpdated: function(container, repositoryId, path) {
    NX.direct.coreui_Maven.readInfo(repositoryId, path, function(response) {
      var panel = container.down('nx-coreui-repositorybrowse-storagefilemaveninfo'),
          info;

      if (Ext.isObject(response) && response.success && response.data) {
        if (!panel) {
          panel = container.add({ xtype: 'nx-coreui-repositorybrowse-storagefilemaveninfo', weight: 20 });
        }
        info = {
          'Group': response.data['groupId'],
          'Artifact': response.data['artifactId'],
          'Version': response.data['version']
        };
        if (response.data['classifier']) {
          info = Ext.apply(info, {
            'Classifier': response.data['classifier']
          });
        }
        info = Ext.apply(info, {
          'Extension': response.data['extension'],
          'XML': '<pre>' + Ext.String.htmlEncode(response.data['xml']) + '</pre>'
        });
        panel.showInfo(info);
      }
      else {
        if (panel) {
          container.remove(panel);
        }
      }
    });
  }

});