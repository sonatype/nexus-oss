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

import com.tinkerpop.blueprints.impls.orient.OrientVertex;

import static org.sonatype.nexus.repository.storage.StorageFacet.P_REPOSITORY_NAME;

class BucketImpl
    extends VertexWrapperSupport
    implements Bucket
{
  BucketImpl(OrientVertex vertex) {
    super(vertex);
  }

  @Override
  public String repositoryName() {
    return get(P_REPOSITORY_NAME);
  }

  @Override
  public Bucket repositoryName(String repositoryName) {
    set(P_REPOSITORY_NAME, repositoryName);
    return this;
  }
}
