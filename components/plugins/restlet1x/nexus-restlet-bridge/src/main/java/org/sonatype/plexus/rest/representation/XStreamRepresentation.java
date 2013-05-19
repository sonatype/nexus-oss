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
package org.sonatype.plexus.rest.representation;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.resource.StringRepresentation;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

/**
 * A String representation powered by XStream. This Representation needs XStream instance in constructor, and it is best
 * if you share your (threadsafe) XStream instance in restlet Application's Context, for example.
 *
 * @author cstamas
 */
public class XStreamRepresentation
    extends StringRepresentation
{
    private XStream xstream;

    public XStreamRepresentation( XStream xstream, String text, MediaType mt, Language language,
                                  CharacterSet characterSet )
    {
        super( text, mt, language, characterSet );

        this.xstream = xstream;
    }

    public XStreamRepresentation( XStream xstream, String text, MediaType mt, Language language )
    {
        this( xstream, text, mt, language, CharacterSet.UTF_8 );
    }

    public XStreamRepresentation( XStream xstream, String text, MediaType mt )
    {
        this( xstream, text, mt, null );
    }

    public Object getPayload( Object root )
        throws XStreamException
    {
        // TODO: A BIG HACK FOLLOWS, UNTIL WE DO NOT RESOLVE XSTREAM HINTING!
        // In case of JSON reading (since JSON is not self-describing), we are adding
        // and "enveloping" object manually to incoming data.
        if ( MediaType.APPLICATION_JSON.equals( getMediaType(), true ) )
        {
            // it is JSON, applying hack, adding "envelope" object
            StringBuffer sb =
                new StringBuffer( "{ \"" ).append( root.getClass().getName() ).append( "\" : " ).append( getText() ).append(
                                                                                                                             " }" );

            return xstream.fromXML( sb.toString(), root );
        }
        else
        {
            // it is XML or something else
            return xstream.fromXML( getText(), root );
        }
    }

    public void setPayload( Object object )
    {
        setText( xstream.toXML( object ) );
    }
}
