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
package org.sonatype.appcontext.guice;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.guice.bean.binders.ParameterKeys;

/**
 * Simple adapter to adapt {@link AppContext} into a {@link Map} of strings, usable for SISU
 * {@link ParameterKeys#PROPERTIES} bindings. Since {@link AppContext} values are Objects, and SISU
 * {@link ParameterKeys#PROPERTIES} expect {@link Map} of string keys and string values, app context values are
 * converted to String on the fly using {@link String#valueOf(Object)}, hence, if you are about to inject a key that has
 * some non-string value, it will (probably) result in failure. Many operation implementations perform
 * {@link AppContext#flatten()} to capture a "snapshot" of {@link AppContext} with it's parent, and it might be costly
 * operation. But anyway, the purpose of this adapter is to be used in SISU, and parameters are looked up in bootstrap
 * phase and never again, so this overhead is not noticeable (might affect wiring speed, but once application is wired,
 * it's speed is not affected). Also, this means this adapter should not be used in any other cases, it was meant for
 * this single use case.
 * 
 * @author cstamas
 */
class StringAdapter
    implements Map<String, String>
{
    private final AppContext appContext;

    public StringAdapter( final AppContext appContext )
    {
        this.appContext = Preconditions.checkNotNull( appContext );
    }

    public String get( Object key )
    {
        return String.valueOf( appContext.get( key ) );
    }

    public boolean containsKey( Object key )
    {
        return appContext.flatten().containsKey( key );
    }

    public boolean containsValue( Object value )
    {
        return appContext.flatten().containsValue( value );
    }

    public int size()
    {
        return appContext.flatten().size();
    }

    public boolean isEmpty()
    {
        return appContext.flatten().isEmpty();
    }

    public Set<String> keySet()
    {
        return appContext.flatten().keySet();
    }

    public Set<Map.Entry<String, String>> entrySet()
    {
        throw new UnsupportedOperationException();
    }

    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    public String put( String key, String value )
    {
        throw new UnsupportedOperationException();
    }

    public void putAll( Map<? extends String, ? extends String> m )
    {
        throw new UnsupportedOperationException();
    }

    public String remove( Object key )
    {
        throw new UnsupportedOperationException();
    }

    public Collection<String> values()
    {
        throw new UnsupportedOperationException();
    }
}
