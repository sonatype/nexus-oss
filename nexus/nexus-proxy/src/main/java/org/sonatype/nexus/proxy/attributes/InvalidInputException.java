/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.attributes;

import java.io.IOException;

/**
 * Exception thrown when {@link Marshaller} does not hit "plain" IOException, is able to read the content but it detects
 * the content as corrupt, unreadable or in any other way unexpectedly formatted and unprocessable.
 * 
 * @author cstamas
 * @since 1.10.0
 */
public class InvalidInputException
    extends IOException
{
    private static final long serialVersionUID = -1729104533714236869L;

    public InvalidInputException( String message )
    {
        super( message );
    }

    public InvalidInputException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
