/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.restlight.common;

import org.jdom.Document;

/**
 * Exception indicating a failure to communicate with the Nexus server. This normally means either an 
 * I/O failure or a failure to parse the response message.
 */
public class RESTLightClientException
    extends Exception
{

    private static final long serialVersionUID = 1L;

    private final Document errorDocument;

    public Document getErrorDocument()
    {
        return errorDocument;
    }

    public RESTLightClientException( final String message, final Throwable cause, final Document errorDocument )
    {
        super( message, cause );
        this.errorDocument = errorDocument;
    }

    public RESTLightClientException( final String message, final Throwable cause )
    {
        this( message, cause, null );
    }

    public RESTLightClientException( final String message )
    {
        this( message, null, null );
    }

}
