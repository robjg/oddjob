[HOME](../../../README.md)
# tee

Split output to multiple other outputs.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [input](#propertyinput) | An input stream that will be copied to the outputs. | 
| [outputs](#propertyoutputs) | List of outputs to split to. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Copy a buffer to stdout, the log, and to a file. |
| [Example 2](#example2) | Copy data to stdout as it is being read during a copy from one buffer to another. |


### Property Detail
#### input <a name="propertyinput"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Only if this type is required to be an input stream.</td></tr>
</table>

An input stream that will be copied to the outputs.

#### outputs <a name="propertyoutputs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No, output will be thrown away if missing.</td></tr>
</table>

List of outputs to split to.


### Examples
#### Example 1 <a name="example1"></a>

Copy a buffer to stdout, the log, and to a file.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
  <job>
    <sequential>
      <jobs>
        <properties>
          <values>
            <value key="work.dir" value="work/io" />
          </values>
        </properties>
        <copy>
          <input>
            <buffer><![CDATA[Duplicate This!
]]></buffer>
          </input>
          <output>
            <tee>
              <outputs>
                <stdout />
                <logout />
                <file file="${work.dir}/TeeTypeTest.txt" />
              </outputs>
            </tee>
          </output>
        </copy>
      </jobs>
    </sequential>
  </job>
</oddjob>

```


#### Example 2 <a name="example2"></a>

Copy data to stdout as it is being read during a copy from one buffer to
another.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <copy>
            <input>
                <tee>
                    <input>
                        <buffer><![CDATA[This will be copied when read.
]]></buffer>
                    </input>
                    <outputs>
                        <stdout/>
                    </outputs>
                </tee>
            </input>
            <output>
                <buffer/>
            </output>
        </copy>
    </job>
</oddjob>

```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
