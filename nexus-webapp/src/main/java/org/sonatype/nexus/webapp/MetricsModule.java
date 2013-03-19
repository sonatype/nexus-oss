package org.sonatype.nexus.webapp;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.reporting.HealthCheckServlet;
import com.yammer.metrics.reporting.MetricsServlet;
import com.yammer.metrics.reporting.PingServlet;
import com.yammer.metrics.reporting.ThreadDumpServlet;
import com.yammer.metrics.util.DeadlockHealthCheck;
import com.yammer.metrics.web.DefaultWebappMetricsFilter;

import javax.inject.Named;

/**
 * ???
 *
 * @since 2.4
 */
public class MetricsModule
    extends AbstractModule
{
    @Override
    protected void configure() {
        // NOTE: AdminServletModule (metrics-guice intgegration) generates invalid links, so wire up servlets ourselves

        install(new ServletModule()
        {
            @Override
            protected void configureServlets() {
                Clock clock = Clock.defaultClock();
                bind(Clock.class).toInstance(clock);

                VirtualMachineMetrics virtualMachineMetrics = VirtualMachineMetrics.getInstance();
                bind(VirtualMachineMetrics.class).toInstance(virtualMachineMetrics);

                JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());
                bind(JsonFactory.class).toInstance(jsonFactory);

                HealthCheckRegistry healthCheckRegistry = HealthChecks.defaultRegistry();
                bind(HealthCheckRegistry.class).toInstance(healthCheckRegistry);

                healthCheckRegistry.register(new DeadlockHealthCheck(virtualMachineMetrics));

                MetricsRegistry metricsRegistry = Metrics.defaultRegistry();
                bind(MetricsRegistry.class).toInstance(metricsRegistry);

                serve("/internal/ping").with(new PingServlet());

                serve("/internal/threads").with(new ThreadDumpServlet(virtualMachineMetrics));

                serve("/internal/metrics").with(new MetricsServlet(
                    clock,
                    virtualMachineMetrics,
                    metricsRegistry,
                    jsonFactory,
                    true
                ));

                serve("/internal/healthcheck").with(new HealthCheckServlet(healthCheckRegistry));

                filter("/*").through(new DefaultWebappMetricsFilter());
            }
        });
    }
}
