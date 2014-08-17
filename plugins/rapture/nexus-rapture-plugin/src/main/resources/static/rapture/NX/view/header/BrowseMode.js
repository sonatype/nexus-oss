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
/*global Ext*/

/**
 * "browse" mode button.
 *
 * @since 3.0
 */
Ext.define('NX.view.header.BrowseMode', {
  extend: 'NX.view.header.Mode',
  alias: 'widget.nx-header-browse-mode',

  mode: 'browse',
  title: 'Browse',
  tooltip: 'Browse server contents',
  glyph: 'xf1b2@FontAwesome' /* fa-cube */

});
