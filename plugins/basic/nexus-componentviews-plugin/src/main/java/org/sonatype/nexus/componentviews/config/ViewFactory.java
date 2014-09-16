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

import org.sonatype.nexus.componentviews.View;

/**
 * A process for taking a view's user-supplied config (USC) and rendering it into a usable {@link View}.
 *
 * @since 3.0
 */
public interface ViewFactory
{
  /**
   * This is an identifier used to look up the factory. (It's not the name of the view the factory produces, that comes
   * from the {@link ViewConfig}.
   */
  String getFactoryName();

  /**
   * Construct a view, ready to respond to incoming requests.
   */
  View createView(ViewConfig userSuppliedConfig);
}
