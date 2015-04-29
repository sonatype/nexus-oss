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

import java.util.Objects;
import java.util.Set;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.maven.internal.MavenFacet;
import org.sonatype.nexus.repository.maven.internal.MavenPath;
import org.sonatype.nexus.repository.maven.internal.MavenPathParser;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.ImmutableMap;
import com.orientechnologies.orient.core.command.OCommandResultListener;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLAsynchQuery;
import org.apache.maven.artifact.repository.metadata.Metadata;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Maven 2 repository metadata builder.
 *
 * @since 3.0
 */
public class MetadataRebuildWorker
    extends ComponentSupport
{
  private final StorageFacet storageFacet;

  private final MavenPathParser mavenPathParser;

  private final MetadataBuilder metadataBuilder;

  private final MetadataUpdater metadataUpdater;

  public MetadataRebuildWorker(final Repository repository) {
    checkNotNull(repository);
    this.storageFacet = repository.facet(StorageFacet.class);
    this.metadataBuilder = new MetadataBuilder();
    final MavenFacet mavenFacet = repository.facet(MavenFacet.class);
    this.mavenPathParser = mavenFacet.getMavenPathParser();
    this.metadataUpdater = new MetadataUpdater(mavenFacet);
  }

  public void rebuild() {
    try (StorageTx tx = storageFacet.openTx()) {
      final ODatabaseDocumentTx readDatabase = tx.getDb();
      readDatabase.command(
          new OSQLAsynchQuery<ODocument>(
              // TODO: bucket
              "select group as groupId, name as artifactId, set(attributes.maven2.baseVersion) as baseVersions from component group by group, name limit=-1",
              new OCommandResultListener()
              {
                String currentGroupId = null;

                @Override
                public boolean result(Object iRecord) {
                  try {
                    final ODocument doc = (ODocument) iRecord;
                    final String groupId = doc.field("groupId", OType.STRING);
                    final String artifactId = doc.field("artifactId", OType.STRING);
                    final Set<String> baseVersions = doc.field("baseVersions", OType.EMBEDDEDSET);

                    final boolean groupChange = !Objects.equals(currentGroupId, groupId);
                    if (groupChange) {
                      currentGroupId = groupId;
                      metadataBuilder.onEnterGroupId(groupId);
                    }
                    txAbVs(groupId, artifactId, baseVersions);
                    if (groupChange) {
                      final Metadata groupMetadata = metadataBuilder.onExitGroupId();
                      if (groupMetadata != null) {
                        metadataUpdater.mayUpdateMetadata(groupMetadata);
                      }
                    }
                    return true;
                  }
                  finally {
                    ODatabaseRecordThreadLocal.INSTANCE.set(readDatabase); // always reset thread DB
                  }
                }

                @Override
                public void end() {
                }
              }
          )
      ).execute();
    }
  }

  /**
   * Method processing in separate TX/DB, performs writes (and mangles ThreadLocal DB, so caller should restore it).
   * Handles A and bV on metadataBuilder.
   */
  private void txAbVs(final String groupId, final String artifactId, final Set<String> baseVersions) {
    metadataBuilder.onEnterArtifactId(artifactId);
    for (String baseVersion : baseVersions) {
      metadataBuilder.onEnterBaseVersion(baseVersion);
      try (StorageTx tx = storageFacet.openTx()) {
        final Iterable<Component> components = tx.findComponents(
            "group = :group and name = :name and attributes.maven2.baseVersion = :baseVersion", // bucket = :bucket and
            // TODO: bucket.rid!
            ImmutableMap.<String, Object>of("group", groupId, "name", artifactId, "baseVersion", baseVersion),
            // "bucket", tx.getBucket(),
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
      final Metadata baseVersionMetadata = metadataBuilder.onExitBaseVersion();
      if (baseVersionMetadata != null) {
        metadataUpdater.mayUpdateMetadata(baseVersionMetadata);
      }
    }
    final Metadata artifactMetadata = metadataBuilder.onExitArtifactId();
    if (artifactMetadata != null) {
      metadataUpdater.mayUpdateMetadata(artifactMetadata);
    }
  }
}
