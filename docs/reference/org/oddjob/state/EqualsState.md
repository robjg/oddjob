[HOME](../../../README.md)
# state:equals

Runs it's child job and then compares the state of the child job to
the given state. It's own state is complete if the states match,
incomplete otherwise.


This job is probably most useful in it's 'not equals' form - i.e. to
check when something hasn't completed.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [job](#propertyjob) | The job to run who's state will be compared. | 
| [name](#propertyname) | A name, can be any text. | 
| [state](#propertystate) | The state to match. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | COMPLETE when the child job isn't complete. |


### Property Detail
#### job <a name="propertyjob"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The job to run who's state will be compared.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### state <a name="propertystate"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to COMPLETE.</td></tr>
</table>

The state to match.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Read only view of the internal stop flag.
This flag is cleared with a reset.


### Examples
#### Example 1 <a name="example1"></a>

COMPLETE when the child job isn't complete. This example
demonstrates how the <code>state:equals</code> job can be used to reverse
the meaning of the <code>exists</code> job. A request to
shutdown a database may complete asynchronously, and the only
way to tell if shutdown is complete is to check that the Database's
lock file has be removed. This example demonstrates how Oddjob
can check for this situation
before attempting to back up the database.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <scheduling:retry name="Database Backup" xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling">
            <schedule>
                <schedules:interval interval="00:00:02" xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"/>
            </schedule>
            <job>
                <sequential id="db-backup">
                    <jobs>
                        <state:equals state="!COMPLETE" xmlns:state="http://rgordon.co.uk/oddjob/state">
                            <job>
                                <state:resets harden="true">
                                    <job>
                                        <exists file="${db.lock.file}"/>
                                    </job>
                                </state:resets>
                            </job>
                        </state:equals>
                        <echo>Backing up the Database...</echo>
                    </jobs>
                </sequential>
            </job>
        </scheduling:retry>
    </job>
</oddjob>

```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
