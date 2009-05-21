package org.sonatype.nexus.mock;

import org.sonatype.nexus.mock.models.User;
import org.sonatype.nexus.mock.pages.ViewsPanel;
import org.sonatype.nexus.mock.pages.AdministrationPanel;
import org.sonatype.nexus.mock.pages.SecurityPanel;
import static org.junit.Assert.*;
import org.junit.Test;

public class RolesUITest extends SeleniumTest {
    @Test
    public void admin() {
        main.clickLogin().populate(User.ADMIN).loginExpectingSuccess();

        // views/repositories
        ViewsPanel viewsPanel = main.viewsPanel();
        assertTrue("Repositories link should be visible", viewsPanel.repositoriesAvailable());
        assertTrue("System Feeds link should be visible", viewsPanel.systemFeedsAvailable());
        assertTrue("Logs and Config Files link should be visible", viewsPanel.logsAndConfigFilesAvailable());

        // now collapse the panel and check
        viewsPanel.collapse();
        assertFalse("Repositories link should not be visible", viewsPanel.repositoriesAvailable());
        assertFalse("System Feeds link should not be visible", viewsPanel.systemFeedsAvailable());
        assertFalse("Logs and Config Files link should not be visible", viewsPanel.logsAndConfigFilesAvailable());

        // administration
        AdministrationPanel adminPanel = main.adminPanel();
        assertTrue("Server link should be visible", adminPanel.serverAvailable());
        assertTrue("Routing link should be visible", adminPanel.routingAvailable());
        assertTrue("Scheduled Tasks link should be visible", adminPanel.scheduleTasksAvailable());
        assertTrue("Repository Targets link should be visible", adminPanel.repositoryTargetsAvailable());
        assertTrue("Log link should be visible", adminPanel.logAvailable());

        // now collapse the panel and check
        adminPanel.collapse();
        assertFalse("Server link should not be visible", adminPanel.serverAvailable());
        assertFalse("Routing link should not be visible", adminPanel.routingAvailable());
        assertFalse("Scheduled Tasks link should not be visible", adminPanel.scheduleTasksAvailable());
        assertFalse("Repository Targets link should not be visible", adminPanel.repositoryTargetsAvailable());
        assertFalse("Log link should not be visible", adminPanel.logAvailable());

        // security
        SecurityPanel securityPanel = main.securityPanel();
        assertTrue("Change Password link should be visible", securityPanel.changePasswordAvailable());
        assertTrue("Users link should be visible", securityPanel.usersAvailable());
        assertTrue("Roles link should be visible", securityPanel.rolesAvailable());
        assertTrue("Privileges link should be visible", securityPanel.privilegesAvailable());

        //now collapse and check again
        securityPanel.collapse();
        assertFalse("Change Password link should not be visible", securityPanel.changePasswordAvailable());
        assertFalse("Users link should not be visible", securityPanel.usersAvailable());
        assertFalse("Roles link should not be visible", securityPanel.rolesAvailable());
        assertFalse("Privileges link should not be visible", securityPanel.privilegesAvailable());
    }

    @Test
    public void roleAdmin() {
        main.clickLogin().populate(User.ROLE_ADMIN).loginExpectingSuccess();

        // views/repositories
        ViewsPanel viewsPanel = main.viewsPanel();
        assertTrue("Views panel should not be disaplyed at all", viewsPanel.hidden());

        // administration
        AdministrationPanel adminPanel = main.adminPanel();
        assertTrue("Admin panel should not be disaplyed at all", adminPanel.hidden());

        // security
        SecurityPanel securityPanel = main.securityPanel();
        assertTrue("Change Password link should be visible", securityPanel.changePasswordAvailable());
        assertFalse("Users link should not be visible", securityPanel.usersAvailable());
        assertTrue("Roles link should be visible", securityPanel.rolesAvailable());
        assertFalse("Privileges link should not be visible", securityPanel.privilegesAvailable());
    }
}
