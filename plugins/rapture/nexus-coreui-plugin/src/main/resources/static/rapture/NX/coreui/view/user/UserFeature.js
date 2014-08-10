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
 * User feature panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.user.UserFeature', {
  extend: 'NX.view.masterdetail.Panel',
  alias: 'widget.nx-coreui-user-feature',

  list: 'nx-coreui-user-list',

  // FIXME: This seems strange to configure icon name here for the detail panel
  iconName: 'user-default',

  tabs: [
    { xtype: 'nx-coreui-user-settings', title: 'Settings' },
    { xtype: 'nx-coreui-privilege-trace', title: 'Privilege Trace' },
    { xtype: 'nx-coreui-role-tree', title: 'Role Tree' }
  ]
});
