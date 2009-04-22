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
package org.sonatype.security.selectors;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.jsecurity.realm.Realm;
import org.sonatype.security.realms.FakeRealm1;
import org.sonatype.security.realms.FakeRealm2;
import org.sonatype.security.selectors.RealmCriteria;
import org.sonatype.security.selectors.RealmSelector;

public class DefaultRealmSelectorTest
    extends PlexusTestCase
{
    public static final String LOCATOR_PROPERTY_FILE = "realm-locator-property-file";

    private RealmSelector selector;

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

        selector = (RealmSelector) lookup( RealmSelector.class );
    }

    public void testSelector()
        throws Exception
    {
        RealmCriteria criteria = new RealmCriteria();

        criteria.setName( FakeRealm1.class.getName() );

        Realm selected = selector.selectRealm( criteria );

        assertTrue( selected != null );
        assertTrue( selected.getName().equals( FakeRealm1.class.getName() ) );

        criteria.setName( FakeRealm2.class.getName() );

        selected = selector.selectRealm( criteria );

        assertTrue( selected != null );
        assertTrue( selected.getName().equals( FakeRealm2.class.getName() ) );
    }
}
