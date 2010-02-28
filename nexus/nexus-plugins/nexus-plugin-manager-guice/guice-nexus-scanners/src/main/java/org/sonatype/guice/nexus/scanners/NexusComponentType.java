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

/**
 * Enumeration of Nexus component types.
 */
enum NexusComponentType
{
    // ----------------------------------------------------------------------
    // Values
    // ----------------------------------------------------------------------

    UNKNOWN
    {
        @Override
        boolean isComponent()
        {
            return false;
        }
    },

    EXTENSION_POINT
    {
        @Override
        boolean isSingleton()
        {
            return false;
        }

        @Override
        NexusComponentType toSingleton()
        {
            return EXTENSION_POINT_SINGLETON;
        }
    },

    EXTENSION_POINT_SINGLETON,

    MANAGED
    {
        @Override
        boolean isSingleton()
        {
            return false;
        }

        @Override
        NexusComponentType toSingleton()
        {
            return MANAGED_SINGLETON;
        }
    },

    MANAGED_SINGLETON;

    // ----------------------------------------------------------------------
    // Common methods
    // ----------------------------------------------------------------------

    boolean isComponent()
    {
        return true;
    }

    NexusComponentType toSingleton()
    {
        return this;
    }

    boolean isSingleton()
    {
        return true;
    }
}