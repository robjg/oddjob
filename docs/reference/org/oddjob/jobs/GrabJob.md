[HOME](../../../README.md)
# grab

Grab work to do. By competing for work with
other Grabbers this job facilitates distribution of work between
multiple Oddjob processes.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [identifier](#propertyidentifier) | This job's identifier which is unique to the Oddjob process, such as server name. | 
| [instance](#propertyinstance) | The instance of identifier for a single grab. | 
| [job](#propertyjob) | The child job. | 
| [keeper](#propertykeeper) | The keeper of work from which this job attempts to grab work. | 
| [name](#propertyname) | A name, can be any text. | 
| [onLoosing](#propertyonLoosing) | The action on loosing. | 
| [winner](#propertywinner) | The identifier of the winner. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | See the user guide. |


### Property Detail
#### identifier <a name="propertyidentifier"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

This job's identifier which is unique to
the Oddjob process, such as server name.

#### instance <a name="propertyinstance"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The instance of identifier for a single grab.
This is an identifier for each run of the grab job and will be
something like the scheduled date/time.

#### job <a name="propertyjob"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No, but pointless if missing.</td></tr>
</table>

The child job.

#### keeper <a name="propertykeeper"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The keeper of work from which this job
attempts to grab work.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### onLoosing <a name="propertyonLoosing"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, Defaults to COMPLETE.</td></tr>
</table>

The action on loosing. Available actions are:
<dl>
<dt>COMPLETE</dt>
<dd>Set the job state to COMPLETE.</dd>
<dt>INCOMPLETE</dt>
<dd>Set the job state to INCOMPLETE.</dd>
<dt>WAIT</dt>
<dd>Wait until the job completes.</dd>

#### winner <a name="propertywinner"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>R/O.</td></tr>
</table>

The identifier of the winner. Will be equal
to this job's identifier if this job has won.


### Examples
#### Example 1 <a name="example1"></a>

See the user guide.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
