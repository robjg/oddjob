[HOME](../../../README.md)
# wait

This Job will either wait a given number of milliseconds
or will wait for a property or job to become available.


If the for property is provided, then the delay is used as the number of
milliseconds between checking if the property is available.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [for](#propertyfor) | The property to wait for. | 
| [name](#propertyname) | A name, can be any text. | 
| [pause](#propertypause) | The wait delay in milliseconds. | 
| [state](#propertystate) | A state condition to wait for. | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | The [throttle](../../../org/oddjob/scheduling/ExecutorThrottleType.md)has a simple example. |
| [Example 2](#example2) | This example waits for a variable 'text' to be set. |


### Property Detail
#### for <a name="propertyfor"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The property to wait for.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### pause <a name="propertypause"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No if for property is set, otherwise yes.</td></tr>
</table>

The wait delay in milliseconds.

#### state <a name="propertystate"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A state condition to wait for. When this is
set this job will wait for the job referenced with the <code>
for</code> property match the given state condition.
See the Oddjob User guide for a full list of state conditions.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

This flag is set by the stop method and should
be examined by any Stoppable jobs in their processing loops.


### Examples
#### Example 1 <a name="example1"></a>

The [throttle](../../../org/oddjob/scheduling/ExecutorThrottleType.md) has a simple example.

#### Example 2 <a name="example2"></a>

This example waits for a variable 'text' to be set. The value could be set
across the network or by a another job running in parallel.

```xml
<sequential name="Waiting For a Property">
    <jobs>
        <variables id="waitvars"/>
        <wait name="Wait for Variable"
              for="${waitvars.text}" 
              pause="2000"/>
        <echo name="Echo Text">${waitvars.text}</echo>
    </jobs>
</sequential>
 
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
