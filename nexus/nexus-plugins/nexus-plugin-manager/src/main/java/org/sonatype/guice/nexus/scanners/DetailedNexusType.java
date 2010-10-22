/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.guice.nexus.scanners;

import java.lang.annotation.Annotation;

final class DetailedNexusType
    implements NexusType
{
    private final boolean isSingleton;

    private final Annotation details;

    DetailedNexusType( final Annotation details )
    {
        isSingleton = false;
        this.details = details;
    }

    private DetailedNexusType( final boolean isSingleton, final Annotation details )
    {
        this.isSingleton = isSingleton;
        this.details = details;
    }

    public boolean isComponent()
    {
        return true;
    }

    public boolean isSingleton()
    {
        return isSingleton;
    }

    public DetailedNexusType asSingleton()
    {
        return isSingleton ? this : new DetailedNexusType( true, details );
    }

    public Annotation details()
    {
        return details;
    }
}