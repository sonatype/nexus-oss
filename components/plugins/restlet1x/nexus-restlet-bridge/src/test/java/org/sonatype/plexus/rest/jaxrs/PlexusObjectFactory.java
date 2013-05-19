/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.plexus.rest.jaxrs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.ext.jaxrs.InstantiateException;
import org.restlet.ext.jaxrs.ObjectFactory;
import org.sonatype.plexus.rest.jsr311.JsrComponent;

@Component( role = PlexusObjectFactory.class )
public class PlexusObjectFactory
    implements ObjectFactory
{
    @Requirement( role = JsrComponent.class )
    private Map<String, Object> hinstsToresources;

    /** A lookup map filled in by getResourceClasses */
    private Map<Class<?>, Object> classesToComponents;

    public Set<Class<?>> getResourceClasses()
    {
        classesToComponents = new HashMap<Class<?>, Object>( hinstsToresources.size() );

        for ( Object res : hinstsToresources.values() )
        {
            classesToComponents.put( res.getClass(), res );
        }

        return classesToComponents.keySet();
    }

    public <T> T getInstance( Class<T> jaxRsClass )
        throws InstantiateException
    {
        if ( classesToComponents.containsKey( jaxRsClass ) )
        {
            return (T) classesToComponents.get( jaxRsClass );
        }

        throw new InstantiateException( "JsrComponent of class '" + jaxRsClass.getName() + "' not found!" );
    }

}
