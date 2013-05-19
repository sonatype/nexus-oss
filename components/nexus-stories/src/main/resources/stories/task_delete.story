Scenario: default Delete button behavior

Given a User is viewing the Scheduled Tasks UI
When no task is selected
Then the Delete button becomes disabled

Given a User is viewing the Scheduled Tasks UI
When a task is selected
Then the Delete button becomes enabled

Scenario: Confirm task deletion

Given a User has selected a deletable task
When a user clicks the Delete button
Then the user is asked for confirmation to delete the task, Yes or No

Given a user is prompted to confirm they would like to delete a running task
When the user clicks No
Then the Scheduled Tasks UI returns to its identical state before the confirmation dialog was displayed

Given a user is prompted to confirm they would like to delete a running task
When the user clicks Yes
Then the Scheduled Tasks task list is not Refreshed


Scenario: Deleting a RUNNING task that cannot be stopped immediately

Given a user is prompted to confirm they would like to delete a RUNNING task
And the task being deleted can be stopped immediately
When the user clicks Yes
Then the task is scheduled to be cancelled
And the task is scheduled to be deleted
And the task is removed from the UI immediately

Scenario: Deleting a RUNNING task that can be stopped immediately

Given a user is prompted to confirm they would like to delete a RUNNING task
And the task being deleted can be stopped immediately
When the user clicks Yes
Then the task is scheduled to be cancelled
And the task is scheduled to be deleted
And the task remains in the task list
And the task status changes to CANCELLING


Scenario: Deleting a task that no longer exists on the server

Given a user is prompted to confirm they would like to delete a task
And the task no longer exists on the server.
When the user clicks Yes
Then a dialog is displayed indicating the task does not exist
!-- And the task should be removed from the UI immediately

Scenario: Deleting a BLOCKED task

Given a user is prompted to confirm they would like to delete a BLOCKED task
When the user clicks Yes
Then the task is scheduled to be deleted
And the task is removed from the UI immediately

Scenario: Deleting a CANCELLING task

Given a user is prompted to confirm they would like to delete a task with CANCELLING status
When the user clicks Yes
Then the task is removed from the UI immediately




