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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.maven.MavenPath;
import org.sonatype.nexus.repository.maven.MavenPath.Coordinates;
import org.sonatype.nexus.repository.maven.internal.maven2.metadata.Maven2Metadata.Plugin;
import org.sonatype.nexus.repository.maven.internal.maven2.metadata.Maven2Metadata.Snapshot;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionScheme;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Maven 2 repository metadata builder.
 *
 * @since 3.0
 */
public class MetadataBuilder
    extends ComponentSupport
{
  private final VersionScheme versionScheme;

  private String groupId;

  private String artifactId;

  private String baseVersion;

  // G level

  private final List<Plugin> plugins;

  // A level

  private final TreeSet<Version> baseVersions;

  // V level

  private final Map<String, VersionCoordinates> latestVersionCoordinatesMap;

  private VersionCoordinates latestVersionCoordinates;

  public MetadataBuilder() {
    this.versionScheme = new GenericVersionScheme();
    // G
    this.plugins = Lists.newArrayList();
    // A
    this.baseVersions = Sets.newTreeSet();
    // V
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

  public boolean onEnterGroupId(final String groupId) {
    if (setGroupId(groupId)) {
      plugins.clear();
      log.debug("-> G: {}", getGroupId());
      return true;
    }
    return false;
  }

  @Nullable
  public Maven2Metadata onExitGroupId() {
    checkState(getGroupId() != null, "groupId");
    log.debug("<- G: {}", getGroupId());
    if (plugins.isEmpty()) {
      log.debug("No plugins in group: {}", getGroupId());
      return null;
    }
    return Maven2Metadata.newGroupLevel(DateTime.now(), getGroupId(), plugins);
  }

  public boolean addPlugin(final String prefix, final String artifactId, final String name) {
    final Plugin plugin = Maven2Metadata.newPlugin(artifactId, prefix, name);
    final Iterator<Plugin> pi = plugins.iterator();
    while (pi.hasNext()) {
      final Plugin p = pi.next();
      if (plugin.equals(p)) {
        return false; // bail out, is present already
      }
      if (plugin.keyEquals(p)) {
        pi.remove(); // remove it, will add it below
        break;
      }
    }
    plugins.add(plugin);
    log.debug("Added plugin {}:{} prefix:{}", getGroupId(), artifactId, prefix);
    return true;
  }

  // -----------------------------------
  // artifactId

  public boolean onEnterArtifactId(final String artifactId) {
    if (setArtifactId(artifactId)) {
      log.debug("-> GA: {}:{}", getGroupId(), getArtifactId());
      baseVersions.clear();
      return true;
    }
    return false;
  }

  @Nullable
  public Maven2Metadata onExitArtifactId() {
    checkState(getArtifactId() != null, "artifactId");
    log.debug("<- GA: {}:{}", getGroupId(), getArtifactId());
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
    return Maven2Metadata.newArtifactLevel(
        DateTime.now(),
        getGroupId(),
        getArtifactId(),
        latest,
        release,
        Iterables.transform(baseVersions, new Function<Version, String>()
        {
          @Override
          public String apply(final Version input) {
            return input.toString();
          }
        }));
  }

  private void addBaseVersion(final String baseVersion) {
    checkNotNull(baseVersion, "baseVersion");
    try {
      if (baseVersions.add(versionScheme.parseVersion(baseVersion))) {
        log.debug("Added base version {}:{}:{}", getGroupId(), getArtifactId(), baseVersion);
      }
    }
    catch (InvalidVersionSpecificationException e) {
      // nada
    }
  }

  // -----------------------------------
  // baseVersion

  /**
   * Internal structure to hold parsed Aether {@link Version} and {@link Coordinates}.
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

  public boolean onEnterBaseVersion(final String baseVersion) {
    if (setBaseVersion(baseVersion)) {
      log.debug("-> GAbV: {}:{}:{}", getGroupId(), getArtifactId(), getBaseVersion());
      latestVersionCoordinatesMap.clear();
      latestVersionCoordinates = null;
      return true;
    }
    return false;
  }

  @Nullable
  public Maven2Metadata onExitBaseVersion() {
    checkState(getBaseVersion() != null, "baseVersion");
    log.debug("<- GAbV: {}:{}:{}", getGroupId(), getArtifactId(), getBaseVersion());
    if (!getBaseVersion().endsWith("SNAPSHOT") || latestVersionCoordinates == null) {
      // release version does not have version-level metadata
      log.debug("Not a snapshot or nothing to generate: {}:{}:{}", getGroupId(), getArtifactId(), getBaseVersion());
      return null;
    }
    final List<Snapshot> snapshots = Lists.newArrayList();
    for (VersionCoordinates versionCoordinates : latestVersionCoordinatesMap.values()) {
      final Coordinates coordinates = versionCoordinates.coordinates;
      final Snapshot snapshotVersion = Maven2Metadata.newSnapshot(
          new DateTime(coordinates.getTimestamp()),
          coordinates.getExtension(),
          coordinates.getClassifier(),
          coordinates.getVersion()
      );
      snapshots.add(snapshotVersion);
    }
    return Maven2Metadata.newVersionLevel(
        DateTime.now(),
        getGroupId(),
        getArtifactId(),
        getBaseVersion(),
        latestVersionCoordinates.coordinates.getTimestamp(),
        latestVersionCoordinates.coordinates.getBuildNumber(),
        snapshots
    );
  }

  public void addArtifactVersion(final MavenPath mavenPath) {
    checkNotNull(mavenPath, "mavenPath");
    if (mavenPath.isSubordinate() || mavenPath.getCoordinates() == null) {
      return;
    }
    checkState(Objects.equals(getGroupId(), mavenPath.getCoordinates().getGroupId()));
    checkState(Objects.equals(getArtifactId(), mavenPath.getCoordinates().getArtifactId()));
    checkState(Objects.equals(getBaseVersion(), mavenPath.getCoordinates().getBaseVersion()));

    log.debug("Discovered {}:{}:{}:{}:{}",
        mavenPath.getCoordinates().getGroupId(),
        mavenPath.getCoordinates().getArtifactId(),
        mavenPath.getCoordinates().getVersion(),
        mavenPath.getCoordinates().getClassifier(),
        mavenPath.getCoordinates().getExtension());

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
