[HOME](../../../README.md)
# script

Execute a script. The script can be in any
language that supports the
<a href="https://docs.oracle.com/en/java/javase/11/scripting/index.html"></a>Java Scripting Framework</a>.
The Oddjob distribution comes packaged with <a href="https://openjdk.org/projects/nashorn/">Nashorn</a>.
All examples here use Nashorn.

<h3>Configuring a Script</h3>
The named `bind` property allows values to be passed to a script
so that they are available as variables.
Setting the property `bindSession` binds all Oddjob's beans
so that they are available as variables. This is equivalent to the variables
available <i>#{} expressions</i>.

<h3>Script Results</h3>
Variables defined within a script may be accessed in several ways.
The `variables` mapped property may be used to access the variable
by name. The {@export } property will export a variable to the oddjob
session. The {@exportAll } property will export all variables into the
oddjob session. The result of a function can be accessed with the
`result` property. Some scripts don't return a result, in which case
the `resultVariable` property can be used to take the result from
a variable. If the {code resultForState} property is true then the
result will be used to set the Completion State from the variable, 0 for
Success, otherwise Failure.

<h3>Input and Output</h3>
Script input and output can be configured using the properties `stdin`
`stdout`, `stderr` and `redirectStderr`.
In Oddjob Explorer, a scripts output is captured in the console tab for
that job.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [beans](#propertybeans) |  | 
| [bind](#propertybind) | A named bean which is made available to the script. | 
| [bindSession](#propertybindsession) | Make all Oddjob's components available to the script as bindings. | 
| [classLoader](#propertyclassloader) | ClassLoader to load the Script Engine. | 
| [export](#propertyexport) | Export bindings from the engine into the session using the given name. | 
| [exportAll](#propertyexportall) | Export all bindings from the engine into Oddjob's session. | 
| [function](#propertyfunction) |  | 
| [input](#propertyinput) | The script provided as input from file or buffer etc. | 
| [invocable](#propertyinvocable) | Allow a scripted function to be evaluated from elsewhere in Oddjob. | 
| [language](#propertylanguage) | The name of the language the script is in. | 
| [name](#propertyname) | A name, can be any text. | 
| [redirectStderr](#propertyredirectstderr) | Combine stdin and stderr. | 
| [result](#propertyresult) | The result of executing the script or the script variable chosen as the result variable with the `resultVariable` property. | 
| [resultForState](#propertyresultforstate) | If true then use the result to determine the completion state of the job. | 
| [resultVariable](#propertyresultvariable) | The variable in the script that will be used to provide the result. | 
| [script](#propertyscript) | The script provided as text. | 
| [stderr](#propertystderr) | An output to where stderr of the script will be written. | 
| [stdin](#propertystdin) | An input stream which will act as stdin for the script. | 
| [stdout](#propertystdout) | An output to where stdout for the script will be written. | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 
| [variable](#propertyvariable) | Provide access to variables declared within the script. | 
| [variables](#propertyvariables) | Use `variable` instead. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Hello World. |
| [Example 2](#example2) | Variables from and to Oddjob. |
| [Example 3](#example3) | Binding and exporting to Oddjob's session. |
| [Example 4](#example4) | Using a script to set a property on a Job elsewhere in Oddjob. |
| [Example 5](#example5) | Invoking a script to provide a substring function. |
| [Example 6](#example6) | Setting the script job to not complete. |
| [Example 7](#example7) | Defining Java Functions in JavaScript. |


### Property Detail
#### beans <a name="propertybeans"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>



#### bind <a name="propertybind"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A named bean which is made available to
the script.

#### bindSession <a name="propertybindsession"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Make all Oddjob's components available to the script as bindings.

#### classLoader <a name="propertyclassloader"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Automatically set to the current Oddjob class loader.</td></tr>
</table>

ClassLoader to load the Script Engine.

#### export <a name="propertyexport"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Export bindings from the engine into the session
using the given name.
The first entry is the name of the binding, the second is the name.
Repeating the name is tedious so the '.' character can be used to
specify the same name as the binding.

#### exportAll <a name="propertyexportall"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Export all bindings from the engine into Oddjob's
session.

#### function <a name="propertyfunction"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### input <a name="propertyinput"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes, if script isn't.</td></tr>
</table>

The script provided as input from file or buffer etc.

#### invocable <a name="propertyinvocable"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

Allow a scripted function to be evaluated
from elsewhere in Oddjob.

#### language <a name="propertylanguage"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to JavaScript.</td></tr>
</table>

The name of the language the script
is in.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### redirectStderr <a name="propertyredirectstderr"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Combine stdin and stderr.

#### result <a name="propertyresult"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

The result of executing the script or the script
variable chosen as the result variable with the `resultVariable`
property.

#### resultForState <a name="propertyresultforstate"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

If true then use the result to determine the
completion state of the job. If the result is not a number this
property will have no affect.
If the result is a number and 0 the job will COMPLETE, any
other value and the job will be INCOMPLETE.

#### resultVariable <a name="propertyresultvariable"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The variable in the script that will be used to
provide the result. The property is designed for use with scripting
languages who's execution does not produce a result. If, however
the script does produce a result and this property is set, the variable
will override the scripts return value.

#### script <a name="propertyscript"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>TEXT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes, if input isn't.</td></tr>
</table>

The script provided as text.

#### stderr <a name="propertystderr"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to none.</td></tr>
</table>

An output to where stderr
of the script will be written.

#### stdin <a name="propertystdin"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to none.</td></tr>
</table>

An input stream which will
act as stdin for the script.

#### stdout <a name="propertystdout"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to none.</td></tr>
</table>

An output to where stdout
for the script will be written.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

This flag is set by the stop method and should
be examined by any Stoppable jobs in their processing loops.

#### variable <a name="propertyvariable"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

Provide access to variables declared within the
script.

#### variables <a name="propertyvariables"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

Use `variable` instead.


### Examples
#### Example 1 <a name="example1"></a>

Hello World.
```xml
<oddjob>
    <job>
        <script id='script' language='JavaScript'>print ("Hello, World!");</script>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Variables from and to Oddjob.
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <fruit>
                        <value value="Apple"/>
                    </fruit>
                </variables>
                <script id="script" language="JavaScript">
                    <bind>
                        <value key="fruit" value="${vars.fruit}"/>
                    </bind><![CDATA[var snack = fruit;
]]></script>
                <echo id="echo"><![CDATA[snack=${script.variable(snack)}
fruit=${script.variable(fruit)}]]></echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 3 <a name="example3"></a>

Binding and exporting to Oddjob's session. The Variables
job uses Identify to insert the 'Apple' into Oddjob's session with the
name fruit. `bindSession` causes this to be available to script,
where it is assigned to the 'snack' variable. The script also defines
an add function. The {code exportAll} property causes both these to
be exported to Oddjob. The two echo jobs show how these variables are
now available in Oddjob's session. When the script job is reset, the
variables are removed from the session.
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <fruitVar>
                        <identify id="fruit">
                            <value>
                                <value value="#{'Apple'}"/>
                            </value>
                        </identify>
                    </fruitVar>
                </variables>
                <script bindSession="true" exportAll="true" id="script" language="JavaScript"><![CDATA[function add(x, y) {
    return x + y
}

snack = fruit;
]]></script>
                <echo id="echoSnack" name="Snack Is"><![CDATA[$${snack} is '${snack}' and ##{snack} is '#{typeof snack == 'undefined' ? 'undefined' : snack}']]></echo>
                <echo id="echoSum" name="Add Numbers"><![CDATA[##add(2, 3) is #{add(2, 3)}]]></echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 4 <a name="example4"></a>

Using a script to set a property on a Job elsewhere in Oddjob.
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <script id="s" language="JavaScript">
                    <bind>
                        <value key="vars" value="${v}"/>
                    </bind><![CDATA[vars.set('today', new java.util.Date());
]]></script>
                <variables id="v">
                    <formattedToday>
                        <format date="${v.today}" format="yyyyMMdd"/>
                    </formattedToday>
                </variables>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 5 <a name="example5"></a>

Invoking a script to provide a substring function.
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <script id="substr" language="JavaScript"><![CDATA[function substr(string, from, to) {
    return string.substring(from, to);
}]]></script>
                <properties id="properties">
                    <values>
                        <value key="text.before" value="Apples and Oranges"/>
                        <invoke function="substr" key="text.after">
                            <parameters>
                                <value value="${text.before}"/>
                                <value value="0"/>
                                <value value="6"/>
                            </parameters>
                            <source>
                                <value value="${substr.invocable}"/>
                            </source>
                        </invoke>
                    </values>
                </properties>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 6 <a name="example6"></a>

Setting the script job to not complete. The result of the script
is used to set the state. 0 for Success, anything else for Incomplete.
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <script language="JavaScript" resultForState="true" resultVariable="result"><![CDATA[var result = 1;]]></script>
    </job>
</oddjob>
```


#### Example 7 <a name="example7"></a>

Defining Java Functions in JavaScript.
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <script id="funcs"><![CDATA[function addTwo(x) { return new java.lang.Integer(x + 2)}
function multiplyByTwo(x) { return new java.lang.Integer(x * 2)}
]]></script>
                <echo id="add"><![CDATA[#{funcs.getFunction('addTwo').apply(5)}]]></echo>
                <echo id="multiply"><![CDATA[#{funcs.getFunction('multiplyByTwo').apply(3)}]]></echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
