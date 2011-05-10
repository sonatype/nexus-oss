Scenario: Deleting a SLEEPING task

!-- need to verify the interaction expected here


Scenario: Deleting a running task

Given a User is viewing the Scheduled Tasks UI
When no task is selected
Then the Delete button becomes disabled

Given a User is viewing the Scheduled Tasks UI
When a task is selected
Then the Delete button becomes enabled

Given a User is viewing the Scheduled Tasks UI
And a RUNNING task is selected
When a user clicks the Delete button
Then the user is asked for confirmation to delete the task, Yes or No

Given a user is prompted to confirm they would like to delete a running task
When the user clicks No
Then the Scheduled Tasks UI returns to its identical state before the confirmation dialog was displayed

Given a user is prompted to confirm they would like to delete a running task
When the user clicks Yes
Then the task is scheduled to be cancelled
And the task is scheduled to be deleted
And the task is removed from the UI immediately
