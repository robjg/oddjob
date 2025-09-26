[HOME](../../../../README.md)
# repeat

This job will repeatedly run its child job. The repeat
can be either for:

- Each value of a collection.
- Or a given number times.
- Or until the until property is true.



Without either a until or a times or values the job will loop indefinitely.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [count](#propertycount) | The count of repeats. | 
| [current](#propertycurrent) | The current value of the repeat. | 
| [executorService](#propertyexecutorservice) | The ExecutorService to use. | 
| [index](#propertyindex) | The same as count. | 
| [job](#propertyjob) | The job who's execution to schedule. | 
| [name](#propertyname) | A name, can be any text. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 
| [times](#propertytimes) | The number of times to repeat. | 
| [until](#propertyuntil) | Repeat will repeat until the value of this property is true. | 
| [values](#propertyvalues) | Values to repeat over. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Repeat a job 3 times. |
| [Example 2](#example2) | Repeat a job 3 times with a sequence. |


### Property Detail
#### count <a name="propertycount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

The count of repeats.

#### current <a name="propertycurrent"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

The current value of the repeat.

#### executorService <a name="propertyexecutorservice"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The ExecutorService to use. This will
be automatically set by Oddjob.

#### index <a name="propertyindex"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

The same as count. Provided so configurations
can be swapped between this and [foreach](../../../../org/oddjob/jobs/structural/ForEachJob.md) job.

#### job <a name="propertyjob"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The job who's execution
to schedule.

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

#### times <a name="propertytimes"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The number of times to repeat.

#### until <a name="propertyuntil"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Repeat will repeat until the value of
this property is true.

#### values <a name="propertyvalues"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Values to repeat over.


### Examples
#### Example 1 <a name="example1"></a>

Repeat a job 3 times.


```xml
<oddjob>
    <job>
        <repeat times="3" id="repeat">
            <job>
                <echo>Hello ${repeat.count}</echo>
            </job>
        </repeat>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Repeat a job 3 times with a sequence.


```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <repeat id="each">
            <values>
                <sequence from="1" to="3"/>
            </values>
            <job>
                <echo><![CDATA[Hello ${each.current}]]></echo>
            </job>
        </repeat>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
