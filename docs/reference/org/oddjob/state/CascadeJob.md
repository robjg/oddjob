[HOME](../../../README.md)
# cascade

A job which triggers the next job after the previous one completes.
This job differs from a [sequential](../../../org/oddjob/jobs/structural/SequentialJob.md) task in that the latter
follows the thread of execution, and only checks state to ensure
it can continue. This job's thread of execution passes onwards after the
cascade has been set up. This job will complete asynchronously once all
it's children have completed.

<h4>State Operator</h4>
This job doesn't currently expose a State Operator property as
[sequential](../../../org/oddjob/jobs/structural/SequentialJob.md) does. The state behaviour is equivalent to the
WORST state operator, which is what is desired in most situations. A
<code>stateOperator</code> property may be added in future versions
if needed.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [cascadeOn](#propertycascadeOn) | The state to continue the cascade on. | 
| [haltOn](#propertyhaltOn) | The state to halt the cascade on. | 
| [jobs](#propertyjobs) | The child jobs. | 
| [name](#propertyname) | A name, can be any text. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A cascade of two jobs. |
| [Example 2](#example2) | Showing cascade being used with [parallel](../../../org/oddjob/jobs/structural/ParallelJob.md). |


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


### Examples
#### Example 1 <a name="example1"></a>

A cascade of two jobs.

```xml
<oddjob>
    <job>
        <cascade>
            <jobs>
                <echo>This runs first.</echo>
                <echo>Then this.</echo>
            </jobs>
        </cascade>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Showing cascade being used with [parallel](../../../org/oddjob/jobs/structural/ParallelJob.md). The cascade will
wait for the parallel job to finish before running the third job.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <cascade>
            <jobs>
                <parallel>
                    <jobs>
                        <echo>Oranges could be first or second.</echo>
                        <echo>Pears could be first or second.</echo>
                    </jobs>
                </parallel>
                <echo>Apples are guaranteed to be third.</echo>
            </jobs>
        </cascade>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
