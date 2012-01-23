/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
