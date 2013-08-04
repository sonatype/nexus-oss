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

package org.sonatype.nexus.proxy.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;

/**
 * The event fired in case of some content changes in Nexus related to multiple item/file (a batch), and is being
 * announced by some component. This might be some subsystem doing "atomic deploy", or batch removal, or similar. These
 * events are NOT about deploys (or deletion, caching etc), and a (series of) deploys or deletions will be emitted
 * before this event. Single {@link RepositoryItemEventStore} will be fired for each deploy always.
 *
 * @author cstamas
 * @since 2.3
 */
public abstract class RepositoryItemBatchEvent
    extends RepositoryEvent
{
  private final List<String> itemPaths;

  /**
   * Constructor.
   */
  public RepositoryItemBatchEvent(final Repository repository, final Collection<String> itemPaths) {
    super(repository);
    this.itemPaths = Collections.unmodifiableList(new ArrayList<String>(itemPaths));
  }

  /**
   * Gets the involved item.
   *
   * @return list of items in this batch.
   */
  public List<String> getItemPaths() {
    return itemPaths;
  }

  // ==

  @Override
  public String toString() {
    return String.format("%s(sender=%s, %s)", getClass().getSimpleName(),
        RepositoryStringUtils.getHumanizedNameString(getRepository()), getItemPaths());
  }
}
