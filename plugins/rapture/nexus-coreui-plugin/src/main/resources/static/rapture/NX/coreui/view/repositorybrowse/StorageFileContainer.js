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
 * Storage file tabs container.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repositorybrowse.StorageFileContainer', {
  extend: 'NX.ext.tab.SortedPanel',
  alias: 'widget.nx-coreui-repositorybrowse-storagefilecontainer',
  requires: [
    'NX.Icons'
  ],

  title: '',

  // HACK: For now make all detail panels light themed while we sort out the overall look of rapture
  // FIXME: This will NOT works, since this is a 'Ext.tab.Panel' impl, not a 'Ext.panel.Panel'
  // FIXME: color of this panel will be darker than others in the same location because of this
  //ui: 'feature-detail',

  tools: [
    { type: 'maximize', tooltip: 'Maximize' }
  ],

  /**
   * Repository of currently shown file.
   */
  repositoryId: undefined,

  /**
   * Path of currently shown file.
   */
  path: undefined,

  /**
   * Type of currently shown file.
   */
  type: undefined,

  /**
   * @public
   * Shows a file in container.
   *
   * @param [repositoryId] containing the file to be shown
   * @param [path] of file to be shown
   * @param [type] of file to be shown
   */
  showStorageFile: function(repositoryId, path, type) {
    var me = this,
        iconName = 'repository-item-type-default',
        segments;

    me.repositoryId = repositoryId;
    me.path = path;
    me.type = type;

    if (repositoryId && path) {
      if (me.hidden) {
        me.show();
      }
      if (NX.getApplication().getIconController().findIcon('repository-item-type-' + type, 'x16')) {
        iconName = 'repository-item-type-' + type;
      }
      me.setIconCls(NX.Icons.cls(iconName, 'x16'));
      segments = path.split('/');
      me.setTitle(segments[segments.length - 1]);

      me.fireEvent('updated', me, repositoryId, path);

      if (!me.getActiveTab()) {
        me.setActiveTab(0);
      }
    }
    else {
      me.hide();
    }
  }
});
