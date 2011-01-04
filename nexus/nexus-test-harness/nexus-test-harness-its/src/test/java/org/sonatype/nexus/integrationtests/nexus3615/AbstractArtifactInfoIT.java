/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus3615;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryUrlResource;

public abstract class AbstractArtifactInfoIT
    extends AbstractNexusIntegrationTest
{

    public AbstractArtifactInfoIT()
    {
        super();
    }

    public AbstractArtifactInfoIT( String testRepositoryId )
    {
        super( testRepositoryId );
    }

    @Override
    protected void deployArtifacts()
        throws Exception
    {
        super.deployArtifacts();
    
        File pom = getTestFile( "artifact.pom" );
        File jar = getTestFile( "artifact.jar" );
        getDeployUtils().deployUsingPomWithRest( REPO_TEST_HARNESS_REPO, jar, pom, null, null );
        getDeployUtils().deployUsingPomWithRest( REPO_TEST_HARNESS_REPO2, jar, pom, null, null );
        getDeployUtils().deployUsingPomWithRest( REPO_TEST_HARNESS_RELEASE_REPO, jar, pom, null, null );
    }

    protected Iterable<String> getRepositoryId( List<RepositoryUrlResource> repositories )
    {
        List<String> repoIds = new ArrayList<String>();
        for ( RepositoryUrlResource repositoryUrlResource : repositories )
        {
            repoIds.add( repositoryUrlResource.getRepositoryId() );
        }
    
        return repoIds;
    }

}