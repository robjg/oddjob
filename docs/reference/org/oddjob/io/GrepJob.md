[HOME](../../../README.md)
# grep

Search files or an input stream for lines containing
a text value or matches for a regular expression.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [files](#propertyfiles) | The files to search. | 
| [ignoreCase](#propertyignorecase) | Ignore case. | 
| [in](#propertyin) | The input to search. | 
| [invert](#propertyinvert) | Invert the search. | 
| [lineNumbers](#propertylinenumbers) | Prefix output with line numbers. | 
| [matchedLineCount](#propertymatchedlinecount) | A count of the number of matched lines. | 
| [name](#propertyname) | A display name for the job. | 
| [noFilename](#propertynofilename) | Don't prefix output with a file name. | 
| [noPath](#propertynopath) | Remove the path from the file name. | 
| [out](#propertyout) | Where to write output to. | 
| [regexp](#propertyregexp) | Treat the text to match as a regular expression. | 
| [results](#propertyresults) | A collection for [org.oddjob.io.GrepLineResult](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/io/GrepLineResult.html) beans to be written to. | 
| [text](#propertytext) | Text to search for. | 
| [withFilename](#propertywithfilename) | Prefix output with a file name. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Search a buffer of text for the word red. |


### Property Detail
#### files <a name="propertyfiles"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, not if an in is provided.</td></tr>
</table>

The files to search.

#### ignoreCase <a name="propertyignorecase"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Default to false.</td></tr>
</table>

Ignore case. If true, the search will be case
insensitive.

#### in <a name="propertyin"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, not if files are provided.</td></tr>
</table>

The input to search.

#### invert <a name="propertyinvert"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Default to false.</td></tr>
</table>

Invert the search. If true, then only lines that
don't contain a match will be output.

#### lineNumbers <a name="propertylinenumbers"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Default to false.</td></tr>
</table>

Prefix output with line numbers. If true
then the number of the match in the file or input will be prepended
to each line of output.

#### matchedLineCount <a name="propertymatchedlinecount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

A count of the number of matched lines.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A display name for the job.

#### noFilename <a name="propertynofilename"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Default to false.</td></tr>
</table>

Don't prefix output with a file name. If true
then no file name will be prefixed to each line of output.

#### noPath <a name="propertynopath"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Default to false.</td></tr>
</table>

Remove the path from the file name. If true
and a file name is prefixed to each line of output, then the path
is removed.

#### out <a name="propertyout"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. If not provided no output will be written</td></tr>
</table>

Where to write output to.

#### regexp <a name="propertyregexp"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, Text is treated as plain text.</td></tr>
</table>

Treat the text to match as a regular expression.

#### results <a name="propertyresults"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A collection for [org.oddjob.io.GrepLineResult](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/io/GrepLineResult.html) beans
to be written to.

#### text <a name="propertytext"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes, not if a regexp is provided.</td></tr>
</table>

Text to search for. This is a regular expression
if the regexp property is set to true.

#### withFilename <a name="propertywithfilename"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Default to false.</td></tr>
</table>

Prefix output with a file name. If true
then the file name will be prefixed to each line of output. By
default the file name is not prefixed to a single file, only when
there are multiple files being searched. This property will prefix
the file name when only a single file is being searched.


### Examples
#### Example 1 <a name="example1"></a>

Search a buffer of text for the word red. In this example the
search is case insensitive and the results a written to the console
with the line number.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <grep ignoreCase="true" lineNumbers="true" text="red">
            <in>
                <buffer><![CDATA[5 green cars.
2 red buses.
1 RED lorry.
]]></buffer>
            </in>
            <out>
                <stdout/>
            </out>
        </grep>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
