/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.jsecurity.realms.tools;

import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;

public class ResourceMergingConfigurationManagerTest
    extends PlexusTestCase
{
    private ConfigurationManager manager;
    
    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );
        
        context.put( "security-xml-file", "target/test-classes/org/sonatype/jsecurity/configuration/static-merging/security.xml" );
    }
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        manager = ( ConfigurationManager ) lookup( ConfigurationManager.class, "resourceMerging" );
    }
    
    public void testMerging()
        throws Exception
    {
        List<SecurityPrivilege> privs = manager.listPrivileges();
        
        assertEquals( 4, privs.size() );
        
        SecurityPrivilege priv = manager.readPrivilege( "1" );        
        assertTrue( priv != null );
        
        priv = manager.readPrivilege( "2" );
        assertTrue( priv != null );
        
        priv = manager.readPrivilege( "3" );
        assertTrue( priv != null );
        
        priv = manager.readPrivilege( "4" );
        assertTrue( priv != null );
    }
}
