Story: Scheduled Tasks User Interface

!-- User Interface Changes
!-- Button Bar
!-- Buttons present from left to right
!--    Refresh | Add | Run | Cancel | Delete

Scenario: default ui state on opening scheduled tasks
Given a User has permission to view Scheduled Tasks
When the User Clicks Scheduled Tasks link in left sidebar
Then the Scheduled Tasks Tab UI opens
And no tasks are selected
And the Scheduled Task Configuration south panel displays "Select a scheduled task to edit it, or click "Add" to create a new one."


Scenario: re-activating the scheduled tasks tab
Given a User has permission to view scheduled Tasks
And the scheduled tasks tab is already open
And the scheduled tasks tab is not activated
When the Scheduled Tasks tab is activated
Then the Scheduled Tasks UI is shown in its exact previously viewed state

Scenario: selecting more than one task in the task list
Given a User has permission to view scheduled tasks
And at least two tasks are shown
When a user selects task A
And then a user selects Task B while holding their cmd key so that two tasks are selected
Then the south panel displays configuration information for Task B
And the toolbar button state reflects Task B

Scenario: right-click menu
Given a User has permission to view scheduled tasks
And at least one task is listed
When a task is right clicked
Then the normal browser right click menu appears

