package org.sonatype.nexus.mock.pages;

import com.thoughtworks.selenium.Selenium;

public class ViewsPanel extends SidePanel {
    public ViewsPanel(Selenium selenium) {
        super(selenium, "window.Ext.getCmp('st-nexus-views')");
    }

    public boolean repositoriesAvailable() {
        return isLinkAvailable("Repositories");
    }

    public void repositoriesClick() {
        clickLink("Repositories");
    }

    public boolean systemFeedsAvailable() {
        return isLinkAvailable("System Feeds");
    }

    public void systemFeedsClick() {
        clickLink("System Feeds");
    }

    public boolean logsAndConfigFilesAvailable() {
        return isLinkAvailable("Logs and Config Files");
    }
}
