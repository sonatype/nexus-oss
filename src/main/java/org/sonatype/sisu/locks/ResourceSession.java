/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.sisu.locks;

import java.io.OutputStream;
import java.net.URI;

public interface ResourceSession
    extends ResourceView
{
    OutputStream write( URI resource );

    void createFile( URI resource );

    void createFolder( URI resource );

    void copy( URI from, URI to );

    void move( URI from, URI to );

    void delete( URI resource );

    void commit();
}
