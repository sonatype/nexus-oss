package org.sonatype.nexus.test.utils;

import com.google.common.base.Preconditions;
import com.thoughtworks.xstream.XStream;
import org.restlet.data.MediaType;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

/**
 * XStream Util
 */
public abstract class XStreamUtil {
    
    public static final XStream DEFAULT_XML_XSTREAM;
    public static final XStream DEFAULT_JSON_XSTREAM;
    
    static {
        DEFAULT_XML_XSTREAM = XStreamFactory.getXmlXStream();
        DEFAULT_JSON_XSTREAM = XStreamFactory.getJsonXStream();
    }
    
    /**
     * Convert a Resource to its XStreamRepresentation using a default XStream instance and text of ""
     */
    public static XStreamRepresentation toRepresentation(final Object resource){
        return toRepresentation(DEFAULT_XML_XSTREAM, "", resource);
    }
    
    public static XStreamRepresentation toRepresentation(final String text, final Object resource){
        return toRepresentation(DEFAULT_XML_XSTREAM, text, resource);
    }
    
    /**
     * Convert a Resource to its XStreamRepresentation
     */
    public static XStreamRepresentation toRepresentation(final XStream xStream, final String text, final Object resource){
        Preconditions.checkNotNull(resource);
        Preconditions.checkNotNull(text);
        XStream xstreamToUse = xStream == null ? DEFAULT_XML_XSTREAM : xStream;
        final XStreamRepresentation representation = new XStreamRepresentation( xstreamToUse, text, MediaType.APPLICATION_XML );
        representation.setPayload( resource );
        return representation;
    }
}
