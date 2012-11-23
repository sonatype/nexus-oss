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
package org.sonatype.plexus.rest.xstream.json;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * JSON.org based StreamDriver.
 * 
 * @author cstamas
 */
public class JsonOrgHierarchicalStreamDriver
    implements HierarchicalStreamDriver
{
    private ClassHintProvider classHintProvider;

    public JsonOrgHierarchicalStreamDriver()
    {
        this( null );
    }

    public JsonOrgHierarchicalStreamDriver( ClassHintProvider classHintProvider )
    {
        super();
        this.classHintProvider = classHintProvider;
    }

    public HierarchicalStreamReader createReader( Reader in )
    {
        if ( classHintProvider != null )
        {
            return new JsonOrgHierarchicalStreamReader( in, false, classHintProvider );
        }
        else
        {
            return new JsonOrgHierarchicalStreamReader( in, true );
        }
    }

    public HierarchicalStreamReader createReader( InputStream in )
    {
        return createReader( new InputStreamReader( in ) );
    }

    public HierarchicalStreamWriter createWriter( Writer out )
    {
        return new JsonOrgHierarchicalStreamWriter( out, false );
    }

    public HierarchicalStreamWriter createWriter( OutputStream out )
    {
        return createWriter( new OutputStreamWriter( out ) );
    }

}
