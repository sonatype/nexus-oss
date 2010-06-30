package org.sonatype.nexus.plugins.rest;

/**
 * A special resource bundle that holds static (preferably static HTML) documentation.
 * 
 * @author velo
 * @author cstamas
 */
public interface NexusDocumentationBundle
    extends NexusResourceBundle
{
    /**
     * Returns the plugin ID (artifactId?) of the plugin contaning this resource. This string should obey all rules that
     * are prescribed for Maven3 artifactId validation. It makes the very 1st segment of the documentation URIs.
     * 
     * @return
     */
    String getPluginId();

    /**
     * Returns the "url snippet". It makes possible to do a deeper "partition" within plugin documentation URIs. Used by
     * plugins that may carry multiple documentations (like core doc plugin is). Others should just use defaults
     * (provided in {@link AbstractDocumentationNexusResourceBundle}.
     * 
     * @return
     */
    String getPathPrefix();

    /**
     * Returns human description of the documentation bundle. Used for human consumption only: concise and short
     * description.
     * 
     * @return
     */
    String getDescription();
}
