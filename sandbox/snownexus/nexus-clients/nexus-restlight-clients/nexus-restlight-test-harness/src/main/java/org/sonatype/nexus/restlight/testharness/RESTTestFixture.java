package org.sonatype.nexus.restlight.testharness;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;

/**
 * <p>
 * Test fixture that supplies a set of expectations and a test-harness HTTP server for use in unit testing RESTLight
 * clients. The HTTP server uses the expectations setup in this fixture to validate client requests and send appropriate
 * reponses.
 * </p>
 * <p>
 * Failure to validate one or more request attributes MUST result in the test-harness server sending a failing HTTP
 * response code, and logging the specific nature of the failure to STDOUT.
 * </p>
 */
public interface RESTTestFixture
{

    /**
     * Retrieve the {@link Server} instance that acts as the mock-Nexus instance for the client under test.
     */
    Server getServer();

    /**
     * Retrieve the TCP port on which the test-harness server is listening.
     */
    int getPort();

    /**
     * Retrieve the {@link Handler} instance that will perform validation of client HTTP requests, and respond with
     * either the data specified in this fixture, or else an appropriate HTTP status code in case validation fails.
     */
    Handler getTestHandler();

    /**
     * Retrieve the state of the debug flag, which controls the verbosity of output from the test-harness HTTP server.
     */
    boolean isDebugEnabled();

    /**
     * Set the state of the debug flag, which controls the verbosity of output from the test-harness HTTP server.
     */
    void setDebugEnabled( boolean debugEnabled );

    /**
     * Start the test-harness HTTP server.
     */
    void startServer()
    throws Exception;

    /**
     * Stop the test-harness HTTP server.
     */
    void stopServer()
    throws Exception;

    /**
     * Make a cloned copy of this fixture, to provide an easy way for unit tests to setup complex expectations for one
     * exchange, then copy and modify the exchange for a subtle variation that could occur in another exchange within
     * the same conversation. This is primarily useful in conjunction with fixture implementations like
     * {@link ConversationalFixture}, which supply an ordered script of exchanges to the test-harness HTTP server for
     * validation.
     */
    RESTTestFixture copy();

}