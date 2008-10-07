package org.sonatype.nexus.plugins.rest;

public interface NexusResourceBundle
{
    String getDescription();

    Resource getResource( String path );

    /**
     * A header contribution is a HTML snippet, that will be injected into the HEAD section of the index.html. The
     * following placeholders will be evaluated if found within the returned string:
     * 
     * <pre>${serviceBase}</pre>
     * 
     * - the path to services base (local)
     * 
     * <pre>${contentBase}</pre>
     * 
     * - the path to the contents base
     * 
     * <pre>${pluginResourcesBase}</pre>
     * 
     * - the path to the published resources of this plugin.
     * 
     * <pre>${pluginServicesBase}</pre>
     * 
     * - the path to the API of this plugin
     * 
     * @return
     */
    String getHeadContribution();

    /**
     * A body contribution is a HTML snippet, that will be injected into the BODY section of the index.html. The
     * following placeholders will be evaluated if found within the returned string:
     * 
     * <pre>${serviceBase}</pre>
     * 
     * - the path to services base (local)
     * 
     * <pre>${contentBase}</pre>
     * 
     * - the path to the contents base
     * 
     * <pre>${pluginResourcesBase}</pre>
     * 
     * - the path to the published resources of this plugin.
     * 
     * <pre>${pluginServicesBase}</pre>
     * 
     * - the path to the API of this plugin
     * 
     * @return
     */
    String getBodyContribution();
}
