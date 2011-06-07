package org.sonatype.nexus.test.utils;

import com.google.common.base.Preconditions;
import org.restlet.data.MediaType;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

/**
 * XStream Util
 */
public abstract class XStreamUtil {
    
    /**
     * Convert a Resource to its XStreamRepresentation
     */
    public static XStreamRepresentation toRepresentation(final Object resource){
        Preconditions.checkNotNull(resource);
        final XStreamRepresentation representation = new XStreamRepresentation( XStreamFactory.getXmlXStream(), "", MediaType.APPLICATION_XML );
        representation.setPayload( resource );
        return representation;
    }
    
    
}
