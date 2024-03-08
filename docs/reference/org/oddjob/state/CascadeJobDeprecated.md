[HOME](../../../README.md)
# state:cascade

The namespace version this job is deprecated. Please use
[cascade](../../../org/oddjob/state/CascadeJob.md) instead.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [cascadeOn](#propertycascadeOn) | The state to continue the cascade on. | 
| [haltOn](#propertyhaltOn) | The state to halt the cascade on. | 
| [jobs](#propertyjobs) | The child jobs. | 
| [name](#propertyname) | A name, can be any text. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 


### Property Detail
#### cascadeOn <a name="propertycascadeOn"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to COMPLETE.</td></tr>
</table>

The state to continue the cascade on.

#### haltOn <a name="propertyhaltOn"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to FAILURE.</td></tr>
</table>

The state to halt the cascade on.

#### jobs <a name="propertyjobs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No, but pointless if missing.</td></tr>
</table>

The child jobs.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Read only view of the internal stop flag.
This flag is cleared with a reset.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
