/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.maven.MavenFacet;
import org.sonatype.nexus.repository.maven.MavenPath;
import org.sonatype.nexus.repository.maven.MavenPath.HashType;
import org.sonatype.nexus.repository.maven.MavenPathParser;
import org.sonatype.nexus.repository.maven.internal.DigestExtractor;
import org.sonatype.nexus.repository.maven.internal.MavenAttributes;
import org.sonatype.nexus.repository.maven.internal.maven2.Constants;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.BucketEntityAdapter;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.ComponentDatabase;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.command.OCommandResultListener;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLAsynchQuery;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Maven 2 repository metadata re-builder.
 *
 * @since 3.0
 */
@Singleton
@Named
public class MetadataRebuilder
    extends ComponentSupport
{
  private final Provider<DatabaseInstance> databaseInstanceProvider;

  private final BucketEntityAdapter bucketEntityAdapter;

  @Inject
  public MetadataRebuilder(@Named(ComponentDatabase.NAME) final Provider<DatabaseInstance> databaseInstanceProvider,
                           final BucketEntityAdapter bucketEntityAdapter)
  {
    this.databaseInstanceProvider = checkNotNull(databaseInstanceProvider);
    this.bucketEntityAdapter = checkNotNull(bucketEntityAdapter);
  }

  /**
   * Rebuilds/updates Maven metadata.
   *
   * @param repository  The repository whose metadata needs rebuild (Maven2 format, Hosted type only).
   * @param update      if {@code true}, updates existing metadata, otherwise overwrites them with newly generated
   *                    ones.
   * @param groupId     scope the work to given groupId.
   * @param artifactId  scope the work to given artifactId (groupId must be given).
   * @param baseVersion scope the work to given baseVersion (groupId and artifactId must ge given).
   */
  public void rebuild(final Repository repository,
                      final boolean update,
                      @Nullable final String groupId,
                      @Nullable final String artifactId,
                      @Nullable final String baseVersion)
  {
    checkNotNull(repository);
    final StringBuilder sql = new StringBuilder();
    final Map<String, Object> sqlParams = Maps.newHashMap();
    buildSql(sql, sqlParams, groupId, artifactId, baseVersion);

    try (ODatabaseDocumentTx db = databaseInstanceProvider.get().acquire()) {
      try (StorageTx tx = repository.facet(StorageFacet.class).openTx(db)) {
        final ORID bucketOrid = bucketEntityAdapter.recordIdentity(tx.getBucket());
        sqlParams.put("bucket", bucketOrid);
      }
      final Worker worker = new Worker(db, repository, update, sql.toString(), sqlParams);
      worker.rebuildMetadata();
    }
  }

  /**
   * Builds up SQL and populates parameters map for it based on passed in parameters.
   */
  private void buildSql(final StringBuilder sql,
                        final Map<String, Object> sqlParams,
                        @Nullable final String groupId,
                        @Nullable final String artifactId,
                        @Nullable final String baseVersion)
  {
    sql.append(
        "SELECT " +
            "group as groupId, " +
            "name as artifactId, " +
            "set(attributes.maven2." + MavenAttributes.P_BASE_VERSION + ") as baseVersions " +
            "FROM component WHERE bucket=:bucket"
    );
    if (!Strings.isNullOrEmpty(groupId)) {
      sql.append(" and group=:groupId");
      sqlParams.put("groupId", groupId);
      if (!Strings.isNullOrEmpty(artifactId)) {
        sql.append(" and name=:artifactId");
        sqlParams.put("artifactId", artifactId);
        if (!Strings.isNullOrEmpty(baseVersion)) {
          sql.append(" and attributes.maven2." + MavenAttributes.P_BASE_VERSION + "=:baseVersion");
          sqlParams.put("baseVersion", baseVersion);
        }
      }
    }
    sql.append(" GROUP BY group, name");
  }

  /**
   * Inner class that encapsulates the work, as metadata builder is stateful.
   */
  private static class Worker
      extends ComponentSupport
  {
    private final ODatabaseDocumentTx db;

    private final String sql;

    private final Map<String, Object> sqlParams;

    private final Repository repository;

    private final StorageFacet storageFacet;

    private final MavenFacet mavenFacet;

    private final MavenPathParser mavenPathParser;

    private final MetadataBuilder metadataBuilder;

    private final MetadataUpdater metadataUpdater;

    public Worker(final ODatabaseDocumentTx db,
                  final Repository repository,
                  final boolean update,
                  final String sql,
                  final Map<String, Object> sqlParams)
    {
      this.db = db;
      this.sql = sql;
      this.sqlParams = sqlParams;
      this.repository = repository;
      this.storageFacet = repository.facet(StorageFacet.class);
      this.mavenFacet = repository.facet(MavenFacet.class);
      this.mavenPathParser = mavenFacet.getMavenPathParser();
      this.metadataBuilder = new MetadataBuilder();
      this.metadataUpdater = new MetadataUpdater(update, repository);
    }

    /**
     * Method rebuilding metadata that performs the group level processing. It uses memory conservative "async" SQL
     * approach, and calls {@link #rebuildMetadataInner(String, String, Set)} method as results are arriving.
     */
    public void rebuildMetadata()
    {
      db.command(
          new OSQLAsynchQuery<ODocument>(
              sql,
              new OCommandResultListener()
              {
                String currentGroupId = null;

                @Override
                public boolean result(Object iRecord) {
                  final ODocument doc = (ODocument) iRecord;
                  final String groupId = doc.field("groupId", OType.STRING);
                  final String artifactId = doc.field("artifactId", OType.STRING);
                  final Set<String> baseVersions = doc.field("baseVersions", OType.EMBEDDEDSET);

                  final boolean groupChange = !Objects.equals(currentGroupId, groupId);
                  if (groupChange) {
                    if (currentGroupId != null) {
                      rebuildMetadataExitGroup(currentGroupId);
                    }
                    currentGroupId = groupId;
                    metadataBuilder.onEnterGroupId(groupId);
                  }
                  rebuildMetadataInner(groupId, artifactId, baseVersions);
                  return true;
                }

                @Override
                public void end() {
                  if (currentGroupId != null) {
                    rebuildMetadataExitGroup(currentGroupId);
                  }
                }
              }
          )
      ).execute(sqlParams);
    }

    /**
     * Process exits from group level, executed in isolation.
     */
    private void rebuildMetadataExitGroup(final String currentGroupId) {
      try (StorageTx tx = storageFacet.openTx(db)) {
        metadataUpdater.processMetadata(
            tx,
            metadataMavenPath(currentGroupId, null, null),
            metadataBuilder.onExitGroupId()
        );
        tx.commit();
      }
    }

    /**
     * Method rebuilding metadata that performs artifact and baseVersion processing. While it is called from {@link
     * #rebuildMetadata()} method, it will use a separate TX/DB to perform writes, it does NOT
     * accept the TX from caller. Executed in isolation.
     */
    private void rebuildMetadataInner(final String groupId,
                                      final String artifactId,
                                      final Set<String> baseVersions)
    {
      metadataBuilder.onEnterArtifactId(artifactId);
      for (final String baseVersion : baseVersions) {
        metadataBuilder.onEnterBaseVersion(baseVersion);
        try (StorageTx tx = storageFacet.openTx(db)) {
          final Iterable<Component> components = tx.findComponents(
              "group = :groupId and name = :artifactId and attributes.maven2." + MavenAttributes.P_BASE_VERSION +
                  " = :baseVersion",
              ImmutableMap.<String, Object>of(
                  "groupId", groupId,
                  "artifactId", artifactId,
                  "baseVersion", baseVersion
              ),
              ImmutableList.of(repository),
              null // order by
          );
          for (Component component : components) {
            for (Asset asset : tx.browseAssets(component)) {
              final MavenPath mavenPath = mavenPathParser.parsePath(
                  asset.formatAttributes().require(StorageFacet.P_PATH, String.class)
              );
              if (mavenPath.isSubordinate()) {
                continue;
              }
              metadataBuilder.addArtifactVersion(mavenPath);
              mayUpdateChecksum(tx, asset, mavenPath, HashType.SHA1);
              mayUpdateChecksum(tx, asset, mavenPath, HashType.MD5);
              if (mavenPath.isPom()) {
                final Xpp3Dom pom = getModel(tx, mavenPath);
                if (pom != null) {
                  final String packaging = getChildValue(pom, "packaging", "jar");
                  log.debug("POM packaging: {}", packaging);
                  if ("maven-plugin".equals(packaging)) {
                    metadataBuilder.addPlugin(getPluginPrefix(tx, mavenPath.locateMainArtifact("jar")), artifactId,
                        getChildValue(pom, "name", null));
                  }
                }
              }
            }
          }
          metadataUpdater.processMetadata(
              tx,
              metadataMavenPath(groupId, artifactId, baseVersion),
              metadataBuilder.onExitBaseVersion()
          );
          tx.commit();
        }
      }
      try (StorageTx tx = storageFacet.openTx(db)) {
        metadataUpdater.processMetadata(
            tx,
            metadataMavenPath(groupId, artifactId, null),
            metadataBuilder.onExitArtifactId()
        );
        tx.commit();
      }
    }

    /**
     * Verifies and may fix/create the broken/non-existent Maven hashes (.sha1/.md5 files).
     */
    private void mayUpdateChecksum(final StorageTx tx, final Asset asset, final MavenPath mavenPath,
                                   final HashType hashType)
    {
      final AttributesMap checksums = asset.attributes().child(StorageFacet.P_CHECKSUM);
      final String assetChecksum = (String) checksums.get(hashType.getHashAlgorithm().name());
      if (Strings.isNullOrEmpty(assetChecksum)) {
        // this means that an asset stored in maven repository lacks checksum required by maven repository (see maven facet)
        log.warn("Asset with path {} lacks checksum {}", mavenPath, hashType);
        return;
      }
      final MavenPath checksumPath = mavenPath.hash(hashType);
      try {
        final Content content = mavenFacet.get(tx, checksumPath);
        if (content != null) {
          try (InputStream is = content.openInputStream()) {
            final String mavenChecksum = DigestExtractor.extract(is);
            if (Objects.equals(assetChecksum, mavenChecksum)) {
              return; // all is OK: exists and matches
            }
          }
        }
      }
      catch (IOException e) {
        log.warn("Error reading {}", checksumPath, e);
      }
      // we need to generate/write it
      try {
        final StringPayload mavenChecksum = new StringPayload(assetChecksum, Constants.CHECKSUM_CONTENT_TYPE);
        mavenFacet.put(tx, checksumPath, mavenChecksum);
      }
      catch (IOException e) {
        log.warn("Error writing {}", checksumPath, e);
        throw Throwables.propagate(e);
      }
    }

    /**
     * Assembles {@link MavenPath} for repository metadata out of groupId, artifactId and baseVersion.
     */
    private MavenPath metadataMavenPath(final String groupId,
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
      sb.append("/").append(Constants.METADATA_FILENAME);
      return mavenPathParser.parsePath(sb.toString());
    }

    /**
     * Reads and parses Maven POM.
     */
    @Nullable
    private Xpp3Dom getModel(final StorageTx tx, final MavenPath mavenPath) {
      // sanity checks: is artifact and extension is "pom", only possibility for maven POM currently
      checkArgument(mavenPath.isPom(), "Not a pom path: %s", mavenPath);
      try {
        final Content pomContent = mavenFacet.get(tx, mavenPath);
        if (pomContent != null) {
          return parse(mavenPath, pomContent.openInputStream());
        }
      }
      catch (Exception e) {
        throw Throwables.propagate(e);
      }
      return null;
    }

    /**
     * Parses the DOM of a XML.
     */
    private Xpp3Dom parse(final MavenPath mavenPath, final InputStream is) {
      try (InputStreamReader reader = new InputStreamReader(is, Charsets.UTF_8)) {
        return Xpp3DomBuilder.build(reader);
      }
      catch (XmlPullParserException e) {
        log.debug("Could not parse POM: {}", mavenPath.getPath(), e);
      }
      catch (Exception e) {
        log.debug("Could not parse POM: {}", mavenPath.getPath(), e);
        throw Throwables.propagate(e);
      }
      return null;
    }

    /**
     * Returns the plugin prefix of a Maven plugin, by opening up the plugin JAR, and reading the Maven Plugin
     * Descriptor. If fails, falls back to mangle artifactId (ie. extract XXX from XXX-maven-plugin or
     * maven-XXX-plugin).
     */
    private String getPluginPrefix(final StorageTx tx, final MavenPath mavenPath) {
      // sanity checks: is artifact and extension is "jar", only possibility for maven plugins currently
      checkArgument(mavenPath.getCoordinates() != null);
      checkArgument(Objects.equals(mavenPath.getCoordinates().getExtension(), "jar"));
      String prefix = null;
      try {
        final Content jarFile = mavenFacet.get(tx, mavenPath);
        if (jarFile != null) {
          try (ZipInputStream zip = new ZipInputStream(jarFile.openInputStream())) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
              if (!entry.isDirectory() && "META-INF/maven/plugin.xml".equals(entry.getName())) {
                final Xpp3Dom dom = parse(mavenPath, zip);
                prefix = getChildValue(dom, "goalPrefix", null);
                break;
              }
              zip.closeEntry();
            }
          }
        }
      }
      catch (Exception e) {
        log.debug("Unable to read plugin.xml of {}", mavenPath, e);
      }
      if (prefix != null) {
        return prefix;
      }
      if ("maven-plugin-plugin".equals(mavenPath.getCoordinates().getArtifactId())) {
        return "plugin";
      }
      else {
        return mavenPath.getCoordinates().getArtifactId().replaceAll("-?maven-?", "").replaceAll("-?plugin-?", "");
      }
    }

    /**
     * Helper method to get node's immediate child or default.
     */
    private String getChildValue(final Xpp3Dom doc, final String childName, final String defaultValue) {
      Xpp3Dom child = doc.getChild(childName);
      if (child == null) {
        return defaultValue;
      }
      return child.getValue();
    }
  }
}
