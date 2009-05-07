package org.sonatype.nexus.rest;

import javax.servlet.http.HttpServletRequest;

import org.restlet.data.Request;

public interface RemoteIPFinder
{
    /**
     * Find the remote IP using rest request
     * 
     * @param request
     * @return
     */
    public String findIP( Request request );
    
    /**
     * Find the remote IP using http servlet request
     * 
     * @param request
     * @return
     */
    public String findIP( HttpServletRequest request );
}
