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

/*global NX, Ext, Nexus*/

/**
 * Container for icons used by capabilities.
 *
 * @since 2.2.2
 */
NX.define('Nexus.capabilities.Icons', {
    extend: 'Nexus.util.IconContainer',
    singleton: true,

    /**
     * @constructor
     */
    constructor: function () {
        var self = this;

        self.constructor.superclass.constructor.call(self, {
            stylePrefix: 'nx-capabilities-icon-',

            icons: {
                capability: 'brick.png',
                capability_add: 'brick_add.png',
                capability_delete: 'brick_delete.png',
                capability_new: 'brick_edit.png',
                capability_active: 'brick_valid.png',
                capability_passive: 'brick_error.png',
                capability_disabled: 'brick_grey.png',
                capability_error: 'brick_error.png',
                warning: 'error.png',
                refresh: 'arrow_refresh.png'
            }
        });
    }
});