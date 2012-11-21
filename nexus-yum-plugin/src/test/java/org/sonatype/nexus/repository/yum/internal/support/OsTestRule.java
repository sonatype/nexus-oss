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
package org.sonatype.nexus.repository.yum.internal.support;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class OsTestRule
    implements TestRule
{

    @Override
    public Statement apply( final Statement statement, final Description description )
    {
        return new Statement()
        {

            @Override
            public void evaluate()
                throws Throwable
            {
                IgnoreOn ignoreOn = description.getAnnotation( IgnoreOn.class );
                if ( ignoreOn == null || !matches( ignoreOn.value() ) )
                {
                    statement.evaluate();
                }
            }

            private boolean matches( String[] osNames )
            {
                if ( osNames != null )
                {
                    String systemOsName = System.getProperty( "os.name" ).toLowerCase();
                    for ( String osName : osNames )
                    {
                        if ( systemOsName.contains( osName.toLowerCase() ) )
                        {
                            return true;
                        }
                    }
                }

                return false;
            }
        };
    }

}
