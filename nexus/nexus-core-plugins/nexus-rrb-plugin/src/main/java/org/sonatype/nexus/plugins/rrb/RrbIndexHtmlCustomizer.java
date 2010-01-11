package org.sonatype.nexus.plugins.rrb;

import java.util.Map;

import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

public class RrbIndexHtmlCustomizer extends AbstractNexusIndexHtmlCustomizer implements NexusIndexHtmlCustomizer {

    @Override
    public String getPostHeadContribution(Map<String, Object> ctx) {
        String version = getVersionFromJarFile("/META-INF/maven/org.sonatype.nexus.plugins/nexus-rrb-plugin/pom.properties");

        return "<script src=\"js/repoServer/nexus-rrb-plugin-all.js" + (version == null ? "" : "?" + version)
                + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";
    }
}
