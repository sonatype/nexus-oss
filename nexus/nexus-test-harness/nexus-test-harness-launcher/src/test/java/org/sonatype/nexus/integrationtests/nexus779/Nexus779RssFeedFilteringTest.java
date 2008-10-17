package org.sonatype.nexus.integrationtests.nexus779;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetStatusResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.test.utils.FeedUtil;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Test filtering search results based upon security
 */
public class Nexus779RssFeedFilteringTest
    extends AbstractPrivilegeTest
{    
    public Nexus779RssFeedFilteringTest()
    {
    }
    
    @Test
    public void filteredFeeds()
        throws Exception
    {                
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        
        // First create the targets
        RepositoryTargetResource test1Target = createTarget( "filterTarget1", Collections.singletonList( ".*/test1/.*" ) );
        RepositoryTargetResource test2Target = createTarget( "filterTarget2", Collections.singletonList( ".*/test2/.*" ) );
        
        // Then create the privileges
        PrivilegeTargetStatusResource priv1 = createPrivilege( "filterPriv1", test1Target.getId() );
        PrivilegeTargetStatusResource priv2 = createPrivilege( "filterPriv2", test2Target.getId() );
        
        // Then create the roles
        List<String> combined = new ArrayList<String>();
        combined.add( priv1.getId() );
        combined.add( priv2.getId() );
        RoleResource role1 = createRole( "filterRole1", Collections.singletonList( priv1.getId() ) );
        RoleResource role2 = createRole( "filterRole2", Collections.singletonList( priv2.getId() ) );
        RoleResource role3 = createRole( "filterRole3", combined );
        
        // Now update the test user
        updateUserRole( TEST_USER_NAME, Collections.singletonList( role3.getId() ) );
        
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );
        
        // Should be able to see both test1 & test2 artifacts
        SyndFeed feed = FeedUtil.getFeed( "recentlyDeployed" );
        List<SyndEntry> entries = feed.getEntries();
        
        Assert.assertTrue("Feed should contain entry for nexus779:test1:1.0.0.\nEntries: "+ this.entriesToString(entries), feedListContainsArtifact( entries, "nexus779", "test1", "1.0.0" ) );
        
        Assert.assertTrue("Feed should contain entry for nexus779:test2:1.0.0\nEntries: "+ this.entriesToString(entries), feedListContainsArtifact( entries, "nexus779", "test2", "1.0.0" ) );

        
        // Now update the test user so that the user can only access test1        
        updateUserRole( TEST_USER_NAME, Collections.singletonList( role1.getId() ) );
        
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );
        
        // Should be able to see only test1 artifacts
        feed = FeedUtil.getFeed( "recentlyDeployed" );
        entries = feed.getEntries();
        
        Assert.assertTrue("Feed should contain entry for nexus779:test1:1.0.0.\nEntries: "+ this.entriesToString(entries), feedListContainsArtifact( entries, "nexus779", "test1", "1.0.0" ) );
        
        Assert.assertFalse( "Feed should not contain entry for nexus779:test2:1.0.0\nEntries: "+ this.entriesToString(entries), feedListContainsArtifact( entries, "nexus779", "test2", "1.0.0" ) );

        
        // Now update the test user so that the user can only access test2        
        updateUserRole( TEST_USER_NAME, Collections.singletonList( role2.getId() ) );
                
        
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );
        
        // Should be able to see only test2 artifacts
        feed = FeedUtil.getFeed( "recentlyDeployed" );
        entries = feed.getEntries();
        
        Assert.assertFalse("Feed should not contain entry for nexus779:test1:1.0.0.\nEntries: "+ this.entriesToString(entries), feedListContainsArtifact( entries, "nexus779", "test1", "1.0.0" ) );
        
        Assert.assertTrue( "Feed should contain entry for nexus779:test2:1.0.0\nEntries: "+ this.entriesToString(entries), feedListContainsArtifact( entries, "nexus779", "test2", "1.0.0" ) );

    }
    
    private String entriesToString(List<SyndEntry> entries)
    {
        StringBuffer buffer = new StringBuffer();
        
        for ( SyndEntry syndEntry : entries )
        {
            buffer.append( syndEntry.getTitle() ).append( "\n" );
        }
        
        return buffer.toString();        
    }
    
    private boolean feedListContainsArtifact( List<SyndEntry> entries, String groupId, String artifactId, String version )
    {
        for ( SyndEntry entry : entries )
        {
            if ( entry.getTitle().contains( groupId )
                && entry.getTitle().contains( artifactId )
                && entry.getTitle().contains( version ) )
            {
                return true;
            }
        }
        return false;
    }
    
    private RepositoryTargetResource createTarget( String name, List<String> patterns )
        throws Exception
    {
        RepositoryTargetResource resource = new RepositoryTargetResource();

        resource.setContentClass( "maven2" );
        resource.setName( name );

        resource.setPatterns( patterns );

        return this.targetUtil.createTarget( resource );
    }
    
    private PrivilegeTargetStatusResource createPrivilege( String name, String targetId )
        throws Exception
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();
        
        resource.setName( name );
        resource.setDescription( "some description" );
        resource.setType( "repositoryTarget" );
        resource.setRepositoryTargetId( targetId );
        resource.addMethod( "read" );
        
        return ( PrivilegeTargetStatusResource ) privUtil.createPrivileges( resource ).iterator().next();
    }
    
    private RoleResource createRole( String name, List<String> privilegeIds )
        throws Exception
    {
        RoleResource role = new RoleResource();
        role.setName( name );
        role.setDescription( "some description" );
        role.setSessionTimeout( 60 );
        
        for ( String privilegeId : privilegeIds )
        {
            role.addPrivilege( privilegeId );
        }
        
        role.addPrivilege( "1" );
        role.addPrivilege( "6" );
        role.addPrivilege( "14" );
        role.addPrivilege( "17" );
        role.addPrivilege( "19" );
        role.addPrivilege( "44" );
        role.addPrivilege( "54" );
        role.addPrivilege( "55" );
        role.addPrivilege( "56" );
        role.addPrivilege( "57" );
        role.addPrivilege( "58" );
        role.addPrivilege( "64" );
        
        return this.roleUtil.createRole( role );
    }
    
    private void updateUserRole( String username, List<String> roleIds )
        throws Exception
    {
        // change to admin so we can update the roles
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        
        UserResource resource = userUtil.getUser( username );
        
        resource.setRoles( roleIds );
        
        userUtil.updateUser( resource );
    }
}
