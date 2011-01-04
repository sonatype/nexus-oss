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
package org.sonatype.nexus.gwt.ui.client.repository;

import org.sonatype.nexus.gwt.ui.client.data.IterableDataStore;
import org.sonatype.nexus.gwt.ui.client.form.ListBoxInput;


/**
 *
 * @author barath
 */
public class VirtualRepositoryPage extends RepositoryPage {
    
    private IterableDataStore repositories;

    public VirtualRepositoryPage(IterableDataStore repositories) {
        super("virtual");
        this.repositories = repositories;
    }

    protected void addTypeSpecificInputs() {
        RepositoriesListBox lb = new RepositoriesListBox(repositories);
        lb.setName("shadowOf");
        getModel().addInput("shadowOf", new ListBoxInput(lb));
        addRow(i18n.shadowOf(), lb);

        addRow(i18n.format(), createRadioButtonGroup("format",
               new String[][] {{"maven1", i18n.formatMaven1()},
                               {"maven2", i18n.formatMaven2()}}));

        addRow(createCheckBox("syncAtStartup", i18n.syncAtStartup()));
    }
    
}
