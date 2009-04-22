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

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.jsecurity.realm.Realm;
import org.sonatype.security.locators.RealmLocator;

@Component( role = RealmSelector.class )
public class DefaultRealmSelector
    implements RealmSelector
{
    @Requirement
    private RealmLocator realmLocator;

    public Realm selectRealm( RealmCriteria criteria )
    {
        for ( Realm realm : this.realmLocator.getRealms() )
        {
            if ( criteria.matches( realm ) )
            {
                return realm;
            }
        }

        return null;
    }

    public List<Realm> selectAllRealms()
    {
        return this.realmLocator.getRealms();
    }
}
