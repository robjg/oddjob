[HOME](../../../README.md)
# scheduling:timer

Provides a simple timer for periodic or once only
execution of the child job.



<h4>Schedules</h4>

Once only execution:

- [schedules:time](../../../org/oddjob/schedules/schedules/TimeSchedule.md)
- [schedules:date](../../../org/oddjob/schedules/schedules/DateSchedule.md)
- [schedules:count](../../../org/oddjob/schedules/schedules/CountSchedule.md)(With a count of 1)

Recurring executions:

- [schedules:yearly](../../../org/oddjob/schedules/schedules/YearlySchedule.md)
- [schedules:monthly](../../../org/oddjob/schedules/schedules/MonthlySchedule.md)
- [schedules:weekly](../../../org/oddjob/schedules/schedules/WeeklySchedule.md)
- [schedules:daily](../../../org/oddjob/schedules/schedules/DailySchedule.md)
- [schedules:interval](../../../org/oddjob/schedules/schedules/IntervalSchedule.md)

Holidays:

- [schedules:broken](../../../org/oddjob/schedules/schedules/BrokenSchedule.md)
- [schedules:day-after](../../../org/oddjob/schedules/schedules/DayAfterSchedule.md)
- [schedules:day-before](../../../org/oddjob/schedules/schedules/DayBeforeSchedule.md)


<h4>Missed Executions</h4>


If Oddjob is running with a persister missed executions fire immediately one
after the other until all missed executions have run.


This can be overridden with the <code>skipMissedRuns</code> property.


If a timer is started after the initial execution time but within the interval
of the schedule - execution will happen immediately. Extended intervals are created
using the <code>from</code> properties instead of the <code>at/in/on</code>
properties of schedules.

<h4>Changing The Next Due Time</h4>

There are two ways to change the next due date of a timer. They both
require that the timer has been started but is not yet executing, and they
both involve dynamically setting properties of the job which can be done
via the 'Job' -&gt; 'Set Property' menu item in Oddjob Explorer or via
the [set](../../../org/oddjob/values/SetJob.md) job within Oddjob.


The first method is to set the next due date directly with the
<code>nextDue</code> property. The existing timer is cancelled and the
job rescheduled to run at this time. If the time is in the past, the job
will run immediately.


The second method is to set the the <code>reschedule</code> property with
a date and time. The next due date is calculated by applying the date
and time the schedule. This is particularly useful for advancing a
timer.

<h4>Retrying Failed Jobs</h4>

Nest a [scheduling:retry](../../../org/oddjob/scheduling/Retry.md) job.

<h4>Recording the Outcome of Runs</h4>

Nest an [archive](../../../org/oddjob/persist/ArchiveJob.md).

<h4>Distributed Scheduling</h4>

Nest a [grab](../../../org/oddjob/jobs/GrabJob.md).

<h4>For More Information</h4>

