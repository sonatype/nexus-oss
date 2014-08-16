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
 * Storage File Container controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.StorageFileContainer', {
  extend: 'Ext.app.Controller',
  requires: [
    'Ext.window.Window'
  ],

  views: [
    'repositorybrowse.StorageFileContainer'
  ],
  refs: [
    { ref: 'storageFileContainer', selector: 'nx-coreui-repositorybrowse-storagefilecontainer' }
  ],

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'repository-item-type-default': { file: 'file_extension_default.png', variants: ['x16', 'x32'] },
      'repository-item-type-md5': { file: 'file_extension_checksum.png', variants: ['x16', 'x32'] },
      'repository-item-type-jar': { file: 'file_extension_jar.png', variants: ['x16', 'x32'] },
      'repository-item-type-pom': { file: 'file_extension_xml.png', variants: ['x16', 'x32'] },
      'repository-item-type-sha1': { file: 'file_extension_checksum.png', variants: ['x16', 'x32'] },
      'repository-item-type-xml': { file: 'file_extension_xml.png', variants: ['x16', 'x32'] },
      'repository-item-type-zip': { file: 'file_extension_zip.png', variants: ['x16', 'x32'] }
    });

    me.listen({
      component: {
        'nx-coreui-repositorybrowse-storagefilecontainer tool[type=maximize]': {
          click: me.onMaximize
        }
      }
    });
  },

  /**
   * Show a maximized window containing the storage file container.
   * @param tool maximize tool
   */
  onMaximize: function(tool) {
    var storageFileContainer = tool.up('nx-coreui-repositorybrowse-storagefilecontainer'),
        container = storageFileContainer.up('container'),
        win;

    storageFileContainer.up('container').remove(storageFileContainer, false);
    storageFileContainer.getHeader().hide();
    win = Ext.create('Ext.window.Window', {
      maximized: true,
      autoScroll: true,
      closable: false,
      layout: 'fit',
      items: storageFileContainer,
      tools: [
        {
          type: 'close',
          handler: function() {
            win.hide(storageFileContainer, function() {
              win.remove(storageFileContainer, false);
              storageFileContainer.getHeader().show();
              container.add(storageFileContainer);
              win.destroy();
            });
          }
        }
      ],
      title: storageFileContainer.title,
      iconCls: storageFileContainer.iconCls
    });

    win.show(storageFileContainer);
  }

});