[HOME](../../../README.md)
# schedule

Applies a schedule to a given date to provide a calculated date.
If the date is not provide the current date is used. This type will most often be
used to calculate the current date or the current time, or a next business date.


The [org.oddjob.schedules.ScheduleResult](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/schedules/ScheduleResult.html) is also available and this can be used to calculate
recurring schedules as in the example below.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [date](#propertydate) | The Date to use. | 
| [result](#propertyresult) | The result of applying the schedule which is a ScheduleResult bean that has the properties fromDate, toDate and useNext. | 
| [schedule](#propertyschedule) | The schedule to use. | 
| [timeZone](#propertytimezone) | The time zone to apply the schedule for. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Display the time now. |
| [Example 2](#example2) | Use a schedule with a time zone. |
| [Example 3](#example3) | Calculate the next business date. |
| [Example 4](#example4) | Display the due dates for a recurring schedule. |


### Property Detail
#### date <a name="propertydate"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>

The Date to use.

#### result <a name="propertyresult"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

The result of applying the schedule which is a
ScheduleResult bean that has the properties fromDate, toDate and
useNext.

#### schedule <a name="propertyschedule"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>

The schedule to use.

#### timeZone <a name="propertytimezone"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>

The time zone to apply the schedule for.


### Examples
#### Example 1 <a name="example1"></a>

Display the time now. Note the date variable is passed into Oddjob during
the testing of this example so the time can be fixed, but run as is it
will be null and so the current time will be displayed.

```xml
<oddjob id='this'>
    <job>
        <sequential>
            <jobs>
                <variables id='time'>
                    <now>
                        <schedule>
                            <date>
                                <value value='${date}'/>
                            </date>
                            <schedule>
                                <schedules:now xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"/>
                            </schedule>
                        </schedule>
                    </now>
                    <formatted>
                        <format date='${time.now}' format="hh:mm a"/>
                    </formatted>
                </variables>
                <echo id='echo-time'>The time now is ${time.formatted}</echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Use a schedule with a time zone. This example displays when tomorrow starts
in Hong Kong in the local time. 

```xml
<oddjob id='this'>
    <job>
        <sequential>
            <jobs>
                <variables id='time'>
                    <now>
                        <schedule timeZone="Asia/Hong_Kong">
                            <date>
                                <value value='${date}'/>
                            </date>
                            <schedule>
                                <schedules:day-after xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"/>
                            </schedule>
                        </schedule>
                    </now>
                    <formatted>
                        <format date='${time.now}' format="hh:mm a"/>
                    </formatted>
                </variables>
                <echo id='echo-time'>Tomorrow in Hong Kong starts at ${time.formatted} our time.</echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


Note that to display the current time in Hong Kong
we would use a Time Zone on the format type, not on the now schedule because
dates internally use UTC (Coordinated Universal Time) so now will always be
the same regardless of time zone.

#### Example 3 <a name="example3"></a>

Calculate the next business date. Two schedule types are used, the first calculates
the next day, the next takes this and applies it to a schedule that defines the
business days. The result will be the next business day.

```xml
<oddjob xmlns:schedules="http://rgordon.co.uk/oddjob/schedules">
    <job>
        <sequential>
            <jobs>
                <variables id='time'>
                    <tomorrow>
                        <schedule>
                            <date>
                                <value value='${date}'/>
                            </date>
                            <schedule>
                                <schedules:day-after/>
                            </schedule>
                        </schedule>
                    </tomorrow>
                    <nextBusinessDay>                    
                        <schedule>
                            <date>
                                <value value='${time.tomorrow}'/>
                            </date>
                            <schedule>
                                <schedules:broken>
                                    <schedule>
                                        <schedules:weekly from="Monday" to="Friday">
                                            <refinement>
                                                <schedules:daily/>
                                            </refinement>
                                        </schedules:weekly>
                                    </schedule>
                                    <breaks>
                                        <schedules:list>
                                            <schedules>
                                                <schedules:date on="2011-12-26"/>
                                                <schedules:date on="2011-12-27"/>
                                            </schedules>
                                        </schedules:list>
                                    </breaks>
                                </schedules:broken>
                            </schedule>
                        </schedule>
                    </nextBusinessDay>
                </variables>
                <echo id='echo-time'>The next business date is ${time.nextBusinessDay}</echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 4 <a name="example4"></a>

Display the due dates for a recurring schedule. This would be useful for
experimenting with schedules.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <date>
                        <value value="${date}"/>
                    </date>
                </variables>
                <foreach>
                    <values>
                        <tokenizer text="1,2,3,4,5"/>
                    </values>
                    <configuration>
                        <xml>
                            <foreach>
                                <job>
                                    <sequential>
                                        <jobs>
                                            <variables id="time">
                                                <schedule>
                                                    <schedule>
                                                        <date>
                                                            <value value="${vars.date}"/>
                                                        </date>
                                                        <schedule>
                                                            <schedules:weekly from="Monday" to="Friday" xmlns:schedules="http://rgordon.co.uk/oddjob/schedules">
                                                                <refinement>
                                                                    <schedules:daily from="10:30"/>
                                                                </refinement>
                                                            </schedules:weekly>
                                                        </schedule>
                                                    </schedule>
                                                </schedule>
                                            </variables>
                                            <echo>Next due: ${time.schedule.result}</echo>
                                            <set>
                                                <values>
                                                    <value key="vars.date" value="${time.schedule.result.useNext}"/>
                                                </values>
                                            </set>
                                        </jobs>
                                    </sequential>
                                </job>
                            </foreach>
                        </xml>
                    </configuration>
                </foreach>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
