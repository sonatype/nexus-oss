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
package org.sonatype.nexus.views.rawbinaries.internal;

import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.model.ComponentId;

/**
 * An essentially placebo implementation of {@link Component} so this plugin has the same structure as other formats for
 * example purposes.
 *
 * @since 3.0
 */
public class RawComponent
    implements Component
{
  @Override
  public ComponentId getId() {
    return null;
  }
}
