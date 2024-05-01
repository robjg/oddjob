[HOME](../../../../README.md)
# class

Returns a Class for the given name.


This class is not `java.io.Serializable` because it requires a ClassLoader
so it can not be used to set a property on a server from an Odjdob Client.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [classLoader](#propertyclassLoader) | The class loader to use to load the class. | 
| [name](#propertyname) | The name of the class. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | See [convert](../../../../org/oddjob/arooa/types/ConvertType.md) for an example. |


### Property Detail
#### classLoader <a name="propertyclassLoader"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to Oddjob's class loader.</td></tr>
</table>

The class loader to use to load the class.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The name of the class.


### Examples
#### Example 1 <a name="example1"></a>

See [convert](../../../../org/oddjob/arooa/types/ConvertType.md) for an example.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
