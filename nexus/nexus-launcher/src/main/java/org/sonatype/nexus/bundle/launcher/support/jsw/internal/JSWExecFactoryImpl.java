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
package org.sonatype.nexus.bundle.launcher.support.jsw.internal;

import org.sonatype.nexus.bundle.launcher.support.ant.AntHelper;
import org.sonatype.nexus.bundle.launcher.support.jsw.JSWExec;
import org.sonatype.nexus.bundle.launcher.support.jsw.JSWExecFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link JSWExecFactory} implementation.
 *
 * @since 1.9.3
 */
@Named
class JSWExecFactoryImpl
        implements JSWExecFactory {

    /**
     * ANT helper used by {@link JSWExecImpl}.
     */
    private AntHelper antHelper;

    /**
     * Constructor.
     *
     * @param antHelper ANT helper used by {@link JSWExecImpl}
     * @since 1.9.3
     */
    @Inject
    public JSWExecFactoryImpl(final AntHelper antHelper) {
        this.antHelper = checkNotNull(antHelper);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.9.3
     */
    @Override
    public JSWExec create(final File binDir, final String appName) {
        return new JSWExecImpl(binDir, appName, antHelper);
    }

}
