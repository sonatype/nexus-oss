/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.plugin.migration.artifactory.security;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.realms.tools.AbstractStaticSecurityResource;
import org.sonatype.jsecurity.realms.tools.StaticSecurityResource;

@Component( role = StaticSecurityResource.class, hint = "ArtifactoryMigrationSecurityResource" )
public class ArtifactoryMigrationSecurityResource
    extends AbstractStaticSecurityResource
    implements StaticSecurityResource
{

    public String getResourcePath()
    {
        return "/META-INF/nexus-artifactory-migration-plugin-security.xml";
    }

    public Configuration getConfiguration()
    {
        return null;
    }

}
