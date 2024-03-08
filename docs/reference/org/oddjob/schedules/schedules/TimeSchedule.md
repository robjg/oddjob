[HOME](../../../../README.md)
# schedules:time

Provide a schedule for an interval of time. When used as a
refinement this schedule will narrow the parent interval down to an interval of
time on the first day of the parent interval, or if the <code>toLast</code>
property is specified, from the first day to the last day of the parent interval. When used as the
topmost definition for a schedule then this schedule specifies a single interval
of time starting on the current day.


To provide a schedule for each day at a certain time see the [schedules:daily](../../../../org/oddjob/schedules/schedules/DailySchedule.md)
schedules.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [at](#propertyat) | The time at which this schedule is for. | 
| [from](#propertyfrom) | The from time. | 
| [refinement](#propertyrefinement) | Provide a refinement to this schedule. | 
| [to](#propertyto) | The to time. | 
| [toLast](#propertytoLast) | The to time for the end of the parent interval. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A simple time example. |
| [Example 2](#example2) | Using an interval with time to schedule something every 15 minutes between 10pm and 4am the next day. |
| [Example 3](#example3) | Schedule something over a whole week between two times. |


### Property Detail
#### at <a name="propertyat"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The time at which this schedule is for.
This has the same effect as setting from and to to the same thing.

#### from <a name="propertyfrom"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to the start of any parent interval
 or the beginning of time.</td></tr>
</table>

The from time.

#### refinement <a name="propertyrefinement"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Provide a refinement to this schedule.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to the end of the last day of the
 parent interval, or the end of time.</td></tr>
</table>

The to time. If specified, this is the
time on the first day of the parent interval.

#### toLast <a name="propertytoLast"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. The to property, or it's default value,
 will be used instead.</td></tr>
</table>

The to time for the end of the parent interval.
This differs from the to property in that the to property is for the first
day of the parent interval.


### Examples
#### Example 1 <a name="example1"></a>

A simple time example.

```xml
<schedules:time xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
           at="10:00"/>

```


When used with a [scheduling:timer](../../../../org/oddjob/scheduling/Timer.md) this would run a job just once at 10am, and
never again. If the
timer was started after 10am, then the job would run the following day at 10am.
If it was required that the job would run any time the timer was started
after 10am then the <code>
from</code> property should be used instead of the <code>at</code> property.

#### Example 2 <a name="example2"></a>

Using an interval with time to schedule something every 15 minutes between
10pm and 4am the next day. The end time is 03:50 yet the last interval is
03:45 to 04:00 because the interval starts before the end time.

```xml
<schedules:time xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
    from="22:00" to="03:50">
    <refinement>
        <schedules:interval interval="00:15"/>
    </refinement>
</schedules:time>

```


#### Example 3 <a name="example3"></a>

Schedule something over a whole week between two times. This demonstrates
how the <code>toLast</code> property works.

```xml
<schedules:weekly xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
    from="Monday" to="Friday">
    <refinement>
        <schedules:time from="08:00" toLast="18:00">
            <refinement>
                <schedules:interval interval="02:00"/>
            </refinement>
        </schedules:time>
    </refinement>
</schedules:weekly>
```


The schedule would be due every two hours all day and all night from 8am
Monday morning until 6pm Friday afternoon.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
