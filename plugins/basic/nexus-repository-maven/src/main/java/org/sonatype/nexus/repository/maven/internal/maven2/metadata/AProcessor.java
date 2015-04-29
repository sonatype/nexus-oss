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
package org.sonatype.nexus.repository.maven.internal.maven2.metadata;

import org.sonatype.nexus.repository.storage.Processor;
import org.sonatype.nexus.repository.storage.ProcessorContext;
import org.sonatype.nexus.repository.storage.StorageFacet;

import org.apache.maven.artifact.repository.metadata.Metadata;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Maven2 artifactId processor.
 *
 * @since 3.0
 */
public class AProcessor
    extends Processor
{
  private final MetadataBuilder metadataBuilder;

  private final MetadataUpdater metadataUpdater;

  public AProcessor(final MetadataBuilder metadataBuilder, final MetadataUpdater metadataUpdater)
  {
    this.metadataBuilder = checkNotNull(metadataBuilder);
    this.metadataUpdater = checkNotNull(metadataUpdater);
  }

  @Override
  public void process(final ProcessorContext context) {
    final String artifactId = context.getAttributes().require(StorageFacet.P_NAME, String.class);
    log.info(">>>>>> A={}", artifactId);
    metadataBuilder.onEnterArtifactId(artifactId);
    context.proceed();
    final Metadata metadata = metadataBuilder.onExitArtifactId();
    if (metadata != null) {
      metadataUpdater.mayUpdateMetadata(metadata);
    }
    log.info("<<<<<< A={}", artifactId);
  }
}
