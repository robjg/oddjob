[HOME](../../../../README.md)
# switch

Switch Based on a predicate.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [jobs](#propertyjobs) | The child jobs. | 
| [name](#propertyname) | A name, can be any text. | 
| [predicates](#propertypredicates) |  | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 
| [switches](#propertyswitches) |  | 
| [value](#propertyvalue) | The value to check. | 


### Property Detail
#### jobs <a name="propertyjobs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>At least one.</td></tr>
</table>

The child jobs.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### predicates <a name="propertypredicates"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>



#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Read only view of the internal stop flag.
This flag is cleared with a reset.

#### switches <a name="propertyswitches"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>



#### value <a name="propertyvalue"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, but the check value is not null will fail.</td></tr>
</table>

The value to check.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
