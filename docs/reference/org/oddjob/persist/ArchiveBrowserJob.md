[HOME](../../../README.md)
# archive-browser

Browse archives previously create with an [archive](../../../org/oddjob/persist/ArchiveJob.md).

### Property Summary

| Property | Description |
| -------- | ----------- |
| [archiveName](#propertyarchiveName) | The name of the archive to browse. | 
| [archiver](#propertyarchiver) | The persister to use to restore archives. | 
| [name](#propertyname) | A name, can be any text. | 
| [stop](#propertystop) | This flag is set by the stop method and should be examined by any Stoppable jobs in their processing loops. | 


### Property Detail
#### archiveName <a name="propertyarchiveName"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The name of the archive to browse.

#### archiver <a name="propertyarchiver"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes, but will fall back on the current Oddjob persister.</td></tr>
</table>

The persister to use to restore archives.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read Only.</td></tr>
</table>

This flag is set by the stop method and should
be examined by any Stoppable jobs in their processing loops.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
