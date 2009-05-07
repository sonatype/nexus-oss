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
package org.sonatype.security.authorization.xml;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = PrivilegeInheritanceManager.class )
public class DefaultPrivilegeInheritanceManager
    implements PrivilegeInheritanceManager
{
    public List<String> getInheritedMethods( String method )
    {
        List<String> methods = new ArrayList<String>();

        methods.add( method );

        if ( "create".equals( method ) )
        {
            methods.add( "read" );
        }
        else if ( "delete".equals( method ) )
        {
            methods.add( "read" );
        }
        else if ( "update".equals( method ) )
        {
            methods.add( "read" );
        }

        return methods;
    }
}
