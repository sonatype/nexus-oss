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
package org.sonatype.nexus.web.internal;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;

/**
 * {@link GuiceFilter} that supports a dynamic ordered pipeline of filters and servlets.
 */
public final class NexusGuiceFilter
    extends GuiceFilter
{
  /*
   * Guice @Inject instead of JSR330 so Resin/CDI won't try to inject this and fail!
   */
  @com.google.inject.Inject
  static Injector injector; // defer creation of pipeline until constructor is called

  public NexusGuiceFilter() {
    super(injector.getInstance(DynamicFilterPipeline.class));
  }
}
