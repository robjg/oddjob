[HOME](../../README.md)
# oddjob

The starting point for a hierarchy of jobs. The Oddjob job
creates and runs a job hierarchy by processing a supplied configuration.


Oddjob creates a 'root' job on which to create the hierarchy. Through this
root Oddjob aquires the first job to run and also exposes some of it's own
properties for the jobs in the configuration to use. The root job's properties
are:

<dl>
<dt><b>job</b></dt>
<dd>The top level job. This is the single job that Oddjob runs. This property is
optional but Oddjob won't do much if a job for it to run isn't supplied.
This is the Oddjob root's only writeable property and is write only.</dd>
<dt><b>file</b></dt>
<dd>The path of the configuration file that Oddjob has loaded. Read Only.</dd>
<dt><b>dir</b></dt>
<dd>The path of the configuration file's directory. Read Only.</dd>
<dt><b>args</b></dt>
<dd>An array of arguments passed in on the command line or from a parent
Oddjob. See below. Read Only.</dd>
<dt><b>services</b></dt>
<dd>Provides access to Oddjobs underlying services. Used by
the frameworks automatic configuration mechanism to configure the properties
of jobs that are documented as set automatically. May be ignored for every day
use. Read Only.</dd>
</dl>


For these properties to be accessible the root oddjob must be given an id.
As can be seen from the examples, the author uses the id '<code>this</code>'
but the choice is arbitrary.


<b>Nesting Oddjobs</b>



An Oddjob job allows an Oddjob instance to be created within an existing Oddjob
configuration. This way complicated processes can be created in manageable and
separately testable units.


Properties of jobs in a nested Oddjob can be accessed using the notation
<i>${nested-oddjob-id/job-id.property}</i> where nested-oddjob-id is the id in
the outer configuration, not the inner one.

<b>Saving Oddjob's State</b>



The <code>persister</code> property on a nested Oddjob will allow it's state to
be saved. See the
<a href="http://rgordon.co.uk/projects/oddjob/userguide/saving.html">User Guide</a>
for more information on how to set a persister.

<b>Customising Oddjob</b>



