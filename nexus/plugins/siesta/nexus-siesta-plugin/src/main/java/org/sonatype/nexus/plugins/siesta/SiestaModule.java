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
package org.sonatype.nexus.plugins.siesta;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import org.sonatype.sisu.siesta.jackson.SiestaJacksonModule;
import org.sonatype.sisu.siesta.server.internal.ComponentDiscoveryApplication;
import org.sonatype.sisu.siesta.server.internal.SiestaServlet;
import org.sonatype.sisu.siesta.server.internal.jersey.SiestaJerseyModule;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Siesta plugin module.
 *
 * @since 2.3
 */
@Named
public class SiestaModule
    extends AbstractModule
{
    @Override
    protected void configure() {
        install(new org.sonatype.sisu.siesta.server.internal.SiestaModule());
        install(new SiestaJerseyModule());
        install(new SiestaJacksonModule());

        // Dynamically discover JAX-RS components
        bind(javax.ws.rs.core.Application.class).to(ComponentDiscoveryApplication.class).in(Singleton.class);

        install(new ServletModule()
        {
            @Override
            protected void configureServlets() {
                // FIXME: Resolve how we want to expose this, might want to add some structure here if we every want/plan/need-to support changing this again
                // FIXME: Maybe /service/<api>/ where <api> is siesta or local (for legacy)?  Since that part will never likely be used for what it was originally intended (a remoting/hostname mechanism IIUC).
                serve("/rest/*").with(SiestaServlet.class);
            }
        });
    }
}
