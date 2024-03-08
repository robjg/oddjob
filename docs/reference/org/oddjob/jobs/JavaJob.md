[HOME](../../../README.md)
# java

Execute a Java Program in a separate process.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [className](#propertyclassName) | The class name | 
| [classPath](#propertyclassPath) | The class path of the java program. | 
| [dir](#propertydir) | The working directory. | 
| [environment](#propertyenvironment) | An environment variable to be set before the program is executed. | 
| [exitValue](#propertyexitValue) | The exit value of the process. | 
| [name](#propertyname) | A name, can be any text. | 
| [newEnvironment](#propertynewEnvironment) | Create a fresh/clean environment. | 
| [programArgs](#propertyprogramArgs) | Space separated program arguments | 
| [redirectStderr](#propertyredirectStderr) | Redirect the standard error stream in standard output. | 
| [stderr](#propertystderr) | An output to where stderr of the proces will be written. | 
| [stdin](#propertystdin) | An input stream which will act as stdin for the process. | 
| [stdout](#propertystdout) | An output to where stdout for the process will be written. | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 
| [stopForcibly](#propertystopForcibly) | Forcibly stop the process on stop. | 
| [vmArgs](#propertyvmArgs) | Space separated vm arguments | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A simple example. |


### Property Detail
#### className <a name="propertyclassName"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>yes.</td></tr>
</table>

The class name

#### classPath <a name="propertyclassPath"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>no.</td></tr>
</table>

The class path of the java program.

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

#### programArgs <a name="propertyprogramArgs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>no.</td></tr>
</table>

Space separated program arguments

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

#### vmArgs <a name="propertyvmArgs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>no.</td></tr>
</table>

Space separated vm arguments


### Examples
#### Example 1 <a name="example1"></a>

A simple example.

```xml
<oddjob>
    <job>
        <java id="javaExample"
              name="Java Example"
              className="org.oddjob.jobs.HelloMain"
              programArgs="Alice Bob"
              vmArgs="-Dour.greeting=Hello">
          <classPath>
              <list>
                  <values>
                      <value value="${classPath}"/>
                  </values>
              </list>
          </classPath>
        </java>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
