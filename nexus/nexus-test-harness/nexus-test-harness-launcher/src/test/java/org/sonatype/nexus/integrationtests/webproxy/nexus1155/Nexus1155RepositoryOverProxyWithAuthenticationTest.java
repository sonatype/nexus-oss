package org.sonatype.nexus.integrationtests.webproxy.nexus1155;

import org.sonatype.nexus.integrationtests.webproxy.nexus1146.Nexus1146RepositoryOverProxyTest;

public class Nexus1155RepositoryOverProxyWithAuthenticationTest
    extends Nexus1146RepositoryOverProxyTest
{

    @Override
    public void startWebProxy()
        throws Exception
    {
        super.startWebProxy();
        server.getProxyServlet().setUseAuthentication( true );
        server.getProxyServlet().getAuthentications().put( "admin", "123" );
    }

    @Override
    public void stopWebProxy()
        throws Exception
    {
        server.getProxyServlet().setUseAuthentication( false );
        server.getProxyServlet().setAuthentications( null );
        super.stopWebProxy();
    }

}
