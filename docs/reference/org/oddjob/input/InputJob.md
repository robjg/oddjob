[HOME](../../../README.md)
# input

Ask for input from the user.


The medium with
which Oddjob asks for input will depend on how it's running. When
running in Oddjob Explorer a GUI dialogue will be used. When running
from the console, input from the console will be requested.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [inputHandler](#propertyinputHandler) | The input handler to use. | 
| [name](#propertyname) | A name, can be any text. | 
| [properties](#propertyproperties) | Provide all the merged properties defined by this job. | 
| [requests](#propertyrequests) | A list of requests for input. | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Request lots of input. |


### Property Detail
#### inputHandler <a name="propertyinputHandler"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. This will be set automatically by Oddjob.</td></tr>
</table>

The input handler to use.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### properties <a name="propertyproperties"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

Provide all the merged properties defined by this
job.

#### requests <a name="propertyrequests"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, but there will be no values.</td></tr>
</table>

A list of requests for input.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

This flag is set by the stop method and should
be examined by any Stoppable jobs in their processing loops.


### Examples
#### Example 1 <a name="example1"></a>

Request lots of input.

```xml
<oddjob>
    <job>
        <sequential>
            <jobs>
                <input>
                    <requests>
                        <input-file prompt="Install Directory"
                            default="/home/oddjob/foo"
                            property="config.install"/>
                        <input-text prompt="System"
                            default="Development"
                            property="config.system"/>
                        <input-text prompt="Username"
                            property="config.username"/>
                        <input-password
                            prompt="Password"
                            property="config.password"/>
                        <input-confirm prompt="Agree To Licence"
                            default = "false"
                            property="config.agree"/>
                    </requests>
                </input>
                <check value="${config.agree}" eq="true"/>
                <echo>Password for ${config.username} is ${config.password}</echo>
                <input>
                    <requests>
                        <input-message>Logging On to ${config.system} Now!</input-message>
                    </requests>
                </input>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
