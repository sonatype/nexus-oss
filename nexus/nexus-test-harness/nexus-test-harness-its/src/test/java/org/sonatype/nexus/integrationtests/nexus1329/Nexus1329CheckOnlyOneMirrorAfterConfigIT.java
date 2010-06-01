package org.sonatype.nexus.integrationtests.nexus1329;

import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceListRequest;
import org.sonatype.nexus.test.utils.TestProperties;

public class Nexus1329CheckOnlyOneMirrorAfterConfigIT
    extends  Nexus1329AbstractCheckOnlyOneMirror
{
    @Override
    protected void beforeCheck()
        throws Exception
    {
        super.beforeCheck();
        
        String proxyPort = TestProperties.getString( "webproxy-server-port" );
        
        MirrorResourceListRequest mirrorReq = new MirrorResourceListRequest();
        MirrorResource resource = new MirrorResource();
        resource.setId( "111" );
        resource.setUrl( "http://localhost:" + proxyPort + "/mirror1" );
        mirrorReq.addData( resource );
        resource = new MirrorResource();
        resource.setId( "222" );
        resource.setUrl( "http://localhost:" + proxyPort + "/mirror2" );
        mirrorReq.addData( resource );
        
        messageUtil.setMirrors( REPO, mirrorReq );
    }
}
