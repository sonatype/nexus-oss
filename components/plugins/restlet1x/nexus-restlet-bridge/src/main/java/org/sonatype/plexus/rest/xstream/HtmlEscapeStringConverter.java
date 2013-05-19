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
package org.sonatype.plexus.rest.xstream;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.app.event.implement.EscapeHtmlReference;

import com.thoughtworks.xstream.converters.basic.StringConverter;

/**
 *  Escapse HTML, to project against XSS.
 */
public class HtmlEscapeStringConverter
    extends StringConverter
{

    @Override
    public Object fromString( String str )
    {
        return StringEscapeUtils.escapeHtml( str );
        
    }

    // TODO: consider escaping this way to in case someone has access to persisted data?
//    @Override
//    public String toString( Object obj )
//    {
//        // TODO Auto-generated method stub
//        return super.toString( obj );
//    }

}
