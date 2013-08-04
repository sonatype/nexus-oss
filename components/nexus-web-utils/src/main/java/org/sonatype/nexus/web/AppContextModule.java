/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.web;

import org.sonatype.appcontext.AppContext;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;

/**
 * Module exposing AppContext as component. We intentionally directly reimplement the class to be found in appcontext
 * 3.1+ as in Nexus bundle, we have appcontext at Jetty cloassloader level, and would explode as at that level there is
 * no Guice available. Anyway, this class is a trivial one.
 *
 * @author cstamas
 * @since 2.1
 */
public class AppContextModule
    extends AbstractModule
{
  private final AppContext appContext;

  public AppContextModule(final AppContext appContext) {
    this.appContext = Preconditions.checkNotNull(appContext);
  }

  @Override
  protected void configure() {
    bind(AppContext.class).toInstance(appContext);
  }
}
