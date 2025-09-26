[HOME](../../../README.md)
# script

Execute a script. The script can be in any
language that supports the Java Scripting Framework.


The named beans property allow values to be passed to and from the
script.


Script output is captured in a console that is visible from Oddjob Explorer
in addition to any output properties.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [beans](#propertybeans) | A named bean which is made available to the script. | 
| [classLoader](#propertyclassloader) | ClassLoader to load the Script Engine. | 
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
| [variables](#propertyvariables) | Provide access to variables declared within the script. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Hello World. |
| [Example 2](#example2) | Variables from and to Oddjob. |
| [Example 3](#example3) | Using a script to set a property on a Job elsewhere in Oddjob. |
| [Example 4](#example4) | Invoking a script to provide a substring function. |
| [Example 5](#example5) | Setting the script job to not complete. |
| [Example 6](#example6) | Setting the script job to not complete. |
| [Example 7](#example7) | Defining Java Functions in JavaScript. |


### Property Detail
#### beans <a name="propertybeans"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A named bean which is made available to
the script.

#### classLoader <a name="propertyclassloader"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Automatically set to the current Oddjob class loader.</td></tr>
</table>

ClassLoader to load the Script Engine.

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

#### variables <a name="propertyvariables"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Provide access to variables declared within the
script.


### Examples
#### Example 1 <a name="example1"></a>

Hello World.
```xml
<oddjob>
    <job>
        <script id='s' language='JavaScript'>print ("hello world\n");</script>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Variables from and to Oddjob.
```xml
<oddjob>
 <job> 
 <sequential>
 <jobs>
  <script id='s' language='JavaScript'>
   <input>
    <buffer>
var snack = fruit;
    </buffer>
   </input>
   <beans>
    <value key='fruit' value='apple'/>
   </beans>
  </script>
  <echo id='e'>${s.variables(fruit)}</echo>
 </jobs>
 </sequential>
 </job> 
</oddjob>
```


#### Example 3 <a name="example3"></a>

Using a script to set a property on a Job elsewhere in Oddjob.
```xml
<oddjob>
 <job>
  <sequential>
   <jobs>
    <script id='s' language='JavaScript'>
     <input>
      <buffer>
vars.set('today', new java.util.Date());
      </buffer>
     </input>
     <beans>
      <value key='vars' value='${v}'/>
     </beans>
    </script>
    <variables id='v'>
     <formattedToday>
      <format date='${v.today}' format='yyyyMMdd'/>
     </formattedToday>
    </variables>
   </jobs>
  </sequential>
 </job>
</oddjob>
```


#### Example 4 <a name="example4"></a>

Invoking a script to provide a substring function.
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <script id="substr" language="JavaScript">
                    <input>
                        <buffer><![CDATA[function substr(string, from, to) {
    return string.substring(from, to);
}]]></buffer>
                    </input>
                </script>
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


#### Example 5 <a name="example5"></a>

Setting the script job to not complete.
```xml
<oddjob>
  <job>
    <script language='JavaScript' 
            resultVariable="result"
            resultForState="true">
      <input>
        <buffer>
var result = 1;
        </buffer>
      </input>
    </script>
  </job>
</oddjob>
```


#### Example 6 <a name="example6"></a>

Setting the script job to not complete.
```xml
<oddjob>
  <job>
    <script language='JavaScript' 
            resultVariable="result"
            resultForState="true">
      <input>
        <buffer>
var result = 1;
        </buffer>
      </input>
    </script>
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
