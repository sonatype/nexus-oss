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
package org.sonatype.nexus.plugins.rest;

import java.util.List;

/**
 * Used to look up plugins contributing proper require.js UI code.
 *
 * @since 2.4
 */
public interface UiContributor
{

    /**
     * Called on Nexus UI page load.
     * @param debug true if debug code should be delivered; false otherwise.
     * @return An object containing information about what to load for the UI.
     */
    UiContribution contribute(boolean debug);

    /**
     * Contains dependencies to load and the base module to use, for example:
     *
     * dependencies: ["js/static/plugin-all.js"]
     *
     * module name: "Nexus/plugin/bootstrap"
     *
     * where plugin-all.js contains require.js module definitions (e.g. `define("Nexus/plugin/bootstrap", ["Nexus/plugin/dep1"], function() {}` ).
     *
     * @since 2.4
     * @see UiContributionBuilder
     */
    public static class UiContribution
    {

        private List<String> dependencies;

        private String module;

        private boolean enabled;

        /**
         * @see UiContributionBuilder
         */
        public UiContribution( final String module, final List<String> dependencies, boolean enabled )
        {
            this.dependencies = dependencies;
            this.module = module;
            this.enabled = enabled;
        }

        /**
         * The dependencies to load before the module defined by {@link #getModule()} is required.
         * May be any valid require.js dependency name (url, module name, plugin)
         *
         * @return the list of dependencies to load.
         */
        public List<String> getDependencies() {
            return this.dependencies;
        }

        /**
         * The module that is used to initialize the plugin.
         * @return the module name.
         */
        public String getModule() {
            return this.module;
        }

        public boolean isEnabled()
        {
            return enabled;
        }
    }

}
