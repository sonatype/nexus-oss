/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.siesta;

import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Contributes Javascript support for Siesta.
 *
 * @since 2.4
 */
@Named
@Singleton
public class NexusIndexHtmlCustomizerImpl
    extends AbstractNexusIndexHtmlCustomizer
{
    public static final String GROUP_ID = "org.sonatype.nexus.plugins";

    public static final String ARTIFACT_ID = "nexus-siesta-plugin";

    private String getUrlSuffix() {
        String version = getVersionFromJarFile(String.format("/META-INF/maven/%s/%s/pom.properties", GROUP_ID, ARTIFACT_ID));
        if (version == null) {
            return "";
        }
        else if (version.endsWith("SNAPSHOT")) {
            return String.format("?v=%s&t=%s", version, System.currentTimeMillis());
        }
        else {
            return "?v=" + version;
        }
    }

    @Override
    public String getPostHeadContribution(final Map<String, Object> ctx) {
        return String.format("<script src='static/js/%s-all.js%s' type='text/javascript' charset='UTF-8'></script>", ARTIFACT_ID, getUrlSuffix());
    }
}
