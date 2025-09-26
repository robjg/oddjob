[HOME](../../../../README.md)
# jmx:server-security

Provide a JMX simple security environment for a
[jmx:server](../../../../org/oddjob/jmx/JMXServerJob.md).


If SSL is used the appropriate JVM parameters need to be set for both
client and server. See  <a href="http://java.sun.com/javase/6/docs/technotes/guides/jmx/tutorial/security.html">
The JMX Tutorial</a>.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [accessFile](#propertyaccessfile) | The location of the access file. | 
| [passwordFile](#propertypasswordfile) | The location of the password file. | 
| [useSSL](#propertyusessl) | Use Secure Sockets (SSL). | 


### Property Detail
#### accessFile <a name="propertyaccessfile"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The location of the access file.

#### passwordFile <a name="propertypasswordfile"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Not sure.</td></tr>
</table>

The location of the password file.

#### useSSL <a name="propertyusessl"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Use Secure Sockets (SSL).


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
