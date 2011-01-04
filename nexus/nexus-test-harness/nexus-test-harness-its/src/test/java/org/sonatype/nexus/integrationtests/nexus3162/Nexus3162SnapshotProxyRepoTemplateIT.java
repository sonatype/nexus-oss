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
package org.sonatype.nexus.integrationtests.nexus3162;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.test.utils.RepositoryTemplateMessageUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author juven
 */
public class Nexus3162SnapshotProxyRepoTemplateIT
    extends AbstractNexusIntegrationTest
{
    protected RepositoryTemplateMessageUtil messageUtil;

    public Nexus3162SnapshotProxyRepoTemplateIT()
        throws Exception
    {
        this.messageUtil = new RepositoryTemplateMessageUtil();
    }

    @Test
    public void getProxySnapshotTemplate()
        throws Exception
    {
        RepositoryBaseResource result = messageUtil.getTemplate( RepositoryTemplateMessageUtil.TEMPLATE_PROXY_SNAPSHOT );

        Assert.assertTrue( result instanceof RepositoryProxyResource );
        Assert.assertEquals( 1440, ( (RepositoryProxyResource) result ).getArtifactMaxAge() );
        Assert.assertEquals( 1440, ( (RepositoryProxyResource) result ).getMetadataMaxAge() );
    }

    @Test
    public void getProxyReleaseTemplate()
        throws Exception
    {
        RepositoryBaseResource result = messageUtil.getTemplate( RepositoryTemplateMessageUtil.TEMPLATE_PROXY_RELEASE );

        Assert.assertTrue( result instanceof RepositoryProxyResource );
        Assert.assertEquals( -1, ( (RepositoryProxyResource) result ).getArtifactMaxAge() );
        Assert.assertEquals( 1440, ( (RepositoryProxyResource) result ).getMetadataMaxAge() );
    }
}
