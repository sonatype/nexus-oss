package org.sonatype.nexus.integrationtests.nexus4268;

import java.io.IOException;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This test tests NEXUS-4268 and Nexus' capability to route properly repository types contributed by plugins, hence
 * their role and also implementation comes from core's child classloader (the plugin classloader). For this purpose,
 * nexus-it-helper-plugin got {@link org.sonatype.nexus.plugins.repository.SimpleRepository} repository type, and this
 * IT in it's resources {@code test-config} delivers a configuration that contains this new repository type with id
 * "simple" defined. We test it's reachability over {@code /content/repositories/simple} but also
 * {@code /contenet/simply/simple} since the new repository type defines "simply" as path prefix (the
 * {@code repositories} path prefix is reserved for ALL repositories (by design).
 * 
 * @author cstamas
 */
public class Nexus4268NewPluginContributedRepositoryTypeRoutingIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void testRepositoriesPath()
        throws IOException
    {
        // note the ending slash! We query the repo root, and slash is there to
        // avoid redirect
        final String servicePath = "content/repositories/simple/";

        Response response = RequestFacade.sendMessage( servicePath, Method.GET );

        Assert.assertEquals( response.getStatus().getCode(), 200, "Repository should be accessible over " + servicePath );
    }

    @Test
    public void testPathPrefixPath()
        throws IOException
    {
        // note the ending slash! We query the repo root, and slash is there to
        // avoid redirect
        final String servicePath = "content/simply/simple/";

        Response response = RequestFacade.sendMessage( servicePath, Method.GET );

        Assert.assertEquals( response.getStatus().getCode(), 200, "Repository should be accessible over " + servicePath );
    }
}
