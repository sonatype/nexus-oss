/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.sonatype.nexus.proxy.maven.metadata.operations;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;

/**
 * Snapshot storage
 * 
 * @author Oleg Gusakov
 * @version $Id: SnapshotOperand.java 726701 2008-12-15 14:31:34Z hboutemy $
 */
public class SnapshotOperand
    extends AbstractOperand
{

    private Snapshot snapshot;

    private List<SnapshotVersion> snapshotVersions;

    public SnapshotOperand( Snapshot data, SnapshotVersion... snapshotVersions )
    {
        this( data, Arrays.asList( snapshotVersions ) );
    }

    public SnapshotOperand( Snapshot data, List<SnapshotVersion> snapshotVersions )
    {
        this.snapshot = data;
        this.snapshotVersions = snapshotVersions;
    }

    public Snapshot getSnapshot()
    {
        return snapshot;
    }

    public List<SnapshotVersion> getSnapshotVersions()
    {
        return snapshotVersions;
    }

}
