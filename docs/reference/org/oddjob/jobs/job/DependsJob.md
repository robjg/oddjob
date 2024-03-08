[HOME](../../../../README.md)
# depends

This job is deprecated, use [run](../../../../org/oddjob/jobs/job/RunJob.md) instead.


A job which depends on another job.

- If the other job is in a READY state, this job will run the other job.
- If the other job is in an EXECUTING state, this job will wait.
- If the other job has finished this job will reflect the completion state.


This job was intended to simulate Ant's dependency like
functionality but the run job is better.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [job](#propertyjob) | Job to depend on. | 
| [name](#propertyname) | A name, can be any text. | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 


### Property Detail
#### job <a name="propertyjob"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

Job to depend on.

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


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
