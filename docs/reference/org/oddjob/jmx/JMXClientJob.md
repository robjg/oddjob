[HOME](../../../README.md)
# jmx:client

Connect to an Oddjob [jmx:server](../../../org/oddjob/jmx/JMXServerJob.md).
This job allows remote jobs to be monitored and controlled from
a local Oddjob.


This service will run until it is manually stopped or until the connection
to the remote server is lost. If this job is stopped it's state will be
COMPLETE, if the connection is lost the state state will be EXCEPTION.


To access and control jobs on a server from within a configuration file this
client must have an id. If the client has an id of <code>'freds-pc'</code>
and the job on the server has an id of <code>'freds-job'</code>. The job on
the server can be accessed from the client using the expression
<code>${freds-pc/freds-job}</code>.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [connection](#propertyconnection) | The JMX service URL. | 
| [environment](#propertyenvironment) | The environment. | 
| [handlerFactories](#propertyhandlerfactories) | Additional handler factories that allow any interface to be invoked from a remote Oddjob. | 
| [heartbeat](#propertyheartbeat) | The heart beat interval, in milliseconds. | 
| [logPollingInterval](#propertylogpollinginterval) | The number of milliseconds between polling for new log events. | 
| [maxConsoleLines](#propertymaxconsolelines) | The maximum number of console lines to retrieve for any component. | 
| [maxLoggerLines](#propertymaxloggerlines) | The maximum number of log lines to retrieve for any component. | 
| [name](#propertyname) | A name, can be any text. | 
| [url](#propertyurl) | This property is now deprecated in favour of connection which reflects that the connection string no longer need only be a full JMX URL. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Connect to a remote server that is using the Platform MBean Server. |
| [Example 2](#example2) | To create a connection to a remote server that is using an RMI registry using the full form of the JMX URL. |
| [Example 3](#example3) | Connect, run a remote job, and disconnect. |
| [Example 4](#example4) | Connect using a username and password to a secure server. |
| [Example 5](#example5) | A local job triggers when a server job runs. |


### Property Detail
#### connection <a name="propertyconnection"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. If not provided the client connects to the Platform 
 MBean Server for the current VM.</td></tr>
</table>

The JMX service URL. This is can be either
the full blown convoluted JMX Service URL starting
<code>service.jmx....</code> or it can just be the last part of the
form <code>hostname[:port][/instance-name]</code>.

#### environment <a name="propertyenvironment"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The environment. Typically username/password
credentials.

#### handlerFactories <a name="propertyhandlerfactories"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Additional handler factories that allow
any interface to be invoked from a remote Oddjob.

#### heartbeat <a name="propertyheartbeat"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Not, defaults to 5 seconds.</td></tr>
</table>

The heart beat interval, in milliseconds.

#### logPollingInterval <a name="propertylogpollinginterval"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The number of milliseconds between polling for new
log events. Defaults to 5.

#### maxConsoleLines <a name="propertymaxconsolelines"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The maximum number of console lines to retrieve for any
component.

#### maxLoggerLines <a name="propertymaxloggerlines"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The maximum number of log lines to retrieve for any
component.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### url <a name="propertyurl"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

This property is now deprecated in favour of
connection which reflects that the connection string no longer need
only be a full JMX URL.


### Examples
#### Example 1 <a name="example1"></a>

Connect to a remote server that is using the Platform MBean Server. This
example also demonstrates using the value of a remote jobs property.


```xml
<oddjob xmlns:jmx='http://rgordon.co.uk/oddjob/jmx'
  id='this'>
  <job>
    <sequential>
      <jobs>
        <jmx:client id='remote' connection='${this.args[0]}'/>
        <echo>${remote/echo.text}</echo>
        <stop job="${remote}"/>
      </jobs>
    </sequential>
  </job>
</oddjob>
```



Note that the [stop](../../../org/oddjob/jobs/job/StopJob.md) is required otherwise Oddjob wouldn't exit. An
Alternative to using stop, would be to make the client a child of a
[sequential](../../../org/oddjob/jobs/structural/SequentialJob.md) with an
[org.oddjob.state.ServiceManagerStateOp](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/state/ServiceManagerStateOp.html) operator.


Here's an example of the command used to launch it:
<pre>
java -jar C:\Users\rob\projects\oddjob\run-oddjob.jar -f C:\Users\rob\projects\oddjob\test\java\org\oddjob\jmx\PlatformMBeanClientExample.xml localhost:13013
</pre>


This configuration is the client side of the first example in
[jmx:server](../../../org/oddjob/jmx/JMXServerJob.md).

#### Example 2 <a name="example2"></a>

To create a connection to a remote server that is using an RMI registry
using the full form of the JMX URL.


```xml
<jmx:client xmlns:jmx="http://rgordon.co.uk/oddjob/jmx" 
            id="freds-pc"
            name="Connection to Freds PC" 
            url="service:jmx:rmi:///jndi/rmi://${hosts.freds-pc}/freds-oddjob-server"/>
```


#### Example 3 <a name="example3"></a>

Connect, run a remote job, and disconnect.


```xml
<oddjob>
  <job>
    <sequential>
      <jobs>
        <jmx:client xmlns:jmx="http://rgordon.co.uk/oddjob/jmx"
          id="freds-pc" name="Oddjob Client"
          url="service:jmx:rmi:///jndi/rmi://${hosts.freds-pc}/freds-oddjob-server" />
        <run job="${freds-pc/server-jobs/greeting}" join="true" />
        <stop job="${freds-pc}" />
      </jobs>
    </sequential>
  </job>
</oddjob>
```



The run job starts the server job but doesn't wait for it to complete.
We would need to add a wait job for that.

#### Example 4 <a name="example4"></a>

Connect using a username and password to a secure server.


```xml
<jmx:client xmlns:jmx="http://rgordon.co.uk/oddjob/jmx"
    connection="localhost/my-oddjob">
    <environment>
        <jmx:client-credentials username="username"
            password="password"/>
    </environment>
</jmx:client>
```


#### Example 5 <a name="example5"></a>

A local job triggers when a server job runs.


```xml
<oddjob>
    <job>
        <sequential>
            <jobs>
                <jmx:client xmlns:jmx="http://rgordon.co.uk/oddjob/jmx"
                    id="freds-pc" name="Oddjob Client"
                    url="service:jmx:rmi:///jndi/rmi://${hosts.freds-pc}/freds-oddjob-server"/>
                <scheduling:trigger on="${freds-pc/server-jobs/greeting}"
                    xmlns:scheduling="http://rgordon.co.uk/oddjob/scheduling">
                    <job>
                        <sequential>
                            <jobs>
                                <echo id="local-job">Server Job Ran!</echo>
                                <stop job="${freds-pc}"/>
                            </jobs>
                        </sequential>
                    </job>
                </scheduling:trigger>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
