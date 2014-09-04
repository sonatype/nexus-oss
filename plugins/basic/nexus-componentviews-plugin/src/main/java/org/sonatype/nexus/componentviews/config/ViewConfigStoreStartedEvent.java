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
package org.sonatype.nexus.componentviews.config;

/**
 * This is an event chiefly designed to facilitate example view plugins, so they can register configurations.  (There is
 * currently no way to do this in the absence of a view config UI.)
 *
 * @since 3.0
 */
public class ViewConfigStoreStartedEvent
    extends ViewConfigStoreEvent
{
  public ViewConfigStoreStartedEvent(final ViewConfigStore component) {
    super(component);
  }
}
