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
package org.sonatype.nexus.repository.storage;

import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.repository.Format;

import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.joda.time.DateTime;

import static org.sonatype.nexus.repository.storage.StorageFacet.P_ATTRIBUTES;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_FORMAT;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_LAST_UPDATED;

/**
 * Supporting base class for {@link MetadataNode} implementations.
 *
 * @since 3.0
 */
abstract class MetadataNodeSupport
    extends VertexWrapperSupport
    implements MetadataNode
{
  protected final NestedAttributesMap attributes;

  MetadataNodeSupport(OrientVertex vertex) {
    super(vertex);
    Map<String, Object> backing = vertex.getProperty(P_ATTRIBUTES);
    this.attributes = new NestedAttributesMap(P_ATTRIBUTES, backing);
  }

  @Override
  public NestedAttributesMap attributes() {
    return attributes;
  }

  @Override
  public NestedAttributesMap formatAttributes() {
    return attributes().child(require(P_FORMAT));
  }

  @Override
  public String format() {
    return require(P_FORMAT);
  }

  @Override
  @Nullable
  public DateTime lastUpdated() {
    return get(P_LAST_UPDATED, DateTime.class);
  }

  @Override
  public DateTime requireLastUpdated() {
    return require(P_LAST_UPDATED, DateTime.class);
  }
}
