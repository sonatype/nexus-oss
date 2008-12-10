/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.plugins.rest;

import java.util.List;
import java.util.Map;

/**
 * A Resource bundle meant for extending/contributing/spoofing existing resources (JS, CSS, Images, etc) of the Nexus
 * Web Application. This component is able only to contribute static resources, if you want to extends REST API, please
 * see PlexusResource.
 * <p>
 * The getHeadContribution() and getBodyContribution() methods should return strings that will finally make into
 * "index"html" (if not overridden that too by some other resource!). This enables you to simply add new JS files to the
 * index.html header, or add custom parts to the HEAD and BODY section of the index.html.
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
 * <p>
 * Both methods is getting the context map used to evaluate the templates, so they can easily affect the context if
 * needed.
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
     * A header contribution is a HTML snippet, that will be injected into the beginning of HEAD section of the
     * index.html. The snippet will be processed by Velocity, so it can be a Velocity template too. The context passed
     * in may be modified by this bundle, and it will be finally used to evaluate the template returned by this call.
     * 
     * @return
     */
    String getPreHeadContribution( Map<String, Object> context );

    /**
     * A header contribution is a HTML snippet, that will be injected into the end of HEAD section of the index.html.
     * The snippet will be processed by Velocity, so it can be a Velocity template too. The context passed in may be
     * modified by this bundle, and it will be finally used to evaluate the template returned by this call.
     * 
     * @return
     */
    String getPostHeadContribution( Map<String, Object> context );

    /**
     * A header contribution is a HTML snippet, that will be injected into the beginning of BODY section of the
     * index.html. The snippet will be processed by Velocity, so it can be a Velocity template too.The context passed in
     * may be modified by this bundle, and it will be finally used to evaluate the template returned by this call.
     * 
     * @return
     */
    String getPreBodyContribution( Map<String, Object> context );

    /**
     * A header contribution is a HTML snippet, that will be injected into the end of BODY section of the index.html.
     * The snippet will be processed by Velocity, so it can be a Velocity template too. The context passed in may be
     * modified by this bundle, and it will be finally used to evaluate the template returned by this call.
     * 
     * @return
     */
    String getPostBodyContribution( Map<String, Object> context );
}
