/**
 * Copyright (c) 2007-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License Version 1.0, which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.repository.metadata.validation;

import org.sonatype.nexus.repository.metadata.MetadataHandlerException;

public class ValidationException
    extends MetadataHandlerException
{
    private static final long serialVersionUID = -8892632174114363043L;

    public ValidationException( String message )
    {
        super( message );
    }

    public ValidationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ValidationException( Throwable cause )
    {
        super( cause );
    }
}
