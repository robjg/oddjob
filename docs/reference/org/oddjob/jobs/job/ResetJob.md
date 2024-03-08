[HOME](../../../../README.md)
# reset

A job which resets another job. This job is
useful to reset jobs or services that have been persisted, and
loaded back in their previous COMPLETE states. The reset
can be used to set them back to READY.


A reset might also be needed before running a job elsewhere
such as on a remote server.


As of version 1.4 of Oddjob, this job can now also be used to force
jobs that are [org.oddjob.Forceable](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/Forceable.html) by specify 'force' as level.


This job is not Serializable and so won't be persisted
itself.


See also the [state:resets](../../../../org/oddjob/state/Resets.md) job.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [job](#propertyjob) | Job to reset. | 
| [level](#propertylevel) | The reset level, hard or soft | 
| [name](#propertyname) | A name, can be any text. | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Using reset in explorer.xml. |
| [Example 2](#example2) | Force a job to complete. |


### Property Detail
#### job <a name="propertyjob"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

Job to reset.

#### level <a name="propertylevel"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to soft.</td></tr>
</table>

The reset level, hard or soft

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

Using reset in explorer.xml.


Look at the explorer.xml file in Oddjob's home directory. This file is
loaded by the default oddjob.xml file when Oddjob first runs.
The explorer.xml configuration is run with a [file-persister](../../../../org/oddjob/persist/FilePersister.md)
persister that persists the Explorers state when it
completes. When Oddjob is run again the Explorer will be
loaded with it's previous COMPLETE state and so won't run. The reset
is necessary to set it back to READY.

#### Example 2 <a name="example2"></a>

Force a job to complete.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <reset job="${echo}" level="force"/>
                <echo id="echo"><![CDATA[You will never see this!]]></echo>
            </jobs>
        </sequential>
    </job>
</oddjob>

```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
