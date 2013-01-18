/*
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
package org.sonatype.nexus.proxy.maven.wl.discovery;

import java.io.IOException;

import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;
import org.sonatype.nexus.proxy.walker.Walker;

/**
 * Local strategy is used to discover local content. It might rely on different techniques, like using {@link Walker} or
 * such.
 * 
 * @author cstamas
 * @since 2.4
 */
public interface LocalStrategy
    extends Strategy
{
    /**
     * Discovers the local content of the given {@link MavenRepository}.
     * 
     * @param mavenRepository to have local content discovered.
     * @return the entry source with discovered entries.
     * @throws StrategyFailedException if "soft" failure detected.
     * @throws IOException in case of IO problem.
     */
    EntrySource discover( MavenRepository mavenRepository )
        throws StrategyFailedException, IOException;
}
