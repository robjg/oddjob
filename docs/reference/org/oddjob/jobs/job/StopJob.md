[HOME](../../../../README.md)
# stop

A job which stops another job.


Normally The stop job will not complete until the job it is
stopping is in a stopped state, however if the
stop job is attempting tos stop a parent of itself (and therefore itself) then
this stop job will detect this and stop. It will therefore complete
even if thing it is trying to stop hasn't fully stopped.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [executorService](#propertyexecutorService) |  | 
| [job](#propertyjob) | Job to stop | 
| [name](#propertyname) | A name, can be any text. | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Examples elsewhere. |


### Property Detail
#### executorService <a name="propertyexecutorService"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
</table>



#### job <a name="propertyjob"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

Job to stop

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
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

This flag is set by the stop method and should
be examined by any Stoppable jobs in their processing loops.


### Examples
#### Example 1 <a name="example1"></a>

Examples elsewhere.

- [jmx:client](../../../../org/oddjob/jmx/JMXClientJob.md) has an example where the stop job is used to stop a client once the connection is no longer needed.



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
