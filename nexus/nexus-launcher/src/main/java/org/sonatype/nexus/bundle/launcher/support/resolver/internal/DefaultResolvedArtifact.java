/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.bundle.launcher.support.resolver.internal;

import com.google.common.base.Preconditions;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.nexus.bundle.launcher.support.resolver.ResolvedArtifact;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * Immutable resolved artifact.
 *
 * @since 1.9.3
 */
public class DefaultResolvedArtifact implements ResolvedArtifact {

    DefaultResolvedArtifact(final Artifact artifact) {
        Preconditions.checkNotNull(artifact);
        this.artifact = artifact;
    }

    private Artifact artifact;

    @Override
    public String getGroupId() {
        return this.artifact.getGroupId();
    }

    @Override
    public String getArtifactId() {
        return this.artifact.getArtifactId();
    }

    @Override
    public String getVersion() {
        return this.artifact.getVersion();
    }

    @Override
    public String getBaseVersion() {
        return this.artifact.getBaseVersion();
    }

    @Override
    public boolean isSnapshot() {
        return this.artifact.isSnapshot();
    }

    @Override
    public String getClassifier() {
        return this.artifact.getClassifier();
    }

    @Override
    public String getExtension() {
        return this.artifact.getExtension();
    }

    @Override
    public File getFile() {
        return this.artifact.getFile();
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return this.artifact.getProperty(key, defaultValue);
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(this.artifact.getProperties());
    }

}
