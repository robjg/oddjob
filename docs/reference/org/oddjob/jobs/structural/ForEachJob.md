[HOME](../../../../README.md)
# foreach

A job which executes its child jobs for
each of the provided values. The child job can access the current
value using the pseudo property 'current' to gain access to the
current value. The pseudo property 'index' provides a 0 based number for
the instance.


The return state of this job depends on the return state
of the children (like [sequential](../../../../org/oddjob/jobs/structural/SequentialJob.md)). Hard resetting this job
will cause the children to be destroyed and recreated on the next run
(with possibly new values). Soft resetting this job will reset the
children but when re-run will not reconfigure the values.


As yet There is no persistence for child jobs.


It is not possible to reference the internal jobs via their id from
outside the foreach job, but within
the foreach internal configuration they can reference each other and
themselves via their ids.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [configuration](#propertyconfiguration) | The configuration that will be parsed for each value. | 
| [current](#propertycurrent) | The current value | 
| [executorService](#propertyexecutorService) | The ExecutorService to use. | 
| [file](#propertyfile) | The name of the configuration file. | 
| [index](#propertyindex) | The current index in the values. | 
| [loadable](#propertyloadable) |  | 
| [name](#propertyname) | A name, can be any text. | 
| [parallel](#propertyparallel) | Should jobs be executed in parallel. | 
| [preLoad](#propertypreLoad) | The number of values to pre-load configurations for. | 
| [purgeAfter](#propertypurgeAfter) | The number of completed jobs to keep. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 
| [values](#propertyvalues) | Any value. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | For each of 3 values. |
| [Example 2](#example2) | For each of 3 files. |
| [Example 3](#example3) | Executing children in parallel. |
| [Example 4](#example4) | Using an execution window. |


### Property Detail
#### configuration <a name="propertyconfiguration"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The configuration that will be parsed
for each value.

#### current <a name="propertycurrent"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>R/O.</td></tr>
</table>

The current value

#### executorService <a name="propertyexecutorService"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The ExecutorService to use. This will
be automatically set by Oddjob.

#### file <a name="propertyfile"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of the configuration file.
to use for configuration.

#### index <a name="propertyindex"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>R/O.</td></tr>
</table>

The current index in the
values.

#### loadable <a name="propertyloadable"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### parallel <a name="propertyparallel"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to false.</td></tr>
</table>

Should jobs be executed in parallel.

#### preLoad <a name="propertypreLoad"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to all configurations being loaded first.</td></tr>
</table>

The number of values to pre-load configurations for.
This property can be used with large sets of values to ensure that only a
certain number are pre-loaded before execution starts.


Setting this property to 0 means that all configuration will be
initially loaded.

#### purgeAfter <a name="propertypurgeAfter"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to no complete jobs being purged.</td></tr>
</table>

The number of completed jobs to keep. Oddjob configurations
can be quite memory intensive, mainly due to logging, purging complete jobs
will stop too much memory being taken.


Setting this property to 0
means that no complete jobs will be purged.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

Read only view of the internal stop flag.
This flag is cleared with a reset.

#### values <a name="propertyvalues"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Any value.


### Examples
#### Example 1 <a name="example1"></a>

For each of 3 values.


```xml
<oddjob id="this" xmlns:arooa="http://rgordon.co.uk/oddjob/arooa">
    <job>
        <foreach id="foreach">
            <values>
                <list>
                    <values>
                        <value value="Red"/>
                        <value value="Blue"/>
                        <value value="Green"/>
                    </values>
                </list>
            </values>
            <configuration>
                <arooa:configuration resource="org/oddjob/jobs/structural/ForEachEchoColour.xml"/>
            </configuration>
        </foreach>
    </job>
</oddjob>

```



The internal configuration is:


```xml
<foreach id="colours">
    <job>
        <echo id="echo-colour" name="${colours.current}">I'm number ${colours.index} and my name is ${echo-colour.name}</echo>
    </job>
</foreach>
```



Unlike other jobs, a job in a for each has it's name configured when it is
loaded, before it is run. The job references its self using its id.


This example will display the following on the console:
<pre>
I'm number 0 and my name is Red
I'm number 1 and my name is Blue
I'm number 2 and my name is Green
</pre>

#### Example 2 <a name="example2"></a>

For each of 3 files. The 3 files <code>test1.txt</code>,
<code>test2.txt</code> and <code>test3.txt</code> are
copied to the <code>work/foreach directory</code>. The oddjob argument
<code>${this.args[0]}</code> is so that a base directory can be passed
in as part of the unit test for this example.


```xml
<oddjob id="this" xmlns:arooa="http://rgordon.co.uk/oddjob/arooa">
    <job>
        <foreach>
            <values>
                <files files="${base.dir}/test/io/reference/test?.txt"/>
            </values>
            <configuration>
                <arooa:configuration>
                    <xml>
                        <xml>
                            <foreach id="copy-files">
                                <job>
                                    <copy to="${some.dir}">
                                        <from>
                                            <value
                                                value="${copy-files.current}"/>
                                        </from>
                                    </copy>
                                </job>
                            </foreach>
                        </xml>
                    </xml>
                </arooa:configuration>
            </configuration>
        </foreach>
    </job>
</oddjob>

```



Also [exists](../../../../org/oddjob/io/ExistsJob.md) has a similar example.

#### Example 3 <a name="example3"></a>

Executing children in parallel. This example uses a
[throttle](../../../../org/oddjob/scheduling/ExecutorThrottleType.md) to limit the number of parallel
executions to three.


```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <foreach parallel="true">
            <values>
                <tokenizer text="1,2,3,4,5,6,7,8,9"/>
            </values>
            <configuration>
                <xml>
                    <foreach id="loop">
                        <job>
                            <wait name="Wait ${loop.current}"/>
                        </job>
                    </foreach>
                </xml>
            </configuration>
            <executorService>
                <throttle limit="3"/>
            </executorService>
        </foreach>
    </job>
</oddjob>

```


#### Example 4 <a name="example4"></a>

Using an execution window. Only the configuration for two jobs will be
pre-loaded, and only the last three complete jobs will remain loaded.


```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <foreach preLoad="2" purgeAfter="3">
            <values>
                <tokenizer text="1,2,3,4,5,6,7,8,9,10,11,12,13,14,15"/>
            </values>
            <configuration>
                <xml>
                    <foreach id="loop">
                        <job>
                            <wait name="Wait ${loop.current} " pause="1"/>
                        </job>
                    </foreach>
                </xml>
            </configuration>
        </foreach>
    </job>
</oddjob>

```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
