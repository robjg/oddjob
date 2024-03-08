[HOME](../../../../README.md)
# schedules:after

Schedule something after the given schedule.


This can be useful when wanting a schedule to begin at the end of an
interval instead of the beginning, or for scheduling around holidays when
a process is still required to run on the holiday, but not the day after
the holiday.


The after schedule differs from the [schedules:day-after](../../../../org/oddjob/schedules/schedules/DayAfterSchedule.md) in that
day-after is designed to narrow it's parent interval but this schedule
applies a refinement to it child schedule. The difference is subtle
but hopefully the examples demonstrate how each should be used.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [refinement](#propertyrefinement) | Provide a refinement to this schedule. | 
| [schedule](#propertyschedule) | The schedule to be after. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A schedule for the end of the interval. |
| [Example 2](#example2) | A schedule for the day after a the current business day. |


### Property Detail
#### refinement <a name="propertyrefinement"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Provide a refinement to this schedule.

#### schedule <a name="propertyschedule"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The schedule to be after.


### Examples
#### Example 1 <a name="example1"></a>

A schedule for the end of the interval.

```xml
<schedules:count count="1"
    xmlns:schedules="http://rgordon.co.uk/oddjob/schedules">
    <refinement>
        <schedules:after>
            <schedule>
                <schedules:interval interval="00:20"/>
            </schedule>
        </schedules:after>
    </refinement>
</schedules:count>
```


This would schedule a job to run once after 20 minutes. It could be
used to stop a long running job for instance.

#### Example 2 <a name="example2"></a>

A schedule for the day after a the current business day.

```xml
<schedules:after xmlns:schedules="http://rgordon.co.uk/oddjob/schedules">
    <schedule>
        <schedules:broken>
            <schedule>
                <schedules:weekly from="MONDAY" to="FRIDAY">
                    <refinement>
                        <schedules:daily/>
                    </refinement>
                </schedules:weekly>
            </schedule>
            <breaks>
                <schedules:date on="2011-05-02"/>
            </breaks>
        </schedules:broken>
    </schedule>
    <refinement>
        <schedules:time from="08:00"/>
    </refinement>
</schedules:after>

```


Normally this will schedule something from 08:00 am Tuesday to Saturday,
but for the week where Monday 2nd of May was a public holiday the schedule
will be from Wednesday to Saturday.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
