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
package org.sonatype.nexus.componentviews;

/**
 * Fired when a {@link View} is removed from the {@link ViewRegistry} and will therefore no longer be given any
 * new requests, although the View may still be handling requests that were dispatched to it before it was
 * unregistered.
 *
 * @since 3.0
 */
public class ViewUnregisteredEvent
    extends ViewRegistryEvent
{
  public ViewUnregisteredEvent(final ViewRegistry component, final View view) {
    super(component, view);
  }
}
