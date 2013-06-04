/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.yum.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.sisu.resource.scanner.Scanner;
import org.sonatype.sisu.resource.scanner.helper.ListenerSupport;
import com.google.common.collect.Sets;

/**
 * @since 3.0
 */
@Named
@Singleton
public class RpmScanner
{

    private final Scanner scanner;

    @Inject
    public RpmScanner( final @Named( "serial" ) Scanner scanner )
    {
        this.scanner = checkNotNull( scanner );
    }

    public Set<File> scan( final File basedDir )
    {
        final Set<File> rpms = Sets.newHashSet();

        scanner.scan( basedDir, new ListenerSupport()
        {
            @Override
            public void onFile( final File file )
            {
                if ( "rpm".equalsIgnoreCase( FileUtils.extension( file.getName() ) ) )
                {
                    rpms.add( file );
                }
            }
        } );

        return rpms;
    }

}