For more information see the Scheduling section of the User Guide.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [clock](#propertyclock) | The clock to use. | 
| [current](#propertycurrent) | This is the current/next result from the schedule. | 
| [haltOn](#propertyhaltOn) | The state of the Child Job from the nextDue property when the job begins to execute. | 
| [haltOnFailure](#propertyhaltOnFailure) | Don't reschedule if the scheduled job doesn't complete. | 
| [job](#propertyjob) | The job to run when it's due. | 
| [lastDue](#propertylastDue) | The time the schedule was lastDue. | 
| [name](#propertyname) | A name, can be any text. | 
| [reset](#propertyreset) |  | 
| [schedule](#propertyschedule) | The Schedule used to provide execution times. | 
| [skipMissedRuns](#propertyskipMissedRuns) | Use the current time, not the last completed time to calculate when the job is next due. | 
| [timeZone](#propertytimeZone) | The time zone the schedule is to run in. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A Timer that runs at 10am each day, Monday to Friday. |
| [Example 2](#example2) | Run once at 10am or any time after. |
| [Example 3](#example3) | Use a timer to stop a long running job. |
| [Example 4](#example4) | Manually setting the next due date of the timer. |
| [Example 5](#example5) | Manually rescheduling the timer. |


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

#### haltOnFailure <a name="propertyhaltOnFailure"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Don't reschedule if the scheduled job doesn't
complete.

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

#### skipMissedRuns <a name="propertyskipMissedRuns"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Use the current time, not the last completed time
to calculate when the job is next due.

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

A Timer that runs at 10am each day, Monday to Friday.

```xml
<oddjob>
    <job>
        <scheduling:timer xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling"
            xmlns:schedules="http://rgordon.co.uk/oddjob/schedules" id="timer">
            <schedule>
                <schedules:weekly from="Monday" to="Friday">
                    <refinement>
                        <schedules:daily at="10:00"/>
                    </refinement>
                </schedules:weekly>
            </schedule>
            <job>
                <echo id="work">Doing some work at ${timer.current.fromDate}</echo>
            </job>
        </scheduling:timer>
    </job>
</oddjob>

```


#### Example 2 <a name="example2"></a>

Run once at 10am or any time after.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <echo id="big-report" name="Pretend this is a Long Running Report">Meaning of Life: 42</echo>
                <scheduling:timer id="timer" xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling">
                    <schedule>
                        <schedules:time from="10:00" xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"/>
                    </schedule>
                    <job>
                        <echo name="Pretend this Forwards the Long Running Report">Emailing: ${big-report.text}</echo>
                    </job>
                </scheduling:timer>
            </jobs>
        </sequential>
    </job>
</oddjob>

```


If the report completes before 10am the timer will schedule it to be e-mailed
at 10am. If the report completes after 10am it is e-mailed immediately.

#### Example 3 <a name="example3"></a>

Use a timer to stop a long running job.

```xml
<oddjob>
    <job>
        <sequential id="main" name="Stop Overdue Job">
            <jobs>
                <scheduling:timer id="timer" xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling">
                    <schedule>
                        <schedules:count count="1" xmlns:schedules="http://rgordon.co.uk/oddjob/schedules">
                            <refinement>
                                <schedules:after>
                                    <schedule>
                                        <schedules:interval interval="00:00:10"/>
                                    </schedule>
                                </schedules:after>
                            </refinement>
                        </schedules:count>
                    </schedule>
                    <job>
                         <stop job="${long-job}" name="Stop Long Running Job"/>
                    </job>
                </scheduling:timer>
                <wait id="long-job" name="A Long Running Job"/>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


The job will be stopped after 10 seconds. If the job has already completed
the stop will have no affect.

#### Example 4 <a name="example4"></a>

Manually setting the next due date of the timer. When the set job is
run manually the job will be schedule to run at the new time.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <scheduling:timer id="timer" xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling">
                    <schedule>
                        <schedules:date on="9999-12-31" xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"/>
                    </schedule>
                    <clock>
                        <value value="${clock}"/>
                    </clock>
                    <job>
                        <echo id="echo"><![CDATA[Running at ${timer.current.fromDate}]]></echo>
                    </job>
                </scheduling:timer>
                <folder>
                    <jobs>
                        <set id="set">
                            <values>
                                <date date="2012-12-27 08:02" key="timer.nextDue"/>
                            </values>
                        </set>
                    </jobs>
                </folder>
            </jobs>
        </sequential>
    </job>
</oddjob>

```


Note that the <code>current<code> interval property is not changed, so
the echo job shows 'Running at 9999-12-31 00:00:00.000'.

#### Example 5 <a name="example5"></a>

Manually rescheduling the timer. When the set job is run manually, the
timer will advance to it's next scheduled slot.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <scheduling:timer id="timer" xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling">
                    <schedule>
                        <schedules:daily at="23:00" xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"/>
                    </schedule>
                    <clock>
                        <value value="${clock}"/>
                    </clock>
                    <job>
                        <echo id="echo"><![CDATA[Running at ${timer.current.fromDate}]]></echo>
                    </job>
                </scheduling:timer>
                <folder>
                    <jobs>
                        <set id="set">
                            <values>
                                <date date="TOMORROW" key="timer.reschedule">
                                    <clock>
                                        <value value="${clock}"/>
                                    </clock>
                                </date>
                            </values>
                        </set>
                    </jobs>
                </folder>
            </jobs>
        </sequential>
    </job>
</oddjob>

```


Note that the unlike above, <code>current<code> interval property
changes when the time is rescheduled.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
