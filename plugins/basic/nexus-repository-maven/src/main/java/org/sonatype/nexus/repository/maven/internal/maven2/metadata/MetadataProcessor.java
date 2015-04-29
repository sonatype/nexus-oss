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

import org.sonatype.nexus.repository.maven.internal.MavenPath;
import org.sonatype.nexus.repository.maven.internal.MavenPathParser;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.Processor;
import org.sonatype.nexus.repository.storage.ProcessorContext;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;

import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Processor that processes components and their assets selected by G, A and bV. Is not standalone, as depends on
 * G, A and bV present in context.
 *
 * @since 3.0
 */
public class MetadataProcessor
    extends Processor
{
  private final MetadataBuilder metadataBuilder;

  private final MavenPathParser mavenPathParser;

  public MetadataProcessor(final MetadataBuilder metadataBuilder,
                           final MavenPathParser mavenPathParser)
  {
    this.metadataBuilder = checkNotNull(metadataBuilder);
    this.mavenPathParser = checkNotNull(mavenPathParser);
  }

  @Override
  public void process(final ProcessorContext context) {
    final String groupId = context.getAttributes().require(StorageFacet.P_GROUP, String.class);
    final String artifactId = context.getAttributes().require(StorageFacet.P_NAME, String.class);
    final String baseVersion = context.getAttributes().require(StorageFacet.P_VERSION, String.class);
    try (StorageTx tx = context.getStorageTxSupplier().get()) {
      final Iterable<Component> components = tx.findComponents(
          "group = :group and name = :name and attributes.maven2.baseVersion = :baseVersion", // bucket = :bucket and
          // TODO: bucket.rid!
          ImmutableMap.<String, Object>of("group", groupId, "name", artifactId, "baseVersion", baseVersion), // "bucket", tx.getBucket(),
          null,
          "order by version asc");
      for (Component component : components) {
        final Iterable<Asset> assets = tx.browseAssets(component);
        for (Asset asset : assets) {
          final MavenPath mavenPath = mavenPathParser.parsePath(
              asset.formatAttributes().require(StorageFacet.P_PATH, String.class)
          );
          metadataBuilder.addArtifactVersion(mavenPath);
          // TODO: crank up POM for this
          if (artifactId.endsWith("-plugin")) {
            metadataBuilder.addPlugin("prefix", artifactId, "Name");
          }
        }
      }
    }
  }
}
