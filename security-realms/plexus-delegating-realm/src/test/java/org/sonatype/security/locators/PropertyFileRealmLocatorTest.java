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
package org.sonatype.security.locators;

import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.jsecurity.realm.Realm;
import org.sonatype.security.locators.RealmLocator;
import org.sonatype.security.realms.FakeRealm1;
import org.sonatype.security.realms.FakeRealm2;

public class PropertyFileRealmLocatorTest
    extends PlexusTestCase
{
    private RealmLocator locator;
    
    public static final String LOCATOR_PROPERTY_FILE = "realm-locator-property-file";
    
    @Override
    protected void customizeContext( Context context )
    {
        context.put( LOCATOR_PROPERTY_FILE, getBasedir() + "/target/test-classes/realm-locator.properties" );
    }
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        locator = ( RealmLocator ) lookup( RealmLocator.class );
    }
    
    public void testLocator()
        throws Exception
    {
        List<Realm> realms = locator.getRealms();
        
        assertTrue( realms.size() == 2);
        
        assertTrue( realms.get( 0 ).getClass().equals( FakeRealm1.class ) );
        assertTrue( realms.get( 1 ).getClass().equals( FakeRealm2.class ) );
    }
}
