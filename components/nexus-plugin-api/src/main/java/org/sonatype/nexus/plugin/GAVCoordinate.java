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
package org.sonatype.nexus.plugin;

import javax.annotation.Nullable;

import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Maven GAV coordinates.
 */
@Deprecated
final class GAVCoordinate
{
  private final String groupId;

  private final String artifactId;

  private final String version;

  private final String classifier;

  private final String type;

  GAVCoordinate(final String groupId, final String artifactId, final String version) {
    this(groupId, artifactId, version, null, null);
  }

  GAVCoordinate(final String groupId,
                final String artifactId,
                final String version,
                @Nullable final String classifier,
                @Nullable final String type)
  {
    this.groupId = checkNotNull(groupId);
    this.artifactId = checkNotNull(artifactId);
    this.version = checkNotNull(version);
    this.classifier = classifier;
    this.type = type;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }

  @Nullable
  public String getClassifier() {
    return classifier;
  }

  @Nullable
  public String getType() {
    return type;
  }

  @Override
  public boolean equals(final Object rhs) {
    if (this == rhs) {
      return true;
    }
    if (!(rhs instanceof GAVCoordinate)) {
      return false;
    }
    return toString().equals(rhs.toString());
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public String toString() {
    final StringBuilder buf = new StringBuilder();
    buf.append(groupId).append(':').append(artifactId).append(':').append(version);
    final boolean haveType = Strings.emptyToNull(type) != null;
    if (Strings.emptyToNull(classifier) != null) {
      buf.append(':').append(classifier);
    }
    else if (haveType) {
      buf.append(':');
    }
    if (haveType) {
      buf.append(':').append(type);
    }
    return buf.toString();
  }
}
