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

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Default implementation of PrivilegeInheritanceManager which adds read to each action. The way we see it, if you can
 * create/update/delete something then you automatically have access to 'read' it as well.
 * 
 * @author Brian Demers
 */
@Singleton
@Typed( PrivilegeInheritanceManager.class )
@Named( "default" )
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
