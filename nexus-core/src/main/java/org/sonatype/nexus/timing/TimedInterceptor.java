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
package org.sonatype.nexus.timing;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.lang.reflect.Method;

/**
 * Handles {@link Timed} method invocation timing.
 *
 * @since 2.4
 */
public class TimedInterceptor
    implements MethodInterceptor
{
    private static final Marker TIMING = MarkerFactory.getMarker("TIMING");

    private static final Logger log = LoggerFactory.getLogger(TimedInterceptor.class);

    // TODO: Sort out javasimon configuration, JMX and callbacks to handling logging or not, etc.
    // TODO: Probably want to do all of this ^^^ in a helper class

    // TODO: Could potentially implement a simple cache here if we think perf of name calculation is too high

    /**
     * Construct name from a {@link MethodInvocation}.  If {@link Timed#value()} is default/blank, then
     * a name will be constructed from the given {@link Method}.
     *
     * @see #nameOf(Method)
     */
    private String nameOf(final MethodInvocation invocation) {
        Method method = invocation.getMethod();
        Timed annotation = method.getAnnotation(Timed.class);
        String name = Timed.DEFAULT_VALUE;

        // This should not happen, as we only intercept methods which have @Timed on them, but just in-case
        if (annotation == null) {
            log.error("Missing @Timed annotation on method: {}", invocation);
            // will construct an automatic name
        }
        else {
            name = annotation.value();
        }

        // autodetect a name if needed
        if (StringUtil.isBlank(name)) {
            name = nameOf(method);
        }

        return name;
    }

    /**
     * Construct name from a {@link Method}.
     */
    private String nameOf(final Method method) {
        return String.format("%s.%s-%s",
            method.getDeclaringClass().getName(),
            method.getName(),
            method.getParameterTypes().length);
    }

    /**
     * Wrap method invocation with a Javasimon {@link Stopwatch} to capture timing metrics.
     */
    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        String name = nameOf(invocation);
        log.trace("Timing invocation: [{}] {}", name, invocation.getMethod());

        // TODO: Sort out how best to use javasimon here for core + plugins, etc.
        Stopwatch watch = SimonManager.getStopwatch(name);
        Split split = watch.start();
        try {
            return invocation.proceed();
        }
        finally {
            split.stop();

            // TODO: log.trace
            log.info(TIMING, "{}", watch);
        }
    }
}
