[HOME](../../../../README.md)
# folder

Holds a collection of jobs but does not
execute them. Used to collect and organise jobs. The jobs can either
be scheduled by a scheduler or run manually.


A folder has no state, it can't be run and it can't be stopped.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [jobs](#propertyjobs) | The jobs. | 
| [name](#propertyname) | The name of the folder. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A folder of jobs. |


### Property Detail
#### jobs <a name="propertyjobs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The jobs.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of the folder.


### Examples
#### Example 1 <a name="example1"></a>

A folder of jobs.

```xml
<folder name="My Jobs">
    <jobs>
        <exec name="Morning Job">echo "Good Morning"</exec>
        <exec name="Afternoon Job">echo "Good Afternoon"</exec>
        <exec name="Evening Job">echo "Good Evening"</exec>
    </jobs>
</folder>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
