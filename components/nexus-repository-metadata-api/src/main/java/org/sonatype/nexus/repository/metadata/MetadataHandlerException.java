/**
 * Copyright (c) 2007-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License Version 1.0, which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.repository.metadata;

public class MetadataHandlerException
    extends Exception
{
    private static final long serialVersionUID = 1748381444529675486L;

    public MetadataHandlerException( String message )
    {
        super( message );
    }

    public MetadataHandlerException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public MetadataHandlerException( Throwable cause )
    {
        super( cause );
    }
}
