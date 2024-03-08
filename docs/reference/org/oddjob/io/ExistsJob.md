[HOME](../../../README.md)
# exists

Test if a file exists. This job will flag
complete if the file exists, not complete if it doesn't, and
will signal an exception if the path to the file does not exist.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [exists](#propertyexists) | The files that match the file specification. | 
| [file](#propertyfile) | The file specification. | 
| [lastModified](#propertylastModified) | If a single file is found, this is the last modified date of the file. | 
| [name](#propertyname) | A name, can be any text. | 
| [result](#propertyresult) |  | 
| [size](#propertysize) | If a single file is found, this is the size of the file in bytes, or -1 if a single file hasn't been found. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A simple example checking for a single file. |
| [Example 2](#example2) | File polling. |
| [Example 3](#example3) | Using exists and processing the files found. |


### Property Detail
#### exists <a name="propertyexists"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>R/O.</td></tr>
</table>

The files that match the file specification.

#### file <a name="propertyfile"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The file specification. The file specification
can contain wild card characters.

#### lastModified <a name="propertylastModified"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>R/O.</td></tr>
</table>

If a single file is found, this is the last
modified date of the file.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### result <a name="propertyresult"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### size <a name="propertysize"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>R/O.</td></tr>
</table>

If a single file is found, this is the size
of the file in bytes, or -1 if a single file hasn't been found.


### Examples
#### Example 1 <a name="example1"></a>

A simple example checking for a single file.

```xml
<oddjob id='this'>
    <job>
        <exists file='${this.args[0]}/test/io/reference/test1.txt'/>
    </job>
</oddjob>

```


#### Example 2 <a name="example2"></a>

File polling.

```xml
<oddjob xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling"
        xmlns:schedules="http://rgordon.co.uk/oddjob/schedules"
        id="this">
    <job>
        <sequential name="The Classic File Polling Example">
            <jobs>
                <scheduling:retry limits="${timer.current}">
                    <schedule>
                        <schedules:interval interval="00:00:02"/>
                    </schedule>
                    <job>
                        <sequential id="echo-when-file">
                            <jobs>
                                <exists id="check"
                                        name="Check File Exists"
                                        file="${work.dir}/done.flag"/>
                                <echo>Found ${check.exists[0]}</echo>
                            </jobs>
                        </sequential>
                    </job>
                </scheduling:retry>
            </jobs>
        </sequential>
    </job>
</oddjob>

```


#### Example 3 <a name="example3"></a>

Using exists and processing the files found.

```xml
<oddjob id="this">
    <job>
        <sequential name="Find Files">
            <jobs>
                <exists id="exists"
                    file="${this.args[0]}/test/io/reference/test*.txt"/>
                <foreach id="found">
                    <values>
                        <value value="${exists.exists}"/>
                    </values>
                    <configuration>
                        <xml>
                            <foreach id="found">
                                <job>
                                    <echo>found ${found.current}</echo>
                                </job>
                            </foreach>
                        </xml>
                    </configuration>
                </foreach>
            </jobs>
        </sequential>
    </job>
</oddjob>



```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
