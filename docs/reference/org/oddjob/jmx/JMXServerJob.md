[HOME](../../../README.md)
# jmx:server

A service which allows a job hierarchy to
be monitored and managed remotely using a [jmx:client](../../../org/oddjob/jmx/JMXClientJob.md).


Security can be added using the environment property. Simple JMX security comes
prepackaged as [jmx:server-security](../../../org/oddjob/jmx/server/SimpleServerSecurity.md). Note that the access file is
an Oddjob specific access file. Oddjob requires full read/write access because
it uses JMX operations and all JMX operation require full read/write access.
Oddjob uses a JMX access format file but provides it's own primitive access
control on top the JMX layer. Oddjob's access control removes an entire java
interface from the client side proxy if any of it's methods are write.
One affect of this is that a read only account can't access properties of
the remote job with the ${server/remote-job} syntax because this functionality
is provided by the same interface (BeanUtils <code>DynaBean</code>) that allows
a remote job's properties to be written.


For more information on JMX Security see
<a href="http://java.sun.com/javase/6/docs/technotes/guides/jmx/tutorial/security.html">
The JMX Tutorial</a>.


This service will use the Platform MBeanServer if no <code>url</code>
property is provided. Creating an unsecured Oddjob server on a private
network can be achieved simply by launching Oddjob with a command line
such as:

<pre>
java -Dcom.sun.management.jmxremote.port=nnnn \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-jar run-oddjob.jar -f my-config.xml
</pre>
And then including this service somewhere in the configuration. Note that
the properties must be to the left of the -jar, not to the right because
the must be available to the JVM before Oddjob starts.


The <code>server.xml</code> Oddjob configration file in Oddjob's top
level directory provides a simple Oddjob server that uses an RMI
Registry.


More information on Oddjob servers can be found in the User Guide under
'Sharing Jobs on the Network'.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [address](#propertyaddress) | The address of this server. | 
| [environment](#propertyenvironment) | An environment such as security settings. | 
| [handlerFactories](#propertyhandlerFactories) | Additional handler factories that allow any interface to be invoked from a remote Oddjob. | 
| [logFormat](#propertylogFormat) | The log format for formatting log messages. | 
| [name](#propertyname) | A name, can be any text. | 
| [remoteIdMappings](#propertyremoteIdMappings) |  | 
| [root](#propertyroot) | The root node. | 
| [serverConnection](#propertyserverConnection) |  | 
| [url](#propertyurl) | The JMX service URL. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Creating a server using the platform MBean Server. |
| [Example 2](#example2) | Creating a server using an RMI registry. |
| [Example 3](#example3) | Creating a secure server. |


### Property Detail
#### address <a name="propertyaddress"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

The address of this server. This is mainly
useful for testing

#### environment <a name="propertyenvironment"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An environment such
as security settings.

#### handlerFactories <a name="propertyhandlerFactories"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Additional handler factories that allow
any interface to be invoked from a remote Oddjob.

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

#### remoteIdMappings <a name="propertyremoteIdMappings"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### root <a name="propertyroot"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The root node.

#### serverConnection <a name="propertyserverConnection"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### url <a name="propertyurl"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The JMX service URL. If this is not provided the
server connects to the Platform MBean Server.


### Examples
#### Example 1 <a name="example1"></a>

Creating a server using the platform MBean Server.


```xml
<oddjob xmlns:jmx="http://rgordon.co.uk/oddjob/jmx">
  <job>
    <sequential>
      <jobs>
        <jmx:server root="${echo}" />
        <echo id="echo"><![CDATA[Hello from an Oddjob Server!]]></echo>
      </jobs>
    </sequential>
  </job>
</oddjob>
```



This is probably the simplest way to launch Oddjob as a server.


Here's an example of the command used to launch it:
<pre>
java -Dcom.sun.management.jmxremote.port=13013 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -jar C:\Users\rob\projects\oddjob\run-oddjob.jar -f C:\Users\rob\projects\oddjob\test\java\org\oddjob\jmx\PlatformMBeanServerExample.xml
</pre>
For an example of a client to connect to this server see the first
example for [jmx:client](../../../org/oddjob/jmx/JMXClientJob.md).

#### Example 2 <a name="example2"></a>

Creating a server using an RMI registry.


```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <rmireg/>
                <jmx:server id="server1" root="${server-jobs}" 
                    url="service:jmx:rmi://ignored/jndi/rmi://localhost/freds-oddjob-server" xmlns:jmx="http://rgordon.co.uk/oddjob/jmx"/>
                <oddjob file="${this.dir}/ServerJobs.xml" id="server-jobs"/>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



The nested Oddjob can be any normal Oddjob configuration. Here is the
nested Oddjob used in some client examples. The greeting is in
a folder because it will only be run from the client.


```xml
<oddjob>
    <job>
        <folder>
            <jobs>
                <echo id="greeting">Hello World</echo>
            </jobs>
        </folder>
    </job>
</oddjob>
```


#### Example 3 <a name="example3"></a>

Creating a secure server.


```xml
<jmx:server xmlns:jmx="http://rgordon.co.uk/oddjob/jmx" root="${some-job}"
    url="service:jmx:rmi://ignored/jndi/rmi://localhost/my-oddjob">
    <environment>
        <jmx:server-security>
            <passwordFile>
                <file
                    file="C:\rob\java\jmx_examples\Security\simple\config\password.properties"/>
            </passwordFile>
        </jmx:server-security>
    </environment>
</jmx:server>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
