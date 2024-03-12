[HOME](../../../README.md)
# state:resets

Captures Reset actions propagating down a job tree and either hardens
soft resets to hard resets or softens hard resets to soft resets before
passing them on to the child job.


Execute and Stop actions are cascaded as normal to the child job.


See also the [reset](../../../org/oddjob/jobs/job/ResetJob.md) job.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [harden](#propertyharden) | Harden soft resets. | 
| [job](#propertyjob) | The job to pass resets on to. | 
| [name](#propertyname) | A name, can be any text. | 
| [soften](#propertysoften) | Soften hard resets. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Examples elsewhere. |


### Property Detail
#### harden <a name="propertyharden"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

Harden soft resets. True/False.

#### job <a name="propertyjob"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The job to pass resets on to.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### soften <a name="propertysoften"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

Soften hard resets. True/False

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Read only view of the internal stop flag.
This flag is cleared with a reset.


### Examples
#### Example 1 <a name="example1"></a>

Examples elsewhere.

- See the [state:equals](../../../org/oddjob/state/EqualsState.md) example. The resets are required because retry only sends a soft reset on retry and it must be hardened to reset the [exists](../../../org/oddjob/io/ExistsJob.md). 



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
