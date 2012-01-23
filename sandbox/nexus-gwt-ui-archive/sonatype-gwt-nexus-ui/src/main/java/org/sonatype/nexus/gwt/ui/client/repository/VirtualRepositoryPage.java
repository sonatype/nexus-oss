/**
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
