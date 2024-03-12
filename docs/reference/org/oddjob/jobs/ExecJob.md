[HOME](../../../README.md)
# exec

Execute an external program. This job will
wait for the process to terminate and be
COMPLETE if the return state of the external program is 0,
otherwise it will be NOT COMPLETE.


Processes may behave differently on different operating systems - for
instance stop doesn't always kill the process. Please see
<a href="http://bugs.sun.com/bugdatabase/view_bug.do;:YfiG?bug_id=4109888">
http://bugs.sun.com/bugdatabase/view_bug.do;:YfiG?bug_id=4109888</a>
for additional information.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [args](#propertyargs) | A string list of arguments. | 
| [command](#propertycommand) | The command to execute. | 
| [dir](#propertydir) | The working directory. | 
| [environment](#propertyenvironment) | An environment variable to be set before the program is executed. | 
| [exitValue](#propertyexitValue) | The exit value of the process. | 
| [name](#propertyname) | A name, can be any text. | 
| [newEnvironment](#propertynewEnvironment) | Create a fresh/clean environment. | 
| [redirectStderr](#propertyredirectStderr) | Redirect the standard error stream in standard output. | 
| [stderr](#propertystderr) | An output to where stderr of the proces will be written. | 
| [stdin](#propertystdin) | An input stream which will act as stdin for the process. | 
| [stdout](#propertystdout) | An output to where stdout for the process will be written. | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 
| [stopForcibly](#propertystopForcibly) | Forcibly stop the process on stop. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A simple example. |
| [Example 2](#example2) | Using the existing environment with an additional environment variable. |
| [Example 3](#example3) | Capturing console output to a file. |
| [Example 4](#example4) | Capturing console output to the logger. |
| [Example 5](#example5) | Using the output of one process as the input of another. |


### Property Detail
#### args <a name="propertyargs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A string list of arguments.

#### command <a name="propertycommand"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>TEXT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>yes, unless args are
 provided instead.</td></tr>
</table>

The command to execute. The command is
interpreted as space delimited text which may
be specified over several lines. Arguments that need to
include spaces must be quoted. Within quoted arguments quotes
may be escaped using a backslash.

#### dir <a name="propertydir"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No</td></tr>
</table>

The working directory.

#### environment <a name="propertyenvironment"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An environment variable to be
set before the program is executed. This is a
[map](../../../org/oddjob/arooa/types/MapType.md) like property.

#### exitValue <a name="propertyexitValue"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

The exit value of the process.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### newEnvironment <a name="propertynewEnvironment"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Create a fresh/clean environment.

#### redirectStderr <a name="propertyredirectStderr"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Redirect the standard error stream in
standard output.

#### stderr <a name="propertystderr"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An output to where stderr
of the proces will be written.

#### stdin <a name="propertystdin"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An input stream which will
act as stdin for the process.

#### stdout <a name="propertystdout"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An output to where stdout
for the process will be written.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

This flag is set by the stop method and should
be examined by any Stoppable jobs in their processing loops.

#### stopForcibly <a name="propertystopForcibly"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

Forcibly stop the process on stop.


### Examples
#### Example 1 <a name="example1"></a>

A simple example.

```xml
<exec name="Batch Example">
cmd /C "${oddjob.dir}\bin\greeting.bat" Hello
</exec>
```


Oddjob will treat arguments in quotes as single program argument and allows
them to be escaped with backslash. If this is too confusing it is sometimes
easier to specify the command as individual arguments. The
above is equivalent to:

```xml
<exec name="Batch Example">
    <args>
        <list>
            <values>
                <value value="cmd"/>
                <value value="/C"/>
                <value value="${oddjob.dir}\bin\greeting.bat"/>
                <value value="Hello"/>
            </values>
        </list>
    </args>
</exec>
```


#### Example 2 <a name="example2"></a>

Using the existing environment with an additional environment variable.

```xml
<oddjob>
    <job>
        <exec name="Example With Environment" id="exec">
            <environment>
                <value key="ODDJOB_FILE" value="myfile.txt"/>
            </environment>
${platform.set.command}
        </exec>
    </job>
</oddjob>
```


#### Example 3 <a name="example3"></a>

Capturing console output to a file. The output is Oddjob's command
line help.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <properties/>
                <exec id="exec" redirectStderr="true">
                    <stdout>
                        <file file="${work.dir}/ExecOutput.log"/>
                    </stdout>
                    <![CDATA[java -jar "${oddjob.test.run.jar}" -h]]>
                </exec>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 4 <a name="example4"></a>

Capturing console output to the logger. Note how the logger output
can be defined with different log levels for stdout and sterr.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <exec>
            <stdout>
                <logout level="INFO"/>
            </stdout>
            <stderr>
                <logout level="WARN"/>
            </stderr>
            <![CDATA[java -jar ${oddjob.test.run.jar} -f Missing.xml ${logConfigArgs}/]]>
        </exec>
    </job>
</oddjob>
```


#### Example 5 <a name="example5"></a>

Using the output of one process as the input of another. Standard input for
the first process is provided by a buffer. A second buffer captures the
output of that process and passess it to the second process. The output
of the second process is captured and sent to the console of the parent
process.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <ourBuffer>
                        <buffer/>
                    </ourBuffer>
                </variables>
                <exec>
                    <stdin>
                        <buffer><![CDATA[apples
oranges
pears
]]></buffer>
                    </stdin>
                    <stdout>
                        <value value="${vars.ourBuffer}"/>
                    </stdout><![CDATA[
java -jar "${oddjob.test.run.jar}" -f "${this.dir}/OddjobCat.xml" ${logConfigArgs}
        ]]></exec>
                <exec id="exec">
                    <stdin>
                        <value value="${vars.ourBuffer}"/>
                    </stdin>
                    <stdout>
                        <stdout/>
                    </stdout><![CDATA[
java -jar "${oddjob.test.run.jar}" -f "${this.dir}/OddjobCat.xml" ${logConfigArgs}
        ]]></exec>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
