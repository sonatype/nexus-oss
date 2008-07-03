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
