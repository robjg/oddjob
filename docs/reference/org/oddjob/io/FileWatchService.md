[HOME](../../../README.md)
# file-watch

Provide a service for subscribers to watch a file system for Files existing, being created or being modified.


If the file is created during subscription the consumer may receive a notification for the same file twice. Once
the subscription has succeeded a consumer should receive every creation and modification happening to the file.


If this service is stopped no notification is sent to consumers. Consumers must use the state of this service
to know that it has stopped.


Consumers will receive creation and modification events on a different thread to the initial event if the
file exists.


<em>Implementation Note:</em> This facility is still a work in progress. Requiring this service
in a configuration is messy. In future releases this service should be hidden from users.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [filter](#propertyfilter) | Provide a regular expression filter on the directory to reduce the stream of events. | 
| [kinds](#propertykinds) | Kinds of events to watch, as specified by the <a link="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/StandardWatchEventKinds.html">Standard Watch Event Kinds</a>, Either ENTRY_CREATE or ENTRY_MODIFY. | 
| [name](#propertyname) | The name of this service. | 
| [numberOfConsumers](#propertynumberofconsumers) |  | 
| [paths](#propertypaths) |  | 
| [to](#propertyto) |  | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Trigger when two files arrive. |


### Property Detail
#### filter <a name="propertyfilter"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Provide a regular expression filter on the directory to reduce the stream of events.

#### kinds <a name="propertykinds"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Kinds of events to watch, as specified by the
<a link="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/StandardWatchEventKinds.html">Standard Watch Event Kinds</a>,
Either ENTRY_CREATE or ENTRY_MODIFY. Note that ENTRY_DELETE will not work in the current implementation.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of this service.

#### numberOfConsumers <a name="propertynumberofconsumers"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### paths <a name="propertypaths"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>



#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>




### Examples
#### Example 1 <a name="example1"></a>

Trigger when two files arrive.


```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <mkdir dir="${some.dir}/etc" name="Create Test Dir"/>
                <file-watch id="file-watch" kinds="ENTRY_CREATE"/>
                <events:when id="both-files" name="When Both Files" xmlns:events="oddjob:events">
                    <jobs>
                        <events:list eventOperator="ALL">
                            <of>
                                <events:watch name="Watch File 1">
                                    <eventSource>
                                        <events:file>
                                            <fileWatch>
                                                <value value="${file-watch}"/>
                                            </fileWatch>
                                            <file>
                                                <value value="${some.dir}/file1.txt"/>
                                            </file>
                                        </events:file>
                                    </eventSource>
                                </events:watch>
                                <events:watch name="Watch File 2">
                                    <eventSource>
                                        <events:file>
                                            <fileWatch>
                                                <value value="${file-watch}"/>
                                            </fileWatch>
                                            <file>
                                                <value value="${some.dir}/file2.txt"/>
                                            </file>
                                        </events:file>
                                    </eventSource>
                                </events:watch>
                            </of>
                        </events:list>
                        <echo id="task">
                            <![CDATA[${both-files.trigger.ofs}]]>
                        </echo>
                    </jobs>
                </events:when>
                <folder>
                    <jobs>
                        <copy id="createFile1" name="Create File 1" to="${some.dir}/file1.txt">
                            <input>
                                <buffer>
                                    <![CDATA[Test1]]>
                                </buffer>
                            </input>
                        </copy>
                        <copy id="createFile2" name="Create File 2" to="${some.dir}/file2.txt">
                            <input>
                                <buffer>
                                    <![CDATA[Test2]]>
                                </buffer>
                            </input>
                        </copy>
                        <delete name="Delete All Test Files">
                            <files>
                                <files files="${some.dir}/*"/>
                            </files>
                        </delete>
                    </jobs>
                </folder>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
