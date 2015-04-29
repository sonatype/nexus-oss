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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.maven.internal.MavenFacet;
import org.sonatype.nexus.repository.maven.internal.MavenPath;
import org.sonatype.nexus.repository.maven.internal.maven2.Maven2Format;
import org.sonatype.nexus.repository.maven.internal.maven2.Maven2MetadataMerger;
import org.sonatype.nexus.repository.maven.internal.maven2.Maven2MetadataMerger.MetadataEnvelope;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.payloads.BytesPayload;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Maven 2 repository metadata updater.
 *
 * @since 3.0
 */
public class MetadataUpdater
    extends ComponentSupport
{
  private final MavenFacet mavenFacet;

  private final Maven2MetadataMerger metadataMerger;

  private final MetadataXpp3Reader metadataReader;

  private final MetadataXpp3Writer metadataWriter;

  public MetadataUpdater(final MavenFacet mavenFacet) {
    this.mavenFacet = checkNotNull(mavenFacet);
    this.metadataMerger = new Maven2MetadataMerger();
    this.metadataReader = new MetadataXpp3Reader();
    this.metadataWriter = new MetadataXpp3Writer();
  }

  /**
   * Writes if not exists, if exists, compares and merges if differs.
   */
  public void mayUpdateMetadata(final Metadata metadata) {
    checkNotNull(metadata);
    final MavenPath mavenPath = mavenFacet.getMavenPathParser().parsePath(
        metadataPath(
            metadata.getGroupId(),
            metadata.getArtifactId(),
            metadata.getVersion()
        )
    );

    try {
      final Metadata oldMetadata = read(mavenPath);
      if (oldMetadata == null) {
        // old does not exists, just write it
        write(mavenPath, metadata);
      }
      else {
        // TODO: compare? unsure is it worth it, as compare would also eat CPU maybe even more that writing would
        // update old by merging them and write out
        final Metadata updated = metadataMerger.merge(
            ImmutableList.of(
                new MetadataEnvelope("old:" + mavenPath.getPath(), oldMetadata),
                new MetadataEnvelope("new" + mavenPath.getPath(), metadata)
            )
        );
        write(mavenPath, updated);
      }
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private String metadataPath(final String groupId,
                              @Nullable final String artifactId,
                              @Nullable final String baseVersion)
  {
    final StringBuilder sb = new StringBuilder("/");
    sb.append(groupId.replace('.', '/'));
    if (artifactId != null) {
      sb.append("/").append(artifactId);
      if (baseVersion != null) {
        sb.append("/").append(baseVersion);
      }
    }
    sb.append("/").append(Maven2Format.METADATA_FILENAME);
    return sb.toString();
  }

  @Nullable
  private Metadata read(final MavenPath mavenPath) throws IOException {
    final Content content = mavenFacet.get(mavenPath);
    if (content == null) {
      return null;
    }
    else {
      try (InputStream is = content.openInputStream()) {
        return metadataReader.read(is);
      }
      catch (XmlPullParserException e) {
        // corrupted, nuke it
        return null;
      }
    }
  }

  private void write(final MavenPath mavenPath, final Metadata metadata) throws IOException {
    System.out.println(mavenPath);
    metadataWriter.write(System.out, metadata);
    System.out.println();
    /*
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    metadataWriter.write(byteArrayOutputStream, metadata);
    mavenFacet.put(
        mavenPath,
        new BytesPayload(byteArrayOutputStream.toByteArray(), Maven2Format.METADATA_CONTENT_TYPE)
    );
    */
  }
}
