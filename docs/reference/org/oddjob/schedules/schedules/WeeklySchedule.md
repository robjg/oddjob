[HOME](../../../../README.md)
# schedules:weekly

A schedule for weekly intervals
specified by days of the week. This schedule
will typically be used with a [schedules:time](../../../../org/oddjob/schedules/schedules/TimeSchedule.md) refinement property.


The days of the week are specified according to the ISO 8601 standard
with Monday being day 1 and Sunday being day 7, or as one of
MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY (case
insensitive).

### Property Summary

| Property | Description |
| -------- | ----------- |
| [from](#propertyfrom) | The from day of the week. | 
| [on](#propertyon) | The on day of week. | 
| [refinement](#propertyrefinement) | Provide a refinement to this schedule. | 
| [to](#propertyto) | The to day of the week. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A schedule for all day Tuesday. |
| [Example 2](#example2) | A schedule between Friday and the following Monday inclusive. |


### Property Detail
#### from <a name="propertyfrom"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to Monday.</td></tr>
</table>

The from day of the week.

#### on <a name="propertyon"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The on day of week. This has the same effect as
setting from and to to the same thing.

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
      <tr><td><i>Required</i></td><td>No. Defaults to Sunday.</td></tr>
</table>

The to day of the week.


### Examples
#### Example 1 <a name="example1"></a>

A schedule for all day Tuesday. This schedule defines an
interval between midnight Tuesday morning and up to midnight Tuesday night.

```xml
<schedules:weekly xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
    on="Tuesday"/>

```


#### Example 2 <a name="example2"></a>

A schedule between Friday and the following Monday inclusive.
This schedule is refined by a time that will define the schedule to be each
of the days Friday, Saturday, Sunday, Monday at 3:45pm.

```xml
<schedules:weekly xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
    from="Friday" to="Monday">
    <refinement>
        <schedules:daily at="15:45"/>
    </refinement>
</schedules:weekly>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
