package org.sonatype.nexus.bundle.launcher.util;

/**
 * Service that reserves free system ports.
 * <p>
 * Ports are only guaranteed freely available at port reservation time.
 */
public interface PortReservationService {

    /**
     * Reserve a port for use
     * @return a free port at time of method call.
     */
    Integer reservePort();

    /**
     * Cancel the reservation of the specified port, indicating the service shall make it available for future reservations.
     * @param port the port to unreserve
     * @throws IllegalArgumentException if the specified port has not been reserved
     */
    void cancelPort(Integer port);

}
