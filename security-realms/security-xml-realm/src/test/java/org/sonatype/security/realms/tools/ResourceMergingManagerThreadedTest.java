package org.sonatype.security.realms.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.context.Context;
import org.slf4j.Logger;
import org.sonatype.security.model.CPrivilege;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

public class ResourceMergingManagerThreadedTest
    extends PlexusTestCase
{
    private ConfigurationManager manager;

    private List<UnitTestSecurityResource> staticSecurityResources = new ArrayList<UnitTestSecurityResource>();

    private List<UnitTestDynamicSecurityResource> dynamicSecurityResources =
        new ArrayList<UnitTestDynamicSecurityResource>();

    private int expectedPrivilegeCount = 0;

    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration configuration )
    {
        configuration.setClassPathScanning( PlexusConstants.SCANNING_CACHE );
    }

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        context.put( "security-xml-file",
                     "target/test-classes/org/sonatype/jsecurity/configuration/static-merging/security.xml" );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        this.manager = (ConfigurationManager) lookup( ConfigurationManager.class, "resourceMerging" );

        int staticResourceCount = 100;
        for ( int ii = 0; ii < staticResourceCount-1; ii++ )
        {
            // defined in a components.xml
            UnitTestSecurityResource staticResource =
                (UnitTestSecurityResource) this.lookup( StaticSecurityResource.class, "default" );
            this.staticSecurityResources.add( staticResource );

            ComponentDescriptor staticTemplateDescriptor =
                this.getContainer().getComponentDescriptor( StaticSecurityResource.class.getName(),
                                                            "UnitTestSecurityResource" );

            ComponentDescriptor<StaticSecurityResource> descriptor = new ComponentDescriptor<StaticSecurityResource>();
            descriptor.setRole( StaticSecurityResource.class.getName() );
            descriptor.setRoleHint( "test-" + ii );
            descriptor.setImplementationClass( UnitTestSecurityResource.class );

            this.getContainer().addComponentDescriptor( descriptor );
        }

        int staticPrivCount = this.manager.listPrivileges().size() - 1; // we have one dynamic resource already configured

        int dynamicResourceCount = 100;
        for ( int ii = 0; ii < dynamicResourceCount-1; ii++ )
        {
            // defined in a components.xml
            UnitTestDynamicSecurityResource dynamicResource =
                (UnitTestDynamicSecurityResource) this.lookup( DynamicSecurityResource.class,
                                                               "default" );
            this.dynamicSecurityResources.add( dynamicResource );

            ComponentDescriptor dynamicTemplateDescriptor =
                this.getContainer().getComponentDescriptor( DynamicSecurityResource.class.getName(),
                                                            "UnitTestDynamicSecurityResource" );

            ComponentDescriptor<DynamicSecurityResource> descriptor =
                new ComponentDescriptor<DynamicSecurityResource>();
            descriptor.setRole( DynamicSecurityResource.class.getName() );
            descriptor.setRoleHint( "test-" + ii );
            descriptor.setImplementationClass( UnitTestDynamicSecurityResource.class );

            this.getContainer().addComponentDescriptor( descriptor );
        }
        
        // test the lookup, make sure we have 100
        Assert.assertEquals( staticResourceCount, this.getContainer().lookupList( StaticSecurityResource.class ).size() );
        Assert.assertEquals( dynamicResourceCount, this.getContainer().lookupList( DynamicSecurityResource.class ).size() );
        
        this.expectedPrivilegeCount = this.manager.listPrivileges().size();
        
        Assert.assertEquals( "Found "+ staticPrivCount + " the result should be this plus 100", staticPrivCount + dynamicResourceCount, expectedPrivilegeCount );

    }

    public void testThreading()
        throws Throwable
    {
        TestFramework.runOnce( new MultithreadedTestCase()
        {
            // public void initialize()
            // {
            //
            // }

            public void thread1()
            {
                dynamicSecurityResources.get( 1 ).setDirty( true );
                Assert.assertEquals( expectedPrivilegeCount, manager.listPrivileges().size() );
            }

            public void thread2()
            {
                Assert.assertEquals( expectedPrivilegeCount, manager.listPrivileges().size() );
            }

            public void thread3()
            {
                dynamicSecurityResources.get( 3 ).setDirty( true );
                Assert.assertEquals( expectedPrivilegeCount, manager.listPrivileges().size() );
            }

            public void thread4()
            {
                Assert.assertEquals( expectedPrivilegeCount, manager.listPrivileges().size() );
            }

            public void thread5()
            {
                dynamicSecurityResources.get( 5 ).setDirty( true );
                Assert.assertEquals( expectedPrivilegeCount, manager.listPrivileges().size() );
            }

        } );// , Integer.MAX_VALUE, Integer.MAX_VALUE ); // uncomment this for debugging, if you don't the framework
            // will timeout and close your debug session

        for ( UnitTestDynamicSecurityResource dynamicSecurityResource : dynamicSecurityResources )
        {

            Assert.assertFalse( dynamicSecurityResource.isDirty() );
            Assert.assertTrue( "Get config should be called on each dynamic resource after set dirty is called on any of them: " + dynamicSecurityResource.getId(),
                               dynamicSecurityResource.isConfigCalledAfterSetDirty() );
        }

    }

}
