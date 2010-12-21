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
