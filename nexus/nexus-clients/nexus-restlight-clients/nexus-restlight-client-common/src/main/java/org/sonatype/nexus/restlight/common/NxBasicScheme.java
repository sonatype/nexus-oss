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

import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.BasicScheme;

/**
 * {@link AuthScheme} for use with commons-httpclient that implements Nexus' NxBASIC
 * HTTP authentication scheme. This is just an extension of {@link BasicScheme} that uses the name
 * 'NxBASIC' for registration with httpclient.
 */
public class NxBasicScheme
    extends BasicScheme
{

    static final String NAME = "NxBASIC";

    public static final String POLICY_NAME = NAME;

    @Override
    public String getSchemeName()
    {
        return NAME;
    }

    @Override
    public String getID()
    {
        return NAME;
    }
}
