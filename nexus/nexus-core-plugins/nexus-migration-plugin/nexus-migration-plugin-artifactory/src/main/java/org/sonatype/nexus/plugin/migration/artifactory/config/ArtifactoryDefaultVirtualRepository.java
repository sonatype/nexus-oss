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
package org.sonatype.nexus.plugin.migration.artifactory.config;

import java.util.Collections;
import java.util.List;

public class ArtifactoryDefaultVirtualRepository
    extends ArtifactoryVirtualRepository
{

    private List<String> allRepositories;

    public ArtifactoryDefaultVirtualRepository( List<String> allRepositories )
    {
        super( null );
        this.allRepositories = Collections.unmodifiableList( allRepositories );
    }

    @Override
    public String getKey()
    {
        return "repo";
    }

    @Override
    public List<String> getRepositories()
    {
        return allRepositories;
    }

}
