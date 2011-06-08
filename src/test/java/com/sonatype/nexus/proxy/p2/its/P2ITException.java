/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.proxy.p2.its;

public class P2ITException
    extends Exception
{

    private static final long serialVersionUID = 1L;

    private int code;

    public int getCode()
    {
        return code;
    }

    public P2ITException( int code, StringBuffer buf )
    {
        super( "P2 return code was " + code + ":\n" + buf );
        this.code = code;
    }

}
