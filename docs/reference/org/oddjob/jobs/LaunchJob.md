[HOME](../../../README.md)
# launch

Launch an application via it's main method. The
application is launched in same JVM as Oddjob, but in it's own class loader.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [args](#propertyargs) | The arguments to pass to main. | 
| [classLoader](#propertyclassLoader) | The class loader in which to find the main class. | 
| [className](#propertyclassName) | The name of the class that contains the main method. | 
| [name](#propertyname) | The name of this job. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | An Oddjob the launches Oddjob. |


### Property Detail
#### args <a name="propertyargs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The arguments to pass to main.

#### classLoader <a name="propertyclassLoader"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The class loader in which to find the main class.

#### className <a name="propertyclassName"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The name of the class that contains the main method.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of this job.


### Examples
#### Example 1 <a name="example1"></a>

An Oddjob the launches Oddjob. In the example, args[0] is org.oddjob.Main, args[1] is the
oddjob home directory. The classes directory is included in the class path
for the log4j.properties file otherwise Log4j would attempt to use one
from ClassLoader.getSystemLoader() which will be the original application
class loader.

```xml
<oddjob id="this">
    <job>
        <launch className="${this.args[1]}">
            <args>
                <list>
                    <values>
                        <value value="-nb"/>
                        <value value="-f"/>
                        <value value="${this.args[0]}/test/conf/echo-class-loader.xml"/>
                        <value value="-l"/>
                        <value value="${this.args[0]}/${this.args[2]}"/>
                    </values>
                </list>
            </args>
            <classLoader>
                <url-class-loader noInherit="true">
                    <files>
                        <files files="${this.args[3]}/lib/*.jar">
                            <list>
                                <file file="${this.args[3]}/classes"/>
                                <file file="${this.args[3]}/opt/classes"/>
                            </list>
                        </files>
                    </files>
                </url-class-loader>
            </classLoader>
        </launch>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
