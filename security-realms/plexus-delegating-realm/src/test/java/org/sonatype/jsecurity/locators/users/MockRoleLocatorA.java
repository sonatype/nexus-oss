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
package org.sonatype.jsecurity.locators.users;

import java.util.HashSet;
import java.util.Set;

public class MockRoleLocatorA extends AbstractTestRoleLocator
{
    private static final String SOURCE = "MockRoleLocatorA";

    public String getSource()
    {
        return SOURCE;
    }

    public Set<String> listRoleIds()
    {
        Set<String> ids = new HashSet<String>();
        ids.add( "role123" );
        ids.add( "role124" );
        ids.add( "role125" );
        ids.add( "role126" );
        ids.add( "duplicateId" );// this is also in B
        return ids;
    }   

}
