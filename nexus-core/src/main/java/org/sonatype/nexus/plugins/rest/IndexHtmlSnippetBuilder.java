/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugins.rest;

import com.google.common.collect.Lists;
import org.sonatype.nexus.logging.AbstractLoggingComponent;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper to build HTML snippets for use in {@link NexusIndexHtmlCustomizer} implementations.
 *
 * @since 2.4
 */
public class IndexHtmlSnippetBuilder
    extends AbstractLoggingComponent
{
    private final Object owner;

    private final String groupId;

    private final String artifactId;

    private final List<String> styleRefs = Lists.newArrayList();

    private final List<String> scriptRefs = Lists.newArrayList();

    private String encoding = "UTF-8";

    public IndexHtmlSnippetBuilder(final Object owner, final String groupId, final String artifactId) {
        this.owner = checkNotNull(owner);
        this.groupId = checkNotNull(groupId);
        this.artifactId = checkNotNull(artifactId);
    }

    public IndexHtmlSnippetBuilder styleRef(final String fileName) {
        checkNotNull(fileName);
        styleRefs.add(fileName);
        return this;
    }

    public IndexHtmlSnippetBuilder defaultStyleRef() {
        return styleRef(String.format("static/css/%s-all.css", artifactId));
    }

    public IndexHtmlSnippetBuilder scriptRef(final String fileName) {
        checkNotNull(fileName);
        scriptRefs.add(fileName);
        return this;
    }

    public IndexHtmlSnippetBuilder defaultScriptRef() {
        return scriptRef(String.format("static/js/%s-all.js", artifactId));
    }

    public IndexHtmlSnippetBuilder encoding(final String encoding) {
        this.encoding = checkNotNull(encoding);
        return this;
    }

    /**
     * Attempt to detect version from the POM of owner.
     */
    private String detectVersion() {
        Properties props = new Properties();

        String path = String.format("/META-INF/maven/%s/%s/pom.properties", groupId, artifactId);
        InputStream input = owner.getClass().getResourceAsStream(path);

        if (input == null) {
            getLogger().warn("Unable to detect version; failed to load: {}", path);
            return null;
        }

        try {
            props.load(input);
        }
        catch (IOException e) {
            getLogger().warn("Failed to load POM: {}", path, e);
            return null;
        }

        return props.getProperty("version");
    }

    /**
     * Return a string suitable for use as suffix to a plain-URL to enforce version/caching semantics.
     */
    private String getUrlSuffix() {
        String version = detectVersion();
        if (version == null) {
            return "";
        }
        else if (version.endsWith("SNAPSHOT")) {
            // append timestamp for SNAPSHOT versions to help sort out cache problems
            return String.format("?v=%s&t=%s", version, System.currentTimeMillis());
        }
        else {
            return "?v=" + version;
        }
    }

    public String build() {
        StringBuilder buff = new StringBuilder();
        String suffix = getUrlSuffix();

        for (String style : styleRefs) {
            buff.append(String.format("<link rel='stylesheet' href='%s%s' type='text/css' media='screen' charset='%s'/>", style, suffix, encoding));
            buff.append("\n");
        }

        for (String script : scriptRefs) {
            buff.append(String.format("<script src='%s%s' type='text/javascript' charset='%s'></script>", script, suffix, encoding));
            buff.append("\n");
        }

        return buff.toString();
    }
}
