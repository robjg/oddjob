[HOME](../../../../README.md)
# schedules:date

Provide a schedule for a
specific date or define an interval between two dates.


The dates must be of the form yyyy-MM-dd
where the format is as specified by the Java Date Format.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [from](#propertyfrom) | The from date for the schedule. | 
| [on](#propertyon) | A specific date on which to schedule something. | 
| [refinement](#propertyrefinement) | Provide a refinement to this schedule. | 
| [to](#propertyto) | The to date for the schedule. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A schedule for Christmas. |


### Property Detail
#### from <a name="propertyfrom"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The from date for the schedule. Defaults to
along time ago.

#### on <a name="propertyon"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A specific date on which to schedule something.

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
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The to date for the schedule. This date is
inclusive, the defined interval is up to and including the last
millisecond of this date. This defaults to
a long time away.


### Examples
#### Example 1 <a name="example1"></a>

A schedule for Christmas.

```xml
<schedules:date xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
                on="2004-12-25"/>

```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
