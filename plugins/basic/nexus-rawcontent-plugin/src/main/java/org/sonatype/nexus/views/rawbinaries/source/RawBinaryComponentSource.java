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
package org.sonatype.nexus.views.rawbinaries.source;

import java.io.IOException;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.source.api.ComponentEnvelope;
import org.sonatype.nexus.component.source.api.ComponentRequest;
import org.sonatype.nexus.component.source.api.ComponentSourceId;
import org.sonatype.nexus.component.source.api.PullComponentSource;

/**
 * @since 3.0
 */
public class RawBinaryComponentSource
    implements PullComponentSource
{
  private final ComponentSourceId sourceName;

  private final String urlPrefix;

  public RawBinaryComponentSource(final ComponentSourceId sourceName, final String urlPrefix) {
    this.sourceName = sourceName;
    this.urlPrefix = urlPrefix;
  }

  @Nullable
  @Override
  public <T extends Component> Iterable<ComponentEnvelope<T>> fetchComponents(final ComponentRequest request)
      throws IOException
  {
    // TODO: Implement

    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public ComponentSourceId getId() {
    return sourceName;
  }

  @Override
  public boolean isAvailable() {
    return true;
  }
}
