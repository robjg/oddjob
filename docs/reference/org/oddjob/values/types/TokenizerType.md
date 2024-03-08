[HOME](../../../../README.md)
# tokenizer

Tokenizes text. This type provides conversion to an array
or list of strings.


The delimiter can be provided as either plain text or a regular expression.
The default delimiter is the regular expression <code>\s*,\s*</code> which is
CSV with optional white space either side.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [delimiter](#propertydelimiter) | The delimiter. | 
| [escape](#propertyescape) | An escape character to use. | 
| [quote](#propertyquote) | An quote character to use. | 
| [regexp](#propertyregexp) | True if The delimiter as a regular expression. | 
| [text](#propertytext) | The value to parse. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Tokenize comma separated values. |


### Property Detail
#### delimiter <a name="propertydelimiter"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The delimiter. This is treated as plain
text unless the regexp property is true, and then it is treated
as a regular expression.

#### escape <a name="propertyescape"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An escape character to use.

#### quote <a name="propertyquote"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An quote character to use.

#### regexp <a name="propertyregexp"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

True if The delimiter as a regular expression.

#### text <a name="propertytext"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. If missing the result of the conversion will be
 null.</td></tr>
</table>

The value to parse.


### Examples
#### Example 1 <a name="example1"></a>

Tokenize comma separated values.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <foreach preLoad="7" purgeAfter="3">
            <values>
                <tokenizer text="1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12"/>
            </values>
            <configuration>
                <xml>
                    <foreach id="test">
                        <job>
                            <echo name="Echo ${test.current}">I'm ${test.current}</echo>
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
