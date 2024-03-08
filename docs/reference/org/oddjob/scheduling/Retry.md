[HOME](../../../README.md)
# scheduling:retry

This is a timer that runs it's job according to the schedule until
the schedule expires or the job completes successfully.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [clock](#propertyclock) | The clock to use. | 
| [current](#propertycurrent) | This is the current/next result from the schedule. | 
| [haltOn](#propertyhaltOn) | The state of the Child Job from the nextDue property when the job begins to execute. | 
| [job](#propertyjob) | The job to run when it's due. | 
| [lastDue](#propertylastDue) | The time the schedule was lastDue. | 
| [limits](#propertylimits) | Used to limit the schedule. | 
| [name](#propertyname) | A name, can be any text. | 
| [reset](#propertyreset) |  | 
| [schedule](#propertyschedule) | The Schedule used to provide execution times. | 
| [timeZone](#propertytimeZone) | The time zone the schedule is to run in. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | File Polling. |
| [Example 2](#example2) | Using Retry with a Timer. |


### Property Detail
#### clock <a name="propertyclock"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Set automatically.</td></tr>
</table>

The clock to use. Tells the current time.

#### current <a name="propertycurrent"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Set automatically.</td></tr>
</table>

This is the current/next result from the
schedule. This properties fromDate is used to set the nextDue date for
the schedule and it's useNext (normally the same as toDate) property is
used to calculate the following new current property after execution. This
property is most useful for the Timer to pass limits to
the Retry, but is also useful for diagnostics.

#### haltOn <a name="propertyhaltOn"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The state of the Child Job
from the nextDue property when the job begins to execute.

#### job <a name="propertyjob"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The job to run when it's due.

#### lastDue <a name="propertylastDue"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The time the schedule was lastDue. This is set
from the nextDue property when the job begins to execute.

#### limits <a name="propertylimits"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Used to limit the schedule. Usually this
will be configured to be a parent timer's current interval.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### reset <a name="propertyreset"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>



#### schedule <a name="propertyschedule"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The Schedule used to provide execution
times.

#### timeZone <a name="propertytimeZone"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Set automatically.</td></tr>
</table>

The time zone the schedule is to run
in. This is the text id of the time zone, such as "Europe/London".
More information can be found at
<a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/TimeZone.html">
TimeZone</a>.


### Examples
#### Example 1 <a name="example1"></a>

File Polling. Check every 5 seconds for a file.

```xml
<oddjob id="this">
    <job>
        <scheduling:retry name="File Polling Example"
            xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling"
            xmlns:schedules="http://rgordon.co.uk/oddjob/schedules">
            <schedule>
                <schedules:interval interval="00:00:05"/>
            </schedule>
            <job>
                <sequential id="look" name="Look for files">
                    <jobs>
                        <exists file="${this.dir}/work/*.foo" id="check"
                            name="Check File Exists"/>
                        <echo name="Echo to Console">Found ${check.exists[0]}</echo>
                    </jobs>
                </sequential>
            </job>
        </scheduling:retry>
    </job>
</oddjob>

```


#### Example 2 <a name="example2"></a>

Using Retry with a Timer. A daily job retries twice.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
        xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling"
        xmlns:state="http://rgordon.co.uk/oddjob/state">
    <job>
        <scheduling:timer id="timer" >
            <schedule>
                <schedules:daily from="08:00"/>
            </schedule>
            <job>
                <scheduling:retry id="retry">
                    <schedule>
                        <schedules:count count="2"/>
                    </schedule>
                    <job>
                        <state:flag id="flag-job" state="EXCEPTION"/>
                    </job>
                </scheduling:retry>
            </job>
        </scheduling:timer>
    </job>
</oddjob>

```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
