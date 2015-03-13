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
package org.sonatype.nexus.repository.search;

import org.sonatype.nexus.orient.graph.GraphTx;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.ORecordHookFactory;

import com.orientechnologies.orient.core.hook.ORecordHook;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Factory for {@link SearchFacetRecordHook} hook.
 *
 * @since 3.0
 */
public class SearchFacetRecordHookFactory
    implements ORecordHookFactory
{
  private final ComponentMetadataFactory componentMetadataFactory;

  public SearchFacetRecordHookFactory(final ComponentMetadataFactory componentMetadataFactory) {
    this.componentMetadataFactory = checkNotNull(componentMetadataFactory);
  }

  @Override
  public ORecordHook createHook(final GraphTx graphTx, final Repository repository) {
    return new SearchFacetRecordHook(componentMetadataFactory, graphTx, repository.facet(SearchFacet.class));
  }
}
