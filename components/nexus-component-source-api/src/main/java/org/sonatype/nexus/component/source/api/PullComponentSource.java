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
package org.sonatype.nexus.component.source.api;

import java.io.IOException;

import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.source.api.config.ComponentSourceFactory;

/**
 * A component source that provides components on request (e.g. when a component is not found in the local nexus
 * cluster).
 *
 * Filtering implementations of this interface may provide interceptor-like functionality - e.g. banning (filtering
 * out) certain components or automatically tagging component metadata with the name of the source. These should be
 * applied by the format-specific {@link ComponentSourceFactory}.
 *
 * @since 3.0
 */
public interface PullComponentSource
    extends ComponentSource, ConnectionStatus
{
  /**
   * Query the source for matching components.
   *
   * Formats are responsible for encoding all of their various request types into the query, whether these are mere
   * checks for the existence of a component, downloading just the metadata (partially or fully), or retrieving and
   * storing some or all of the binary assets of the component.
   *
   * If no component(s) match the query, the returned {@link Iterable} is empty.
   */
  <T extends Component> Iterable<ComponentEnvelope<T>> fetchComponents(ComponentRequest<T> request) throws IOException;
}
