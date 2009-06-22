package org.sonatype.nexus.error.reporting;

import com.thoughtworks.xstream.XStream;

public class AbstractXmlHandler
{
    /**
     * XStream is used for a deep clone (TODO: not sure if this is a great idea)
     */
    private static XStream xstream = new XStream();
    
    protected static final String PASSWORD_MASK = "*****";
    
    protected Object cloneViaXml( Object configuration )
    {
        if ( configuration == null )
        {
            return null;
        }
        
        return xstream.fromXML( xstream.toXML( configuration ) );
    }
}
