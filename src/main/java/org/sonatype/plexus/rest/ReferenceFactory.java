package org.sonatype.plexus.rest;

import org.restlet.data.Reference;
import org.restlet.data.Request;

public interface ReferenceFactory
{

    Reference createChildReference( Request request, String childPath );
    
    Reference getContextRoot( Request request );
    
}
