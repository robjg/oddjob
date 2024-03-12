[HOME](../../../README.md)
# archive

A Job that is capable of taking a snapshot of the
state of it's child jobs. An [archive-browser](../../../org/oddjob/persist/ArchiveBrowserJob.md)
can be used to browse an archive created with this job.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [archiveIdentifier](#propertyarchiveIdentifier) | The identifier of the snapshot that will be taken when this job runs. | 
| [archiveName](#propertyarchiveName) | The name of the archive that all snapshots will be stored in. | 
| [archiver](#propertyarchiver) | The persister to use to store archives. | 
| [job](#propertyjob) | The child job. | 
| [name](#propertyname) | A name, can be any text. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Create an archive after each scheduled run. |


### Property Detail
#### archiveIdentifier <a name="propertyarchiveIdentifier"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The identifier of the snapshot that will
be taken when this job runs.

#### archiveName <a name="propertyarchiveName"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The name of the archive that all snapshots
will be stored in.

#### archiver <a name="propertyarchiver"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes, but will fall back on the current Oddjob persister.</td></tr>
</table>

The persister to use to store archives.

#### job <a name="propertyjob"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No, but pointless if missing.</td></tr>
</table>

The child job.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.


### Examples
#### Example 1 <a name="example1"></a>

Create an archive after each scheduled run. The time of the schedule
is used to identify the archive.


```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <scheduling:timer id="the-timer" xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling">
                    <clock>
                      <value value="${clock}"/>
                    </clock>
                    <schedule>
                        <schedules:count count="3" xmlns:schedules="http://rgordon.co.uk/oddjob/schedules">
                        	<refinement>
                        		<schedules:interval interval="00:00:00.100"/>
                        	</refinement>
                        </schedules:count>
                    </schedule>
                    <job>
                    	<archive archiveName="Batch_01">
                    		<archiver>
                    			<file-persister dir="${this.args[0]}"/>
                    		</archiver>
                    	    <archiveIdentifier>
                    	    	<format date="${the-timer.current.fromDate}"
                    	    		format="mm_ss_SSS"/>
                    	    </archiveIdentifier>
                    		<job>
		                        <echo>Run at ${the-timer.current.fromDate}</echo>
                        	</job>
                        </archive>
                    </job>
                </scheduling:timer>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
