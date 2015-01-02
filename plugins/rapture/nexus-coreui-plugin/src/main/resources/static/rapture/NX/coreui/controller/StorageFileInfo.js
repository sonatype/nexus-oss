/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
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
 * Storage file info controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.StorageFileInfo', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.util.Url'
  ],

  views: [
    'repositorybrowse.StorageFileInfo'
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
   * Loads & shows generic information about selected file.
   */
  onUpdated: function(container, repositoryId, path) {
    var me = this,
        panel = container.down('nx-coreui-repositorybrowse-storagefileinfo');

    if (!panel) {
      panel = container.add({ xtype: 'nx-coreui-repositorybrowse-storagefileinfo', weight: 10 });
    }

    NX.direct.coreui_RepositoryStorage.readInfo(repositoryId, path, function(response) {
      var info = {},
          repositories = [];

      if (Ext.isObject(response) && response.success && response.data) {
        info[NX.I18n.get('BROWSE_STANDARD_INFO_PATH')] = NX.util.Url.asLink(
              NX.util.Url.urlOf('content/repositories/' + repositoryId + response.data['path']),
              response.data['path'] + (response.data['inLocalStorage'] ? '' : ' (Not Locally Cached)')
        );
        if (response.data['inLocalStorage']) {
          Ext.Array.each(response.data['repositories'], function(repository) {
            repositories.push(NX.util.Url.asLink(
                NX.util.Url.urlOf('#browse/repository/standard:' + repository['id'] + ':' + path),
                repository['name'],
                '_self'
            ));
          });
          info[NX.I18n.get('BROWSE_STANDARD_INFO_SIZE')] = me.toSizeString(response.data['size']);
          info[NX.I18n.get('BROWSE_STANDARD_INFO_UPLOADED_BY')] = response.data['createdBy'];
          info[NX.I18n.get('BROWSE_STANDARD_INFO_UPLOADED_DATE')] = Ext.Date.parse(response.data['created'], 'c');
          info[NX.I18n.get('BROWSE_STANDARD_INFO_MODIFIED')] = Ext.Date.parse(response.data['modified'], 'c');
          info[NX.I18n.get('BROWSE_STANDARD_INFO_SHA1')] = response.data['sha1'];
          info[NX.I18n.get('BROWSE_STANDARD_INFO_MD5')] = response.data['md5'];
          info[NX.I18n.get('BROWSE_STANDARD_INFO_CONTAINED')] = repositories;
        }
      }
      panel.showInfo(info);
    });
  },

  /**
   * @private
   */
  toSizeString: function(v) {
    if (typeof v !== 'number') {
      return '<unknown>';
    }
    if (v < 0) {
      return 'Unknown';
    }
    if (v < 1024) {
      return v + ' Bytes';
    }
    if (v < 1048576) {
      return (v / 1024).toFixed(2) + ' KB';
    }
    if (v < 1073741824) {
      return (v / 1048576).toFixed(2) + ' MB';
    }
    return (v / 1073741824).toFixed(2) + ' GB';
  }

});
