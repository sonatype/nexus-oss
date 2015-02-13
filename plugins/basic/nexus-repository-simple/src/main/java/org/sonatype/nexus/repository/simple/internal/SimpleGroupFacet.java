/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.simple.internal;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.group.GroupFacetImpl;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.simple.SimpleContent;
import org.sonatype.nexus.repository.simple.SimpleContentCreatedEvent;
import org.sonatype.nexus.repository.simple.SimpleContentDeletedEvent;
import org.sonatype.nexus.repository.simple.SimpleContentEvent;
import org.sonatype.nexus.repository.simple.SimpleContentUpdatedEvent;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

import static org.sonatype.nexus.repository.FacetSupport.State.STARTED;

/**
 * Simple {link GroupFacet}.
 *
 * @since 3.0
 */
@Named
public class SimpleGroupFacet
    extends GroupFacetImpl
    implements SimpleIndexHtmlContents
{
  private SimpleIndexHtmlFacet indexHtml;

  @Inject
  public SimpleGroupFacet(final RepositoryManager repositoryManager) {
    super(repositoryManager);
  }

  @Override
  protected void doStart() throws Exception {
    super.doStart();
    indexHtml = getRepository().facet(SimpleIndexHtmlFacet.class);
  }

  @Override
  protected void doStop() throws Exception {
    indexHtml = null;
    super.doStop();
  }

  //
  // SimpleIndexHtmlContents
  //

  /**
   * Returns a union of all member repositories contents.
   *
   * Order of members is important, first member with content will be included,
   * second with same name is masked by former.
   */
  @Override
  @Guarded(by=STARTED)
  public Set<Entry<String, SimpleContent>> entries() {
    Map<String, SimpleContent> union = Maps.newHashMap();

    for (Repository repository : members()) {
      SimpleIndexHtmlContents contents = repository.facet(SimpleIndexHtmlContents.class);
      for (Map.Entry<String, SimpleContent> entry : contents.entries()) {
        // only add new content if named entry does not already exist
        if (!union.containsKey(entry.getKey())) {
          union.put(entry.getKey(), entry.getValue());
        }
      }
    }

    return union.entrySet();
  }

  /**
   * Invalidate index.html cache if any member repositories are mutated.
   */
  private void maybeInvalidateIndexHtmlCache(final SimpleContentEvent event) {
    if (member(event.getRepository())) {
      indexHtml.invalidate();
    }
  }

  @Subscribe
  public void on(final SimpleContentCreatedEvent event) {
    maybeInvalidateIndexHtmlCache(event);
  }

  @Subscribe
  public void on(final SimpleContentUpdatedEvent event) {
    maybeInvalidateIndexHtmlCache(event);
  }

  @Subscribe
  public void on(final SimpleContentDeletedEvent event) {
    maybeInvalidateIndexHtmlCache(event);
  }
}
