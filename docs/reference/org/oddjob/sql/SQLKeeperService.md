[HOME](../../../README.md)
# sql-keeper-service

Provides a [org.oddjob.scheduling.Keeper](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/scheduling/Keeper.html) that uses a database
table.


The keeper uses a simple 'first to insert' a row wins methodology for deciding
winner and looser. This is quite primitive and decides that any exception
from the insert operation is a duplicate key exception and therefore a
looser.


A [org.oddjob.scheduling.LoosingOutcome](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/scheduling/LoosingOutcome.html) will continue to Poll the database (for as long
as it has listeners) until the work is complete. The default polling schedule
polls every 5 seconds indefinitely. The <code>pollSchedule</code> property
can be used to poll for a limited time, after which it flags an exception
state. This could be used by loosing servers to flag the winner is taking
too long and has possibly crashed.


This is an example of the SQL that would create a suitable table.

<pre><code>
CREATE TABLE oddjob_grabbable(
key VARCHAR(32),
instance VARCHAR(32),
winner VARCHAR(32),
complete boolean,
CONSTRAINT oddjob_pk PRIMARY KEY (key, instance))
</pre></code>



This service does not tidy up the database so rows will grow indefinitely.
A separate tidy job should be implemented.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [connection](#propertyconnection) | The [connection](../../../org/oddjob/sql/ConnectionType.md) to use. | 
| [keeper](#propertykeeper) |  | 
| [name](#propertyname) | The name. | 
| [pollSchedule](#propertypollSchedule) | The schedule to provide the polling interval. | 
| [pollerCount](#propertypollerCount) | The number of outstanding loosing outcome's polling of the database that are still in progress. | 
| [scheduleExecutorService](#propertyscheduleExecutorService) | The scheduling service for polling. | 
| [table](#propertytable) | The database table name. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | See the User Guide. |


### Property Detail
#### connection <a name="propertyconnection"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The [connection](../../../org/oddjob/sql/ConnectionType.md) to use.

#### keeper <a name="propertykeeper"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name.

#### pollSchedule <a name="propertypollSchedule"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No - defaults to a 5 second {@link IntervalSchedule}.</td></tr>
</table>

The schedule to provide the polling interval.

#### pollerCount <a name="propertypollerCount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

The number of outstanding loosing outcome's
polling of the database that are still in progress.

#### scheduleExecutorService <a name="propertyscheduleExecutorService"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No - provided by Oddjob.</td></tr>
</table>

The scheduling service for polling.

#### table <a name="propertytable"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The database table name.


### Examples
#### Example 1 <a name="example1"></a>

See the User Guide.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
