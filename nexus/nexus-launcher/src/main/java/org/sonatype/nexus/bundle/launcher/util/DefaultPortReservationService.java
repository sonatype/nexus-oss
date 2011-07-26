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
