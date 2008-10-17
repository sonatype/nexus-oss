package org.sonatype.nexus.integrationtests.nexus923;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.test.utils.ContentListMessageUtil;

public class Nexus923BrowseRootWithTarget
    extends AbstractPrivilegeTest
{

    @Test
    public void browseRootTest()
        throws Exception
    {
        // create repo target
        RepositoryTargetResource target = new RepositoryTargetResource();
        target.setName( "browseRootTest" );
        target.setContentClass( "maven2" );
        target.addPattern( "/nexus923/group/*" );

        target = this.targetUtil.createTarget( target );

        // now create a priv
        PrivilegeTargetResource browseRootTestPriv = new PrivilegeTargetResource();
        browseRootTestPriv.addMethod( "create" );
        browseRootTestPriv.addMethod( "read" );
        browseRootTestPriv.addMethod( "update" );
        browseRootTestPriv.addMethod( "delete" );
        browseRootTestPriv.setName( "browseRootTestPriv" );
        browseRootTestPriv.setType( "repositoryTarget" );
        browseRootTestPriv.setRepositoryTargetId( target.getId() );
        browseRootTestPriv.setRepositoryId( this.getTestRepositoryId() );
        // get the Resource object
        List<PrivilegeBaseStatusResource> browseRootTestPrivs = this.privUtil.createPrivileges( browseRootTestPriv );
        String[] pivsStrings = new String[browseRootTestPrivs.size()];
        for ( int ii = 0; ii < browseRootTestPrivs.size(); ii++ )
        {
            PrivilegeBaseStatusResource priv = browseRootTestPrivs.get(ii);
            pivsStrings[ii] =  priv.getId();
        }

        this.overwriteUserRole( TEST_USER_NAME, "browseRootTestRole", pivsStrings );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        ContentListMessageUtil contentUtil = new ContentListMessageUtil(
            this.getXMLXStream(),
            MediaType.APPLICATION_XML );

        List<ContentListResource> items = contentUtil.getContentListResource( this.getTestRepositoryId(), "/", false );
        Assert.assertEquals( "Expected to have only one entry: " + items, 1, items.size() );
        
        items = contentUtil.getContentListResource( this.getTestRepositoryId(), "/nexus923", false );
        Assert.assertEquals( "Expected to have only one entry: " + items, 1, items.size() );
        
        items = contentUtil.getContentListResource( this.getTestRepositoryId(), "/nexus923/group/", false );
        Assert.assertEquals( "Expected to have only one entry: " + items, 2, items.size() );
    }

}
