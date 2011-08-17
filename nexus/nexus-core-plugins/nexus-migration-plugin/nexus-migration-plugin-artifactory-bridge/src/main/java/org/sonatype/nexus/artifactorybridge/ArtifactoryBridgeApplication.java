/**
 * Copyright (c) 2008-2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.artifactorybridge;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Application;
import org.restlet.Router;
import org.sonatype.plexus.rest.PlexusResourceFinder;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;

@Component( role = Application.class, hint = "artifactoryBridge" )
public class ArtifactoryBridgeApplication
    extends PlexusRestletApplicationBridge
{

    @Requirement( role = ArtifactoryRedirectorPlexusResource.class )
    private ArtifactoryRedirectorPlexusResource artifactoryRedirector;

    @Override
    protected void doCreateRoot( Router root, boolean isStarted )
    {
        attach( root, false, "", new PlexusResourceFinder( getContext(), artifactoryRedirector ) );
        attach( root, false, "/", new PlexusResourceFinder( getContext(), artifactoryRedirector ) );
    }
}
