[HOME](../../../README.md)
# explorer

A container that allows multiple [org.oddjob.monitor.OddjobExplorer](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/monitor/OddjobExplorer.html)s to run.
This is the default job that Oddjob runs on startup.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [dir](#propertydir) | The directory the file chooser should use when opening and saving Oddjobs. | 
| [file](#propertyfile) | A file to load when the explorer starts. | 
| [fileHistorySize](#propertyfileHistorySize) | How many lines to keep in file history. | 
| [logFormat](#propertylogFormat) | The log format for formatting log messages. | 
| [name](#propertyname) | A name, can be any text. | 
| [oddjobServices](#propertyoddjobServices) | Internal services. | 
| [pollingInterval](#propertypollingInterval) | How often to poll in milli seconds for property updates. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 


### Property Detail
#### dir <a name="propertydir"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The directory the file chooser
should use when opening and saving Oddjobs.

#### file <a name="propertyfile"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A file to load when the explorer starts.

#### fileHistorySize <a name="propertyfileHistorySize"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

How many lines to keep in file history.

#### logFormat <a name="propertylogFormat"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The log format for formatting log messages. For more
information on the format please see <a href="http://logging.apache.org/log4j/docs/">
http://logging.apache.org/log4j/docs/</a>

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### oddjobServices <a name="propertyoddjobServices"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Internal services. Set automatically
by Oddjob.

#### pollingInterval <a name="propertypollingInterval"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

How often to poll in milli seconds for property updates.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Read only view of the internal stop flag.
This flag is cleared with a reset.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
