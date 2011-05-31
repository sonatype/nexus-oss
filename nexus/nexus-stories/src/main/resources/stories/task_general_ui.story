Story: Scheduled Tasks User Interface

!-- User Interface Changes
!-- Button Bar
!-- Buttons present from left to right
!--    Refresh | Add | Run | Cancel | Delete
!--  Remove the right click menu from task rows
!-- Manage enabled/disabled state of relevant buttons

Given a User has permission to view Scheuled Tasks
When the User Clicks Scheduled Tasks link in left sidebar
Then the Scheduled Tasks Tab UI opens
And no tasks are selected
And the Scheduled Task Configuration south panel displays "Select a scheduled task to edit it, or click "Add" to create a new one."


!-- add bits about multiple task selections
!-- currently multiple tasks can be selected at the same time but then
!-- the toolbar action is only performed on the last selected task

