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
 * Browse repository index panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repositorybrowse.BrowseIndex', {
  extend: 'Ext.Panel',
  alias: 'widget.nx-coreui-repositorybrowse-index',

  layout: 'border',

  items: [
    {
      xtype: 'nx-coreui-repositorybrowse-indextree',
      region: 'center'
    },
    {
      xtype: 'nx-coreui-repositorybrowse-storagefilecontainer',
      region: 'east',
      split: true,
      flex: 1,
      header: false,
      collapsible: true,
      collapsed: false,
      collapseMode: 'mini',
      hidden: false
    }
  ]

});
