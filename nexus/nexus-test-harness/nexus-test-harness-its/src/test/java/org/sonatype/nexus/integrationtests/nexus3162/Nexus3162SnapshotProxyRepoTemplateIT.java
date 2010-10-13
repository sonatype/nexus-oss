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
