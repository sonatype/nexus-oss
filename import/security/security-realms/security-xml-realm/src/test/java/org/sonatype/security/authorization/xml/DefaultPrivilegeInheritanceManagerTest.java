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
package org.sonatype.security.authorization.xml;

import java.util.List;

import org.sonatype.guice.bean.containers.InjectedTestCase;

public class DefaultPrivilegeInheritanceManagerTest
    extends InjectedTestCase
{
    private DefaultPrivilegeInheritanceManager manager;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        manager = (DefaultPrivilegeInheritanceManager) this.lookup( PrivilegeInheritanceManager.class );
    }

    public void testCreateInherit()
        throws Exception
    {
        List<String> methods = manager.getInheritedMethods( "create" );

        assertTrue( methods.size() == 2 );
        assertTrue( methods.contains( "read" ) );
        assertTrue( methods.contains( "create" ) );
    }

    public void testReadInherit()
        throws Exception
    {
        List<String> methods = manager.getInheritedMethods( "read" );

        assertTrue( methods.size() == 1 );
        assertTrue( methods.contains( "read" ) );
    }

    public void testUpdateInherit()
        throws Exception
    {
        List<String> methods = manager.getInheritedMethods( "update" );

        assertTrue( methods.size() == 2 );
        assertTrue( methods.contains( "read" ) );
        assertTrue( methods.contains( "update" ) );
    }

    public void testDeleteInherit()
        throws Exception
    {
        List<String> methods = manager.getInheritedMethods( "delete" );

        assertTrue( methods.size() == 2 );
        assertTrue( methods.contains( "read" ) );
        assertTrue( methods.contains( "delete" ) );
    }
}
