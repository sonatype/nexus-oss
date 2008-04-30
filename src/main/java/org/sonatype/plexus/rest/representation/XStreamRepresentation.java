/**
  * Copyright (C) 2008 Sonatype Inc. 
  * Sonatype Inc, licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in 
  * compliance with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
package org.sonatype.plexus.rest.representation;

import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.resource.StringRepresentation;

import com.thoughtworks.xstream.XStream;

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
    {
        // TODO: A BIG HACK FOLLOWS, UNTIL WE DO NOT RESOLVE XSTREAM HINTING!
        // In case of JSON reading (since JSON is not self-describing), we are adding
        // and "enveloping" object manually to incoming data.
        if ( MediaType.APPLICATION_JSON.equals( getMediaType(), true ) )
        {
            // it is JSON, applying hack, adding "envelope" object
            StringBuffer sb = new StringBuffer( "{ \"" ).append( root.getClass().getName() ).append( "\" : " ).append(
                getText() ).append( " }" );

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
