[HOME](../../../README.md)
# scheduling:trigger

A trigger runs its job when the job being triggered
on enters the state specified.


Once the trigger's job runs, the trigger
will reflect the state of the job. The trigger will continue to
reflect its job's state until it is reset.

Subsequent state changes in
the triggering job are ignored until the trigger is reset and re-run.


If the triggering job is destroyed, because it is deleted or on a remote
server the trigger will enter an exception state.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [cancelWhen](#propertycancelWhen) | A state condition that will cause the trigger to cancel. | 
| [job](#propertyjob) | The job to run when the trigger fires. | 
| [name](#propertyname) | A name, can be any text. | 
| [newOnly](#propertynewOnly) | Fire trigger on new events only. | 
| [on](#propertyon) | The job the trigger will trigger on. | 
| [state](#propertystate) | The state condition which will cause the trigger to fire. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A simple trigger. |
| [Example 2](#example2) | A trigger that runs once two other jobs have completed. |
| [Example 3](#example3) | Cancelling a trigger. |
| [Example 4](#example4) | Examples Elsewhere. |


### Property Detail
#### cancelWhen <a name="propertycancelWhen"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to not cancelling.</td></tr>
</table>

A state condition that will cause the trigger
to cancel.

#### job <a name="propertyjob"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The job to run when the trigger fires.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### newOnly <a name="propertynewOnly"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

Fire trigger on new events only. If set the time on
the event will be compared with the last that this trigger received and
only a new event will cause the trigger to fire.

#### on <a name="propertyon"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The job the trigger will trigger on.

#### state <a name="propertystate"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to COMPLETE.</td></tr>
</table>

The state condition which will cause the trigger
to fire. See the Oddjob User guide for a full list of state
conditions.


### Examples
#### Example 1 <a name="example1"></a>

A simple trigger.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential name="Trigger Example">
            <jobs>
                <scheduling:trigger name="Alert Trigger" on="${important}" xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling">
                    <job>
                        <echo name="Alert">Important job has completed!</echo>
                    </job>
                </scheduling:trigger>
                <folder name="A Folder of Jobs">
                    <jobs>
                        <echo id="important" name="Run Me">I'm important</echo>
                    </jobs>
                </folder>
            </jobs>
        </sequential>
    </job>
</oddjob>

```


#### Example 2 <a name="example2"></a>

A trigger that runs once two other jobs have completed.

```xml
<oddjob xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling"
        xmlns:state="http://rgordon.co.uk/oddjob/state">
    <job>
        <sequential name="Trigger on Two Things">
            <jobs>
                <scheduling:trigger id="trigger"
                                    on="${watch-both}">
                    <job>
                        <echo id="notify"
                              name="Triggered Job">You ran two things!</echo>
                    </job>
                </scheduling:trigger>
                <state:and id="watch-both"
                           name="Watch Two Jobs">
                    <jobs>
                        <state:mirror job="${thing1}"
                                      name="Mirror Job 1"/>
                        <state:mirror job="${thing2}"
                                      name="Mirror Job 2"/>
                    </jobs>
                </state:and>
                <folder name="A Folder of Jobs">
                    <jobs>
                        <echo id="thing1"
                              name="Run me!">Thank you</echo>
                        <echo id="thing2"
                              name="Run me!">Thank you</echo>
                    </jobs>
                </folder>
            </jobs>
        </sequential>
    </job>
</oddjob>

```


#### Example 3 <a name="example3"></a>

Cancelling a trigger.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this" xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling">
    <job>
        <sequential name="Trigger Example">
            <jobs>
                <scheduling:trigger id="trigger" 
                		name="Alert Trigger" on="${our-job}"
                		state="FAILURE" cancelWhen="FINISHED">
                    <job>
                        <echo name="Alert" id="triggered-job">That job shouldn't fail!</echo>
                    </job>
                </scheduling:trigger>
                <folder name="A Folder of Jobs">
                    <jobs>
                        <echo id="our-job" name="Run Me">I won't fail</echo>
                    </jobs>
                </folder>
            </jobs>
        </sequential>
    </job>
</oddjob>

```


#### Example 4 <a name="example4"></a>

Examples Elsewhere.

- The scheduling example (<code>examples/scheduling/dailyftp.xml</code>) uses a trigger to send an email if one of the FTP transfers fails.



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
