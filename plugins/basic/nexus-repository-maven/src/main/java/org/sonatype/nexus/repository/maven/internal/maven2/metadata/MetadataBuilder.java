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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.sonatype.nexus.repository.maven.internal.MavenPath;
import org.sonatype.nexus.repository.maven.internal.MavenPath.Coordinates;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionScheme;
import org.elasticsearch.common.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Maven 2 repository metadata builder.
 *
 * @since 3.0
 */
@NotThreadSafe
public class MetadataBuilder
    extends ComponentSupport
{
  private final VersionScheme versionScheme;

  private String groupId;

  private String artifactId;

  private String baseVersion;

  public MetadataBuilder() {
    this.versionScheme = new GenericVersionScheme();
    // G
    this.plugins = Lists.newArrayList();
    // A
    this.baseVersions = Sets.newTreeSet();
    // V
    this.dotlessTimestampFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    this.dotlessTimestampFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    this.dottedTimestampFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
    this.dottedTimestampFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    this.latestVersionCoordinatesMap = Maps.newHashMap();
  }

  // -----------------------------------
  // context

  public String getGroupId() {
    return groupId;
  }

  private boolean setGroupId(final String groupId) {
    checkNotNull(groupId, "groupId");
    if (Objects.equals(groupId, this.getGroupId())) {
      return false;
    }
    this.groupId = groupId;
    this.artifactId = null;
    this.baseVersion = null;
    return true;
  }

  public String getArtifactId() {
    return artifactId;
  }

  private boolean setArtifactId(final String artifactId) {
    checkState(groupId != null, "groupId == null");
    checkNotNull(artifactId, "artifactId");
    if (Objects.equals(artifactId, this.getArtifactId())) {
      return false;
    }
    this.artifactId = artifactId;
    this.baseVersion = null;
    return true;
  }

  public String getBaseVersion() {
    return baseVersion;
  }

  private boolean setBaseVersion(final String baseVersion) {
    checkState(groupId != null, "groupId == null");
    checkState(artifactId != null, "artifactId == null");
    checkNotNull(baseVersion, "baseVersion");
    if (Objects.equals(baseVersion, this.getBaseVersion())) {
      return false;
    }
    this.baseVersion = baseVersion;
    return true;
  }

  // -----------------------------------
  // groupId

  private final List<Plugin> plugins;

  public boolean onEnterGroupId(final String groupId) {
    if (setGroupId(groupId)) {
      plugins.clear();
      return true;
    }
    return false;
  }

  @Nullable
  public Metadata onExitGroupId() {
    checkState(getGroupId() != null, "groupId");
    if (plugins.isEmpty()) {
      log.debug("No plugins in group: {}:{}", getGroupId());
      return null;
    }
    final Metadata result = new Metadata();
    result.setModelVersion("1.1.0");
    result.setGroupId(getGroupId());
    result.getPlugins().addAll(plugins);
    return result;
  }

  public boolean addPlugin(final String prefix, final String artifactId, final String name) {
    checkNotNull(prefix, "prefix");
    checkNotNull(artifactId, "artifactId");
    final Plugin plugin = new Plugin();
    plugin.setPrefix(prefix);
    plugin.setArtifactId(artifactId);
    plugin.setName(Strings.isNullOrEmpty(name) ? artifactId : name);
    for (Plugin existing : plugins) {
      if (Objects.equals(plugin.getArtifactId(), existing.getArtifactId())
          && Objects.equals(plugin.getPrefix(), existing.getPrefix())) {
        existing.setName(plugin.getName()); // update name, else is OK
        return false;
      }
    }
    plugins.add(plugin);
    return true;
  }

  // -----------------------------------
  // artifactId

  private final TreeSet<Version> baseVersions;

  public boolean onEnterArtifactId(final String artifactId) {
    if (setArtifactId(artifactId)) {
      baseVersions.clear();
      return true;
    }
    return false;
  }

  @Nullable
  public Metadata onExitArtifactId() {
    checkState(getArtifactId() != null, "artifactId");
    if (baseVersions.isEmpty()) {
      log.debug("Nothing to generate: {}:{}", getGroupId(), getArtifactId());
      return null;
    }
    Iterator<Version> vi = baseVersions.descendingIterator();
    String latest = vi.next().toString();
    String release = latest;
    while (release.contains("SNAPSHOT") && vi.hasNext()) {
      release = vi.next().toString();
    }
    if (release.contains("SNAPSHOT")) {
      release = null;
    }
    final Metadata result = new Metadata();
    result.setModelVersion("1.1.0");
    result.setGroupId(getGroupId());
    result.setArtifactId(getArtifactId());
    final Versioning versioning = new Versioning();
    versioning.setLatest(latest);
    versioning.setRelease(release);
    versioning.setVersions(
        Lists.newArrayList(
            Iterables.transform(baseVersions, new Function<Version, String>()
            {
              @Override
              public String apply(final Version input) {
                return input.toString();
              }
            })
        )
    );
    versioning.setLastUpdatedTimestamp(DateTime.now().toDate());
    result.setVersioning(versioning);
    return result;
  }

  private void addBaseVersion(final String baseVersion) {
    checkNotNull(baseVersion, "baseVersion");
    try {
      baseVersions.add(versionScheme.parseVersion(baseVersion));
    }
    catch (InvalidVersionSpecificationException e) {
      // nada
    }
  }

  // -----------------------------------
  // baseVersion

  /**
   * Internal structure to hold parsed version and MavenPath
   */
  private static class VersionCoordinates
  {
    private final Version version;

    private final Coordinates coordinates;

    private VersionCoordinates(final Version version, final Coordinates coordinates) {
      this.version = version;
      this.coordinates = coordinates;
    }
  }

  private final DateFormat dotlessTimestampFormat;

  private final DateFormat dottedTimestampFormat;

  private final Map<String, VersionCoordinates> latestVersionCoordinatesMap;

  private VersionCoordinates latestVersionCoordinates;

  public boolean onEnterBaseVersion(final String baseVersion) {
    if (setBaseVersion(baseVersion)) {
      latestVersionCoordinatesMap.clear();
      latestVersionCoordinates = null;
      return true;
    }
    return false;
  }

  @Nullable
  public Metadata onExitBaseVersion() {
    checkState(getBaseVersion() != null, "baseVersion");
    if (!getBaseVersion().endsWith("SNAPSHOT") || latestVersionCoordinates == null) {
      // release versions does not have version level metadata
      log.debug("Not a snapshot or nothing to generate: {}:{}:{}", getGroupId(), getArtifactId(), getBaseVersion());
      return null;
    }
    final Metadata result = new Metadata();
    result.setModelVersion("1.1.0");
    result.setGroupId(getGroupId());
    result.setArtifactId(getArtifactId());
    result.setVersion(getBaseVersion());
    final Versioning versioning = new Versioning();
    final Snapshot snapshot = new Snapshot();
    // TODO: ignore NPEs as that cannot happen, or just make IDEA happy with these?
    snapshot.setTimestamp(dottedTimestampFormat.format(new Date(latestVersionCoordinates.coordinates.getTimestamp())));
    snapshot.setBuildNumber(latestVersionCoordinates.coordinates.getBuildNumber());
    versioning.setSnapshot(snapshot);
    final List<SnapshotVersion> snapshotVersions = Lists.newArrayList();
    for (VersionCoordinates versionCoordinates : latestVersionCoordinatesMap.values()) {
      final SnapshotVersion snapshotVersion = new SnapshotVersion();
      snapshotVersion.setExtension(versionCoordinates.coordinates.getExtension());
      if (versionCoordinates.coordinates.getClassifier() != null) {
        snapshotVersion.setClassifier(versionCoordinates.coordinates.getClassifier());
      }
      snapshotVersion.setVersion(versionCoordinates.coordinates.getVersion());
      snapshotVersion.setUpdated(
          dotlessTimestampFormat.format(new Date(versionCoordinates.coordinates.getTimestamp()))
      );
      snapshotVersions.add(snapshotVersion);
    }
    versioning.setSnapshotVersions(snapshotVersions);
    versioning.setLastUpdatedTimestamp(DateTime.now().toDate());
    result.setVersioning(versioning);
    return result;
  }

  public void addArtifactVersion(final MavenPath mavenPath) {
    checkNotNull(mavenPath, "mavenPath");
    if (mavenPath.isSubordinate() || mavenPath.getCoordinates() == null) {
      return;
    }
    checkState(Objects.equals(getGroupId(), mavenPath.getCoordinates().getGroupId()));
    checkState(Objects.equals(getArtifactId(), mavenPath.getCoordinates().getArtifactId()));
    checkState(Objects.equals(getBaseVersion(), mavenPath.getCoordinates().getBaseVersion()));

    addBaseVersion(mavenPath.getCoordinates().getBaseVersion());

    if (!mavenPath.getCoordinates().isSnapshot()) {
      return;
    }
    if (Objects.equals(mavenPath.getCoordinates().getBaseVersion(), mavenPath.getCoordinates().getVersion())) {
      log.warn("Non-timestamped snapshot, ignoring it: {}", mavenPath);
      return;
    }

    final String key = key(mavenPath.getCoordinates());
    final Version version = parseVersion(mavenPath.getCoordinates().getVersion());
    final VersionCoordinates versionCoordinates = new VersionCoordinates(version, mavenPath.getCoordinates());

    // maintain latestVersionCoordinates
    if (latestVersionCoordinates == null || latestVersionCoordinates.version.compareTo(version) < 0) {
      latestVersionCoordinates = versionCoordinates;
    }

    // maintain latestVersionCoordinatesMap
    if (!latestVersionCoordinatesMap.containsKey(key)) {
      latestVersionCoordinatesMap.put(key, versionCoordinates);
    }
    else {
      // add if contained version is less than version
      final VersionCoordinates other = latestVersionCoordinatesMap.get(key);
      if (other.version.compareTo(versionCoordinates.version) < 0) {
        latestVersionCoordinatesMap.put(key, versionCoordinates);
      }
    }
  }

  private String key(final Coordinates coordinates) {
    if (coordinates.getClassifier() == null) {
      return coordinates.getExtension();
    }
    else {
      return coordinates.getExtension() + ":" + coordinates.getClassifier();
    }
  }

  private Version parseVersion(final String version) {
    try {
      return versionScheme.parseVersion(version);
    }
    catch (InvalidVersionSpecificationException e) {
      // nada
      throw Throwables.propagate(e); // make IDEA happy wrt NPE
    }
  }
}
