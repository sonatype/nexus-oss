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
package org.sonatype.nexus.repository.yum.internal.m2yum;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.AbstractIdContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;

@Component( role = ContentClass.class, hint = M2YumContentClass.ID )
public class M2YumContentClass
    extends AbstractIdContentClass
{
    private static final int STAGING_VALIDATION_METHOD_INDEX = 2;

    public static final String ID = "maven2yum";

    @Override
    public String getId()
    {
        if ( isStagingRepoValidation() )
        {
            return "maven2";
        }
        return ID;
    }

    protected boolean isStagingRepoValidation()
    {
        try
        {
            throw new RuntimeException();
        }
        catch ( RuntimeException e )
        {
            final StackTraceElement[] elements = e.getStackTrace();
            if ( elements.length > STAGING_VALIDATION_METHOD_INDEX
                && isThrownByStagingValidationMethod( elements[STAGING_VALIDATION_METHOD_INDEX] ) )
            {
                return true;
            }
        }
        return false;
    }

    protected boolean isThrownByStagingValidationMethod( StackTraceElement elem )
    {
        return "com.sonatype.nexus.staging.api.AbstractStagingProfilePlexusResource".equals( elem.getClassName() )
            && "validateProfile".equals( elem.getMethodName() );
    }

    @Override
    public boolean isCompatible( ContentClass contentClass )
    {
        return ID.equals( contentClass.getId() ) || Maven2ContentClass.ID.equals( contentClass.getId() );
    }
}
