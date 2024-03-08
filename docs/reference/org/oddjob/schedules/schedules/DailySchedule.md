[HOME](../../../../README.md)
# schedules:daily

A schedule for each day at, or from a given time.
This schedule
enables job to be scheduled daily at a particular time or a from/to time which
could be used to constrain a sub schedule.


If the 'to' time is less than the 'from' time it is assumed that the 'to'
time is the next day.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [at](#propertyat) | The time at which this schedule is for. | 
| [from](#propertyfrom) | The from time. | 
| [refinement](#propertyrefinement) | Provide a refinement to this schedule. | 
| [to](#propertyto) | The to time. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A simple daily schedule. |
| [Example 2](#example2) | Using an interval with a daily schedule to schedules something every 15 minutes between 10pm and 4am. |


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
      <tr><td><i>Required</i></td><td>No. Default to the start of the day.</td></tr>
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
      <tr><td><i>Required</i></td><td>No. Default to the end of the day.</td></tr>
</table>

The to time.


### Examples
#### Example 1 <a name="example1"></a>

A simple daily schedule. Used with a [scheduling:timer](../../../../org/oddjob/scheduling/Timer.md) this would run a job
every day at 10am.

```xml
<schedules:daily xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
           at="10:00"/>

```


#### Example 2 <a name="example2"></a>

Using an interval with a daily schedule to schedules something every 15 minutes
between 10pm and 4am. The end time is 03:50 yet the last interval is
03:45 to 04:00 because the interval starts before the end time.

```xml
<schedules:daily xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
    from="22:00" to="03:50">
    <refinement>
        <schedules:interval interval="00:15"/>
    </refinement>
</schedules:daily>

```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
