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

import org.sonatype.nexus.component.model.Component;

/**
 * A remote source for components, where component transfers are initiated by the remote source, such as Smart Proxy.
 *
 * @since 3.0
 */
public interface PushComponentSource<T extends Component>
    extends ComponentSource
{
  /**
   * Register a {@link ComponentReceiver} with this source so it will receive components from this source.
   */
  void register(ComponentReceiver<T> receiver);

  /**
   * Unregister the receiver.
   */
  void unregister(ComponentReceiver<T> receiver);
}
