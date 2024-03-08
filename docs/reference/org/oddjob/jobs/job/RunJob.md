[HOME](../../../../README.md)
# run

A job which runs another job. The other job can be
local or on a server.


This job reflects the state of the job being executed.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [job](#propertyjob) | Job to run | 
| [join](#propertyjoin) | Wait for the target job to finish executing. | 
| [name](#propertyname) | A name, can be any text. | 
| [reset](#propertyreset) | The reset level. | 
| [showJob](#propertyshowJob) | Add the target job as a child of this job. | 
| [stateOperator](#propertystateOperator) | Set the way the children's state is evaluated and reflected by the parent. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Examples elsewhere. |


### Property Detail
#### job <a name="propertyjob"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

Job to run

#### join <a name="propertyjoin"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

Wait for the target job to finish executing.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### reset <a name="propertyreset"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to NONE.</td></tr>
</table>

The reset level. See [org.oddjob.jobs.job.ResetActions](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/jobs/job/ResetActions.html).

#### showJob <a name="propertyshowJob"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

Add the target job as a child of this job. Allows
the target job to be easily monitored from a UI.

#### stateOperator <a name="propertystateOperator"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, default is ACTIVE.</td></tr>
</table>

Set the way the children's state is
evaluated and reflected by the parent. Values can be WORST,
ACTIVE, or SERVICES.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Read only view of the internal stop flag.
This flag is cleared with a reset.


### Examples
#### Example 1 <a name="example1"></a>

Examples elsewhere.

- The [jmx:client](../../../../org/oddjob/jmx/JMXClientJob.md)job has an example that uses <code>run</code>to run a job on a remote server.



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
