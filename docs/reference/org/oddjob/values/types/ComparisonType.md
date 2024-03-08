[HOME](../../../../README.md)
# comparison

Provides a Predicate from simple checks.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [eq](#propertyeq) | The value must be equal to this. | 
| [ge](#propertyge) | The value must be greater than or equal to this. | 
| [gt](#propertygt) | The value must be greater than this. | 
| [le](#propertyle) | The value must be less than or equals to this. | 
| [lt](#propertylt) | The value must be less than this. | 
| [name](#propertyname) | The name of this job. | 
| [ne](#propertyne) | The value must be not equal to this. | 
| [null](#propertynull) | Must the value be null for the check to pass. | 
| [z](#propertyz) | The value to check. | 


### Property Detail
#### eq <a name="propertyeq"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The value must be equal to this.

#### ge <a name="propertyge"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The value must be greater than or equal to this.

#### gt <a name="propertygt"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The value must be greater than this.

#### le <a name="propertyle"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The value must be less than or equals to this.

#### lt <a name="propertylt"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The value must be less than this.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of this job. Can be any text.

#### ne <a name="propertyne"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The value must be not equal to this.

#### null <a name="propertynull"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, if this does exist the check value null will fail.</td></tr>
</table>

Must the value be null for the check to pass.
True the value must be null. False it must not be null. If this
property is true other checks will cause an exception because they
require the value property has a value.

#### z <a name="propertyz"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, but the check value is not null will fail.</td></tr>
</table>

The value to check.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
