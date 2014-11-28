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
 * Task feature panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.task.TaskFeature', {
  extend: 'NX.view.masterdetail.Panel',
  alias: 'widget.nx-coreui-task-feature',

  list: 'nx-coreui-task-list',

  iconName: 'task-default',

  tabs: [
    { xtype: 'nx-info-panel', weight: 10 }
  ],

  actions: [
    { xtype: 'button', text: 'Delete', glyph: 'xf056@FontAwesome' /* fa-minus-circle */, action: 'delete', disabled: true },
    { xtype: 'button', text: 'Run', glyph: 'xf04b@FontAwesome' /* fa-play */, action: 'run', disabled: true },
    { xtype: 'button', text: 'Stop', glyph: 'xf04d@FontAwesome' /* fa-stop */, action: 'stop', disabled: true }
  ]
});
