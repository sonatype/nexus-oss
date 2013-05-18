/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.realms.tools;

import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import junit.framework.Assert;

import org.sonatype.security.AbstractSecurityTestCase;

import com.google.inject.Binder;
import com.google.inject.name.Names;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

public class ResourceMergingManagerThreadedTest
    extends AbstractSecurityTestCase
{
    private ConfigurationManager manager;

    private int expectedPrivilegeCount = 0;

    @Inject
    private List<StaticSecurityResource> injectedStaticResources;

    @Inject
    private List<DynamicSecurityResource> injectedDynamicResources;

    @Override
    public void configure( Properties properties )
    {
        super.configure( properties );
        
        //Overriding default value set in parent
        properties.put( "security-xml-file",
            "target/test-classes/org/sonatype/jsecurity/configuration/static-merging/security.xml" );
    }

    @Override
    public void configure( Binder binder )
    {
        super.configure( binder );

        binder.bind( StaticSecurityResource.class ).annotatedWith( Names.named( "default" ) ).to( UnitTestSecurityResource.class );
        binder.bind( DynamicSecurityResource.class ).annotatedWith( Names.named( "default" ) ).to( UnitTestDynamicSecurityResource.class );

        int staticResourceCount = 100;
        for ( int ii = 0; ii < staticResourceCount - 1; ii++ )
        {
            binder.bind( StaticSecurityResource.class ).annotatedWith( Names.named( "test-" + ii ) ).to( UnitTestSecurityResource.class );
        }

        int dynamicResourceCount = 100;
        for ( int ii = 0; ii < dynamicResourceCount - 1; ii++ )
        {
            binder.bind( DynamicSecurityResource.class ).annotatedWith( Names.named( "test-" + ii ) ).to( UnitTestDynamicSecurityResource.class );
        }
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        this.manager = (ConfigurationManager) lookup( ConfigurationManager.class, "resourceMerging" );

        // test the lookup, make sure we have 100
        Assert.assertEquals( 100, injectedStaticResources.size() );
        Assert.assertEquals( 100, injectedDynamicResources.size() );

        this.expectedPrivilegeCount = this.manager.listPrivileges().size();

        // 100 static items with 3 privs each + 100 dynamic items
        Assert.assertEquals( ( 100 * 3 ) + 100, expectedPrivilegeCount );

        for ( DynamicSecurityResource dynamicSecurityResource : injectedDynamicResources )
        {
            Assert.assertFalse( dynamicSecurityResource.isDirty() );
        }
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
                ( (UnitTestDynamicSecurityResource) injectedDynamicResources.get( 1 ) ).setDirty( true );
                Assert.assertEquals( expectedPrivilegeCount, manager.listPrivileges().size() );
            }

            public void thread2()
            {
                Assert.assertEquals( expectedPrivilegeCount, manager.listPrivileges().size() );
            }

            public void thread3()
            {
                ( (UnitTestDynamicSecurityResource) injectedDynamicResources.get( 3 ) ).setDirty( true );
                Assert.assertEquals( expectedPrivilegeCount, manager.listPrivileges().size() );
            }

            public void thread4()
            {
                Assert.assertEquals( expectedPrivilegeCount, manager.listPrivileges().size() );
            }

            public void thread5()
            {
                ( (UnitTestDynamicSecurityResource) injectedDynamicResources.get( 5 ) ).setDirty( true );
                Assert.assertEquals( expectedPrivilegeCount, manager.listPrivileges().size() );
            }

        } );// , Integer.MAX_VALUE, Integer.MAX_VALUE ); // uncomment this for debugging, if you don't the framework
        // will timeout and close your debug session

        for ( DynamicSecurityResource dynamicSecurityResource : injectedDynamicResources )
        {

            Assert.assertFalse( dynamicSecurityResource.isDirty() );
            Assert.assertTrue( "Get config should be called on each dynamic resource after set dirty is called on any of them: "
                                   + ( (UnitTestDynamicSecurityResource) dynamicSecurityResource ).getId(),
                               ( (UnitTestDynamicSecurityResource) dynamicSecurityResource ).isConfigCalledAfterSetDirty() );
        }

    }

}
