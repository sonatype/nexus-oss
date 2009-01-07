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
package org.sonatype.nexus.plugin.migration.artifactory.security.builder;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactorySecurityConfig;

public abstract class AbstractSecurityConfigParser
{
    private Xpp3Dom dom;

    private ArtifactorySecurityConfig config;

    public AbstractSecurityConfigParser( Xpp3Dom dom, ArtifactorySecurityConfig config )
    {
        this.dom = dom;

        this.config = config;
    }

    protected Xpp3Dom getDom()
    {
        return dom;
    }

    protected ArtifactorySecurityConfig getConfig()
    {
        return config;
    }

    abstract protected void parseUsers();

    abstract protected void parseGroups();
    
    abstract protected void parsePermissionTargets();

    abstract protected void parseAcls();
    
    public void parse()
    {
        // NOTE that this order is critical
        parseGroups();
        
        parseUsers();
        
        parsePermissionTargets();
        
        parseAcls();
    }
}
