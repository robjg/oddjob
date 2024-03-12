[HOME](../../../../README.md)
# schedules:broken

This schedule allows a normal schedule
to be broken by the results of another
schedule. This might be a list of bank holidays, or time of day, or any other
schedule.


This schedule works by moving the schedule forward if the start time of the
next interval falls within the next interval defined by the break. In the
example below for a time of Midday on 24th of December the logic is as follows:

- The schedule is next due at 10:00 on the 25th of December.
- This is within the break, move the schedule on.
- The schedule is next due at 10:00 on the 26th of December.
- This is within the break, move the schedule on.
- The schedule is next due at 10:00 on the 27th of December.
- This schedule is outside the break, use this result.



The optional alternative property defines a schedule to be used during the
breaks, instead of simply moving the interval forward.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [alternative](#propertyalternative) | An alternative schedule to apply during a break. | 
| [breaks](#propertybreaks) | The breaks. | 
| [schedule](#propertyschedule) | The schedule. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A schedule that breaks for Christmas. |
| [Example 2](#example2) | A schedule with an alternative. |
| [Example 3](#example3) | Examples elsewhere. |


### Property Detail
#### alternative <a name="propertyalternative"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An alternative schedule to apply during a break.
The alternative schedule will be passed the interval that is the break.

#### breaks <a name="propertybreaks"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, but this schedule is pointless if none are provided.</td></tr>
</table>

The breaks.

#### schedule <a name="propertyschedule"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The schedule.


### Examples
#### Example 1 <a name="example1"></a>

A schedule that breaks for Christmas.

```xml
<schedules:broken xmlns:schedules="http://rgordon.co.uk/oddjob/schedules">
    <schedule>
        <schedules:daily at="10:00"/>
    </schedule>
    <breaks>
        <schedules:yearly fromDate="12-25" toDate="12-26"/>
    </breaks>
</schedules:broken>
```


The logic is explained above.

#### Example 2 <a name="example2"></a>

A schedule with an alternative. The schedule breaks at weekends and for
Christmas. During the break the schedule will be due once at 11am the
first day of the break, instead of the usual 10am.

```xml
<schedules:broken xmlns:schedules="http://rgordon.co.uk/oddjob/schedules">
    <schedule>
         <schedules:daily from="10:00"/>
    </schedule>
    <breaks>
        <schedules:list>
            <schedules>
                <schedules:weekly from="Saturday" to="Sunday"/>
                <schedules:date on="2011-12-26"/>
                <schedules:date on="2011-12-27"/>
            </schedules>
        </schedules:list>        
    </breaks>
    <alternative>
        <schedules:time from="11:00"/>
    </alternative>
</schedules:broken>
```


#### Example 3 <a name="example3"></a>

Examples elsewhere.

- The [schedules:after](../../../../org/oddjob/schedules/schedules/AfterSchedule.md) documentation has an example that uses the <code>broken</code> schedule to calculate the day after the next working day.
- The [schedule](../../../../org/oddjob/schedules/ScheduleType.md) documentation shows a <code>broken</code> schedule being used to calculate the next working day.
- The [schedules:day-after](../../../../org/oddjob/schedules/schedules/DayAfterSchedule.md) and [schedules:day-before](../../../../org/oddjob/schedules/schedules/DayBeforeSchedule.md) documentation shows a <code>broken</code> schedule being used to move the last day of the month.



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
