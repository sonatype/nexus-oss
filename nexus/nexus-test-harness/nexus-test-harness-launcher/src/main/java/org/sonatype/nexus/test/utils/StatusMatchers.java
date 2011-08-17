/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.test.utils;

import org.hamcrest.Factory;
import org.sonatype.nexus.test.utils.NexusRequestMatchers.HasCode;
import org.sonatype.nexus.test.utils.NexusRequestMatchers.IsError;
import org.sonatype.nexus.test.utils.NexusRequestMatchers.IsSuccess;

public class StatusMatchers
{

    @Factory
    public static <T> IsSuccess isSuccess()
    {
        return new IsSuccess();
    }

    @Factory
    public static <T> IsError isError()
    {
        return new IsError();
    }

    @Factory
    public static <T> HasCode hasStatusCode( int expectedCode )
    {
        return new HasCode( expectedCode );
    }

    @Factory
    public static <T> HasCode isNotFound()
    {
        return new HasCode( 404 );
    }
}
