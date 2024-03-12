[HOME](../../../../README.md)
# schedules:monthly

A schedule for monthly intervals. The intervals
can be specified as days of the month, a day of the week in a week of the month,
or less usefully as weeks of the month.


The day of the month is given
as an number, normally 1 to 31. 0 and negative numbers can be used to specify
days from the end of the month. The words LAST and PENULTIMATE
(case insensitive) can also be
used as a convenience. Note that unlike the java
<code>GregorianCalander</code>, 0 and negative numbers are taken to be
this month, not the previous month. i.e. on="0" is the last day of the month and
is equivalent to on="LAST".


Days and week of the month are given as the day number, 1 to 7, or as one
of MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
(case insensitive). The week of
the month is specified as a number, typically 1 to 5, or using one of FIRST,
SECOND, THIRD, FOURTH, FIFTH, PENULTIMATE, or LAST (case insensitive).


If the week of the month is specified on it's own then the first week is
taken to be the first complete week of the month.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [fromDay](#propertyfromDay) | The from day of the month. | 
| [fromDayOfWeek](#propertyfromDayOfWeek) | The from day of the week. | 
| [fromWeek](#propertyfromWeek) | The from week of the month. | 
| [inWeek](#propertyinWeek) | The in week of the month. | 
| [onDay](#propertyonDay) | The day on which this schedule is for. | 
| [onDayOfWeek](#propertyonDayOfWeek) | The on day of the week. | 
| [refinement](#propertyrefinement) | Provide a refinement to this schedule. | 
| [toDay](#propertytoDay) | The to day of the month. | 
| [toDayOfWeek](#propertytoDayOfWeek) | The to day of the week. | 
| [toWeek](#propertytoWeek) | The to week of the month. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A range of days of the month. |
| [Example 2](#example2) | On a single day of the month. |
| [Example 3](#example3) | On the last Friday of the month. |


### Property Detail
#### fromDay <a name="propertyfromDay"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to 1.</td></tr>
</table>

The from day of the month.

#### fromDayOfWeek <a name="propertyfromDayOfWeek"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The from day of the week. Used in conjunction with
<code>fromWeekOfMonth</code>.

#### fromWeek <a name="propertyfromWeek"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The from week of the month.

#### inWeek <a name="propertyinWeek"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The in week of the month. This is equivalent to
setting <code>fromWeek</code> and <code>toWeek</code> to the same thing.

#### onDay <a name="propertyonDay"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The day on which this schedule is for.
This has the same effect as setting <code>fromDay</code>
and <code>toDay</code> to the same thing.

#### onDayOfWeek <a name="propertyonDayOfWeek"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The on day of the week. This is equivalent to
setting <code>fromDayOfWeek</code> and  <code>toDayOfWeek</code>
to the same thing.

#### refinement <a name="propertyrefinement"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Provide a refinement to this schedule.

#### toDay <a name="propertytoDay"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to the last day of the month.</td></tr>
</table>

The to day of the month.

#### toDayOfWeek <a name="propertytoDayOfWeek"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The to day of the week. Used in conjunction with
<code>toDayOfWeek</code>.

#### toWeek <a name="propertytoWeek"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The to week of the month.


### Examples
#### Example 1 <a name="example1"></a>

A range of days of the month.

```xml
<schedules:monthly xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
                      fromDay="17" toDay="22">
     <refinement>
        <schedules:daily at="10:00"/>
     </refinement>
</schedules:monthly>
```


This would schedule a job to run every day from the 17th of each month to
the 25th of each month at 10am.

#### Example 2 <a name="example2"></a>

On a single day of the month.

```xml
<schedules:monthly xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
                     onDay="15"/>
```


This will run a job on the 15th of every month.

#### Example 3 <a name="example3"></a>

On the last Friday of the month.

```xml
<schedules:monthly xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
                      onDayOfWeek="FRIDAY" inWeek="LAST">
     <refinement>
        <schedules:time at="07:00"/>
     </refinement>
</schedules:monthly>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
