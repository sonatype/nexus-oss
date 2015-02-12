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

import org.sonatype.nexus.repository.Facet;

/**
 * Search {@link Facet}, providing means to index/de-index components.
 *
 * @since 3.0
 */
@Facet.Exposed
public interface SearchFacet
    extends Facet
{

  /**
   * Index component.
   *
   * @param data data to be indexed
   */
  void put(SearchableComponent data);

  /**
   * Removes component from index.
   *
   * @param id id of data to be deleted
   */
  void delete(String id);

}
