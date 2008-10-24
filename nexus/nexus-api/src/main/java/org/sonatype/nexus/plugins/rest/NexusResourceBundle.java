package org.sonatype.nexus.plugins.rest;

import java.util.List;

/**
 * A Resource bundle meant for extending/contributing/spoofing existing resources (JS, CSS, Images, etc) of the Nexus
 * Web Application. This component is able only to contribute static resources, if you want to extends REST API, please
 * see PlexusResource.
 * <p>
 * The getHeadContribution() and getBodyContribution() methods should return strings that will finally make into
 * "index"html" (if not overridden that too!). This enables you to simply add new JS files to the index.html header, or
 * add custom parts to the HEAD and BODY section of the index.html.
 * <p>
 * The returned String of the above mentioned methods will be processed by a Velocity Engine, so you are able to return
 * plain HTML but also arbitrary Velocity template. The following context variables are available:
 * <ul>
 * <li>${serviceBase} - the path to local services base ("service/local" by default)</li>
 * <li>${contentBase} - the path to content base ("content" by default)</li>
 * <li>${nexusRoot} - the root path of Nexus Web Application</li>
 * <li>${bundle} - the instance of this ResourceBundle (the contributor, this instance)</li>
 * <li>${nexusVersion} - the version of Nexus that currently runs, see NEXUS-932</li>
 * </ul>
 * 
 * @author cstamas
 */
public interface NexusResourceBundle
{
    /**
     * Returns the list of static resources.
     * 
     * @return
     */
    List<StaticResource> getContributedResouces();

    /**
     * A header contribution is a HTML snippet, that will be injected into the HEAD section of the index.html. The
     * snippet will be processed by Velocity, so it can be a Velocity template too.
     * 
     * @return
     */
    String getHeadContribution();

    /**
     * A header contribution is a HTML snippet, that will be injected into the HEAD section of the index.html. The
     * snippet will be processed by Velocity, so it can be a Velocity template too.
     * 
     * @return
     */
    String getBodyContribution();
}