Oddjob's <code>descriptorFactory</code> and <code>classLoader</code> properties
allow bespoke components and
types to be used. The
<a href="http://rgordon.co.uk/projects/oddjob/devguide/index.html">developer guide</a>
is all about writing custom job's for Oddjob.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [args](#propertyargs) | An array of arguments the Oddjob configuration can use. | 
| [classLoader](#propertyclassLoader) | The classLoader to use when loading the configuration. | 
| [configuration](#propertyconfiguration) | The configuration. | 
| [descriptorFactory](#propertydescriptorFactory) | An [org.oddjob.arooa.deploy.ArooaDescriptorFactory](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/arooa/deploy/ArooaDescriptorFactory.html)that will be used when loading the configuration. | 
| [dir](#propertydir) | The name of the directory the configuration file is in. | 
| [export](#propertyexport) | Values to be exported into the nested configuration. | 
| [file](#propertyfile) | The name of the configuration file. | 
| [inheritance](#propertyinheritance) | Set how an Oddjob should share the values and properties of it's parent. | 
| [inputHandler](#propertyinputHandler) | A handler for user input. | 
| [lastReset](#propertylastReset) | Used internally to remember which reset to apply after loading a configuration. | 
| [loadable](#propertyloadable) | Can Oddjob be loaded. | 
| [name](#propertyname) | A name, can be any text. | 
| [oddjobExecutors](#propertyoddjobExecutors) | Executors for Oddjob to use. | 
| [oddjobServices](#propertyoddjobServices) | Services for Oddjob to use. | 
| [persister](#propertypersister) | A component which is able to save and restore jobs. | 
| [properties](#propertyproperties) | Properties to be set in the nested configuration. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 
| [version](#propertyversion) | This Oddjob's version. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Hello World with Oddjob. |
| [Example 2](#example2) | Using an argument passed into Oddjob that may or may not be set. |
| [Example 3](#example3) | Nesting Oddjob. |
| [Example 4](#example4) | A nested Oddjob with one argument passed to the child. |
| [Example 5](#example5) | A nested Oddjob with a property past to the child. |
| [Example 6](#example6) | Using export to pass values to a nested Oddjob. |
| [Example 7](#example7) | Examples elsewhere. |


### Property Detail
#### args <a name="propertyargs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An array of arguments the Oddjob configuration can use.

#### classLoader <a name="propertyclassLoader"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The classLoader to use when loading the configuration. This classloader
will also be made available to child components that inject a classloader. This
might not always be what is required as this classloader defaults to the application classloader
not the Oddball classloader that will have loaded an Oddball component. This might be
changed in future versions of Oddjob.


See also [url-class-loader](../../org/oddjob/util/URLClassLoaderType.md)

#### configuration <a name="propertyconfiguration"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Either this or file is required.</td></tr>
</table>

The configuration. An alternative to
setting a file. This can be useful when the configuration
is to come from some other input.


See also [arooa:configuration](../../org/oddjob/arooa/types/XMLConfigurationType.md)

#### descriptorFactory <a name="propertydescriptorFactory"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An [org.oddjob.arooa.deploy.ArooaDescriptorFactory](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/arooa/deploy/ArooaDescriptorFactory.html) that
will be used when loading the configuration. This augments Oddjob's
internal descriptor, and allows custom jobs to have their own
definitions.


See also [arooa:descriptor](../../org/oddjob/arooa/deploy/ArooaDescriptorBean.md), [arooa:descriptors](../../org/oddjob/arooa/deploy/ListDescriptorBean.md)
and [oddballs](../../org/oddjob/oddballs/OddballsDescriptorFactory.md)

#### dir <a name="propertydir"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>R/O</td></tr>
</table>

The name of the directory the configuration
file is in.

#### export <a name="propertyexport"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No</td></tr>
</table>

Values to be exported into the nested
configuration. Values will be registered in the inner
oddjob using the key of this mapped property.

#### file <a name="propertyfile"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of the configuration file.
to configure this oddjob.

#### inheritance <a name="propertyinheritance"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to PROPERTIES.</td></tr>
</table>

Set how an Oddjob should share the values and
properties of it's parent. Valid values are:
<dl>
<dt>NONE</dt>
<dd>No values or properties are automatically inherited.</dd>
<dt>PROPERTIES</dt>
<dd>All properties are inherited. Only properties are inherited, values
must be exported explicitly using the export property.</dd>
<dt>SHARED</dt>
<dd>All properties and values are shared between the parent and child
Oddjobs. Any properties or values set in the child will be visible
in the parent. This setting is particularly useful for shared common
configuration.</dd>

#### inputHandler <a name="propertyinputHandler"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A handler for user input. This will be
provided internally and will only be required in specialised
situations.

#### lastReset <a name="propertylastReset"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Used internally to remember which
reset to apply after loading a configuration.

#### loadable <a name="propertyloadable"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

Can Oddjob be loaded. Used by the Load/Unload actions.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### oddjobExecutors <a name="propertyoddjobExecutors"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Executors for Oddjob to use. This is
set automatically in Oddjob. For advanced use, user
supplied [org.oddjob.OddjobExecutors](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/OddjobExecutors.html) may be provided.

#### oddjobServices <a name="propertyoddjobServices"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Services for Oddjob to use. This is
set automatically in Oddjob. Unlikely to be required.

#### persister <a name="propertypersister"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A component which is able to save and restore
jobs.


See also [file-persister](../../org/oddjob/persist/FilePersister.md) and [sql-persister-service](../../org/oddjob/sql/SQLPersisterService.md).

#### properties <a name="propertyproperties"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No</td></tr>
</table>

Properties to be set in the nested
configuration. Can be set using a [properties](../../org/oddjob/values/properties/PropertiesType.md).

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Read only view of the internal stop flag.
This flag is cleared with a reset.

#### version <a name="propertyversion"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

This Oddjob's version.


### Examples
#### Example 1 <a name="example1"></a>

Hello World with Oddjob. Oddjob is configured to run the [echo](../../org/oddjob/jobs/EchoJob.md) job.

```xml
<oddjob>
    <job>
        <echo id="echo">Hello World</echo>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Using an argument passed into Oddjob that may or may not be set.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <state:if xmlns:state="http://rgordon.co.uk/oddjob/state">
                    <jobs>
                        <check value="${this.args[0]}"/>
                        <properties>
                            <values>
                                <value key="our.file.name" value="${this.args[0]}"/>
                            </values>
                        </properties>
                        <input>
                            <requests>
                                <input-text prompt="File Name?" property="our.file.name"/>
                            </requests>
                        </input>
                    </jobs>
                </state:if>
                <echo><![CDATA[File Name is ${our.file.name}]]></echo>
            </jobs>
        </sequential>
    </job>
</oddjob>

```


#### Example 3 <a name="example3"></a>

Nesting Oddjob. Note how the <code>dir</code> property of the
Oddjob root is used as the path of the nested configuration file.

```xml
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <oddjob id="nested" 
                        file="${this.dir}/HelloWorld.xml"/>
                <echo>Nested job said: ${nested/echo.text}</echo>
            </jobs>
        </sequential>
    </job>
</oddjob>

```




The nested job is the first example:

```xml
<oddjob>
    <job>
        <echo id="echo">Hello World</echo>
    </job>
</oddjob>
```


This example also shows how a property within the nested file can be
accessed within the parent configuration.

#### Example 4 <a name="example4"></a>

A nested Oddjob with one argument passed to the child.

```xml
<oddjob id="this">
    <job>
        <oddjob id='nested' file="${this.dir}/EchoArg.xml">
            <args>
                <list>
                    <values>
                        <value value='Hello World'/>
                    </values>
                </list>
            </args>
        </oddjob>
    </job>
</oddjob>

```


And EchoArg.xml:

```xml
<oddjob id='this'>
    <job>
        <echo id='echo'>${this.args[0]}</echo>
    </job>
</oddjob>

```


#### Example 5 <a name="example5"></a>

A nested Oddjob with a property past to the child.

```xml
<oddjob id="this">
    <job>
        <oddjob id='nested' file="${this.dir}/EchoProperty.xml">
            <properties>
                <properties>
                    <values>
                        <value key='our.greeting' value='Hello World'/>
                    </values>
                </properties>
            </properties>
        </oddjob>
    </job>
</oddjob>

```


And EchoProperty.xml:

```xml
<oddjob id='this'>
    <job>
        <echo id='echo'>${our.greeting}</echo>
    </job>
</oddjob>

```


Unlike the properties of jobs, free format properties like this can't be
accessed using the nested convention.
<pre>
${nested/our.greeting} DOES NOT WORK!
</pre>
This may be fixed in future versions.

#### Example 6 <a name="example6"></a>

Using export to pass values to a nested Oddjob.

```xml
<oddjob>
    <job>
        <sequential>
            <jobs>
                <folder>
                    <jobs>
                        <echo id="secret">I'm a secret job</echo>
                    </jobs>
                </folder>
                <oddjob id="inner">
                    <export>
                        <value key="secret" value="${secret}"/>
                    </export>
                    <configuration>
                        <arooa:configuration xmlns:arooa="http://rgordon.co.uk/oddjob/arooa">
                            <xml>
                                <xml>
                                    <oddjob>
                                        <job>
                                            <run job="${secret}"/>
                                        </job>
                                    </oddjob>
                                </xml>
                            </xml>
                        </arooa:configuration>
                    </configuration>
                </oddjob>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


Here a job is exported into a nested Oddjob. The
exported object is actually a [value](../../org/oddjob/arooa/types/ValueType.md). The value is converted back
to the job when the job property of the run job is set. Expressions such
as <code>${secret.text}</code> are not valid (because value does not have a
text property!). Even <code>${secret.value.text}</code> will not work because
of value wraps the job in yet another layer of complexity.

#### Example 7 <a name="example7"></a>

Examples elsewhere.

- [file-persister](../../org/oddjob/persist/FilePersister.md)
- [sql-persister-service](../../org/oddjob/sql/SQLPersisterService.md)
- [oddballs](../../org/oddjob/oddballs/OddballsDescriptorFactory.md)
- [url-class-loader](../../org/oddjob/util/URLClassLoaderType.md)





-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
