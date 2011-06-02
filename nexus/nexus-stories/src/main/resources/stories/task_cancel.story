Story: Cancelling a running task.

Narrative:

In order to protect a stable Nexus from long running scheduled tasks which are consuming resources
As a Nexus Administrator
I want to Cancel a running task

Scenario: Task User interface

Given a User is viewing the Scheduled Tasks UI
When a user clicks Refresh button
Then the Cancel button is disabled

Given a User is viewing the Scheduled Tasks UI
When no task is selected
Then the Cancel button becomes disabled

Scenario: Cancelling a RUNNING internal task

!-- An Example of this task is when download indexes is enabled on a repository
Given a running task created and started internally by Nexus is displayed
When the running internal task is selected
Then  the Cancel button becomes enabled

Scenario: Cancelling a task that is already being cancelled
Given a running task created and started internally by Nexus is displayed
When the running internal task is selected
Then  the Cancel button becomes enabled

Scenario: Cancelling a RUNNING user created task

Given a RUNNING task is selected
When a user clicks the Cancel button
Then the user is asked for confirmation to Cancel the task, Yes or No

Given a user is prompted to confirm they would like to Cancel a running task
When the user clicks No
Then the Scheduled Tasks UI returns to its identical state before the confirmation dialog was displayed

Given a user is prompted to confirm they would like to Cancel a running task
When the user clicks Yes
Then the request to cancel is sent to the server
And the user is prompted with a dialog that displays an "Ok" button
And the dialog indicates the request to cancel has been sent

Given a user is prompted with a dialog confirming a task cancellation request has been sent
When the user clicks Ok
Then the Cancelled task status changes to SUBMITTED
And all other tasks listed remain in the state before the confirmation dialog was displayed
And the cancelled task remains selected in the task list

Given a User is viewing the Scheduled Tasks UI
And a RUNNING task is listed
When the RUNNING task is selected
Then the Cancel button is enabled

Given a User is viewing the Scheduled Tasks UI
And a not RUNNING task is listed
When a not RUNNING task is selected
Then the Cancel button is disabled

Scenario: Alert Email is configured for task
Given a task is running
And Alert Email for the task is set  with an email address
And Nexus is configured with a mail server
When a user explicitly cancels the running task
Then an email is NOT sent to the Alert Email

Scenario: Tasks which block updates to repo config
Given a repo is configured to not download indexes
And the repo has no index
When a user configures the repo config Download Indexes to true
And the user saves the repo configuration
And the RepairIndexTask is running
And the user clicks save for the repo configuration again
Then the RepairIndexTask blocks saving the repo configuration
And the user receives a request timeout message in the ui

