[HOME](../../../../README.md)
# schedules:interval

This schedule returns an interval
from the given time to the interval time later.


This schedule is commonly used as a refinement of another schedule
such as the [schedules:daily](../../../../org/oddjob/schedules/schedules/DailySchedule.md), [schedules:time](../../../../org/oddjob/schedules/schedules/TimeSchedule.md) or [schedules:count](../../../../org/oddjob/schedules/schedules/CountSchedule.md)
schedules.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [interval](#propertyinterval) | The interval time. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Every 20 minutes. |
| [Example 2](#example2) | Examples Elsewhere. |


### Property Detail
#### interval <a name="propertyinterval"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No but defaults to no interval.</td></tr>
</table>

The interval time. The interval must be specified
in one of the formats:
<dl>
<dt>hh:mm</dt><dd>Hours and minutes.</dd>
<dt>hh:mm:ss</dt><dd>Hours, minutes and seconds.</dd>
<dt>hh.mm.ss.SSS</dt><dd>Hours, minutes, seconds and milliseconds.</dd>
</dl>


### Examples
#### Example 1 <a name="example1"></a>

Every 20 minutes.

```xml
<schedules:interval xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
     interval="00:20" />


```


#### Example 2 <a name="example2"></a>

Examples Elsewhere.

- [schedules:time](../../../../org/oddjob/schedules/schedules/TimeSchedule.md)
- [schedules:daily](../../../../org/oddjob/schedules/schedules/DailySchedule.md)
- [schedules:count](../../../../org/oddjob/schedules/schedules/CountSchedule.md)



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
