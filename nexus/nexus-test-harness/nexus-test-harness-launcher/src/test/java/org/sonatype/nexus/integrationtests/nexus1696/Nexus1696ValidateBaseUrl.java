package org.sonatype.nexus.integrationtests.nexus1696;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

public class Nexus1696ValidateBaseUrl extends AbstractNexusIntegrationTest
{

    private String baseUrl;

    @Override
    @Before
    public  void runOnce()
        throws Exception
    {
        baseUrl = baseNexusUrl.replace( "nexus", "nexus1696" );

        super.runOnce();

        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
        settings.setForceBaseUrl( true );
        settings.setBaseUrl( baseUrl );
    }

    @Test
    public void checkRepositoryURI() throws Exception {
        List<RepositoryListResource> repositories = new RepositoryMessageUtil(null, null, null).getList();
        for ( RepositoryListResource repo : repositories )
        {
            Assert.assertTrue( "Repository '" + repo.getId() + "' uri do not start with baseUrl.  Expected: " + baseUrl + ", but got: " + repo.getResourceURI(), repo.getResourceURI().startsWith( baseUrl ) );
        }
    }

}
