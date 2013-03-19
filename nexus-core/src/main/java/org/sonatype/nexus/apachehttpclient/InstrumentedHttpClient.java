package org.sonatype.nexus.apachehttpclient;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.httpclient.InstrumentedClientConnManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.client.*;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

// NOTE: Duplicated and augmented from original 2.2.0 source to change signature of CTOR ClientConnectionManager parameter
// NOTE: Should get this changes into metrics-httpclient and avoid needing this

public class InstrumentedHttpClient extends DefaultHttpClient {
    private final Log log = LogFactory.getLog(getClass());

    private final MetricsRegistry registry;

    public InstrumentedHttpClient(MetricsRegistry registry,
                                  ClientConnectionManager manager,
                                  HttpParams params) {
        super(manager, params);
        this.registry = registry;
    }

    public InstrumentedHttpClient(ClientConnectionManager manager, HttpParams params) {
        this(Metrics.defaultRegistry(), manager, params);
    }

    public InstrumentedHttpClient(HttpParams params) {
        this(new InstrumentedClientConnManager(), params);
    }

    public InstrumentedHttpClient() {
        this(null);
    }

    @Override
    protected RequestDirector createClientRequestDirector(HttpRequestExecutor requestExec,
                                                          ClientConnectionManager conman,
                                                          ConnectionReuseStrategy reustrat,
                                                          ConnectionKeepAliveStrategy kastrat,
                                                          HttpRoutePlanner rouplan,
                                                          HttpProcessor httpProcessor,
                                                          HttpRequestRetryHandler retryHandler,
                                                          RedirectStrategy redirectStrategy,
                                                          AuthenticationStrategy targetAuthStrategy,
                                                          AuthenticationStrategy proxyAuthStrategy,
                                                          UserTokenHandler userTokenHandler,
                                                          HttpParams params) {
        return new InstrumentedRequestDirector(
                registry,
                log,
                requestExec,
                conman,
                reustrat,
                kastrat,
                rouplan,
                httpProcessor,
                retryHandler,
                redirectStrategy,
                targetAuthStrategy,
                proxyAuthStrategy,
                userTokenHandler,
                params);
    }
}