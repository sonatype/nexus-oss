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
package org.sonatype.nexus.integrationtests.nexus947;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.test.utils.ContentListMessageUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus947GroupBrowsingIT
    extends AbstractNexusIntegrationTest
{
	
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void groupTest() throws IOException
    {
        ContentListMessageUtil contentUtil = new ContentListMessageUtil(this.getXMLXStream(), MediaType.APPLICATION_XML);

        List<ContentListResource> items = contentUtil.getContentListResource( "public", "/", true );

        // make sure we have a few items
        Assert.assertTrue( items.size() > 1, "Expected more then 1 item. " );

        // now for a bit more control
        items = contentUtil.getContentListResource( "public", "/nexus947/nexus947/3.2.1/", true );

        ArrayList<String> itemsText = new ArrayList<String>();        
      
        for(ContentListResource resource: items)
        {
            itemsText.add( resource.getText() );
        }

        // they are sorted in alpha order, so expect the jar, then the pom
        Assert.assertTrue( itemsText.contains("nexus947-3.2.1.jar") );
        Assert.assertTrue( itemsText.contains("nexus947-3.2.1.pom") );
    }

    @Test
    public void redirectTest() throws IOException
    {
        String uriPart = RequestFacade.SERVICE_LOCAL + "repo_groups/" + "public" + "/content";
        Response response = RequestFacade.sendMessage( uriPart, Method.GET );
        Assert.assertEquals( 301, response.getStatus().getCode() );

        Assert.assertTrue(response.getLocationRef().toString().endsWith( uriPart + "/" ));

    }
    
    
}
