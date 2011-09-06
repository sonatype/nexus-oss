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
package org.sonatype.nexus.bundle.launcher.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Set;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Act as a registry of ports available to use in the JVM.
 * @author plynch
 */
@Singleton
@Named
public class DefaultPortReservationService implements PortReservationService {

    /**
     * Port registry
     */
    private final Set<Integer> ports = Sets.newHashSet();

    @Override
    public Integer reservePort() {
        int port = 0;
        int attempts = 0;
        boolean searchingForPort = true;
        synchronized (ports) {
            while (searchingForPort && ++attempts < 10) {
                port = findFreePort();
                searchingForPort = !ports.add(port);
            }
        }
        if (!(attempts < 10)) {
            throw new RuntimeException("Could not allocate a free port after " + attempts + " attempts.");
        }
        return port;
    }

    @Override
    public void cancelPort(Integer port) {
        Preconditions.checkNotNull(port);
        synchronized (ports) {
            if (!ports.remove(port)) {
                throw new IllegalArgumentException("port " + port + " not yet reserved by this service.");
            }
        }
    }

    /**
     * Find a random free system port.
     * @param portNumber
     * @return a free system port at the time this method was called.
     */
    protected Integer findFreePort() {
        ServerSocket server;
        try {
            server = new ServerSocket(0);
        } catch (IOException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }

        Integer portNumber = server.getLocalPort();
        try {
            server.close();
        } catch (IOException e) {
            throw new RuntimeException("Unable to release port " + portNumber, e);
        }
        return portNumber;
    }
}
