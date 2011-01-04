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
package org.sonatype.nexus.integrationtests.nexus923;

import java.util.List;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.rest.model.PrivilegeResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.test.utils.ContentListMessageUtil;
import org.sonatype.security.rest.model.PrivilegeStatusResource;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus923BrowseRootWithTargetIT
    extends AbstractPrivilegeTest
{
    @Test
    public void browseRootTest()
        throws Exception
    {
        if ( this.printKnownErrorButDoNotFail( Nexus923BrowseRootWithTargetIT.class , "browseRootTest" ) )
        {
            return;
        }
        
        // create repo target
        RepositoryTargetResource target = new RepositoryTargetResource();
        target.setName( "browseRootTest" );
        target.setContentClass( "maven2" );
        target.addPattern( "/nexus923/group/*" );

        target = this.targetUtil.createTarget( target );

        // now create a priv
        PrivilegeResource browseRootTestPriv = new PrivilegeResource();
        browseRootTestPriv.addMethod( "create" );
        browseRootTestPriv.addMethod( "read" );
        browseRootTestPriv.addMethod( "update" );
        browseRootTestPriv.addMethod( "delete" );
        browseRootTestPriv.setName( "browseRootTestPriv" );
        browseRootTestPriv.setType( TargetPrivilegeDescriptor.TYPE );
        browseRootTestPriv.setRepositoryTargetId( target.getId() );
        browseRootTestPriv.setRepositoryId( this.getTestRepositoryId() );
        // get the Resource object
        List<PrivilegeStatusResource> browseRootTestPrivs = this.privUtil.createPrivileges( browseRootTestPriv );
        String[] pivsStrings = new String[browseRootTestPrivs.size()];
        for ( int ii = 0; ii < browseRootTestPrivs.size(); ii++ )
        {
            PrivilegeStatusResource priv = browseRootTestPrivs.get(ii);
            pivsStrings[ii] =  priv.getId();
        }

        this.overwriteUserRole( TEST_USER_NAME, "browseRootTestRole", pivsStrings );
        this.giveUserPrivilege( TEST_USER_NAME, "repository-all" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        ContentListMessageUtil contentUtil = new ContentListMessageUtil(
            this.getXMLXStream(),
            MediaType.APPLICATION_XML );

        List<ContentListResource> items = contentUtil.getContentListResource( this.getTestRepositoryId(), "/", false );
        Assert.assertEquals( items.size(), 1, "Expected to have only one entry: " + items );
        
        items = contentUtil.getContentListResource( this.getTestRepositoryId(), "/nexus923", false );
        Assert.assertEquals( items.size(), 1, "Expected to have only one entry: " + items );
        
        items = contentUtil.getContentListResource( this.getTestRepositoryId(), "/nexus923/group/", false );
        Assert.assertEquals( items.size(), 1, "Expected to have only one entry: " + items );
    }

}
