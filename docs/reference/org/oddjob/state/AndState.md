[HOME](../../../README.md)
# state:and

A job who's return state is a logical AND of the child states.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [executorService](#propertyexecutorService) | The ExecutorService to use. | 
| [jobs](#propertyjobs) | The child jobs. | 
| [join](#propertyjoin) |  | 
| [name](#propertyname) | A name, can be any text. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | COMPLETE if both files exist, INCOMPLETE otherwise. |


### Property Detail
#### executorService <a name="propertyexecutorService"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The ExecutorService to use. This will
be automatically set by Oddjob.

#### jobs <a name="propertyjobs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No, but pointless if missing.</td></tr>
</table>

The child jobs.

#### join <a name="propertyjoin"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



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


### Examples
#### Example 1 <a name="example1"></a>

COMPLETE if both files exist, INCOMPLETE otherwise.

```xml
<state:and xmlns:state="http://rgordon.co.uk/oddjob/state">
    <jobs>
        <exists file="${file.one}"/>
        <exists file="${file.two}"/>
    </jobs>
</state:and>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
