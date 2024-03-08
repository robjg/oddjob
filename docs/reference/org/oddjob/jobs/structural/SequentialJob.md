[HOME](../../../../README.md)
# sequential

Executes it's children in a sequence one after the
other. The sequence will only continue to be executed if each child
COMPLETEs. If a child is INCOMPLETE, or throws an EXCEPTION then execution
will terminate and this job's state will reflect that of the
failed child.


This behaviour can be changed by setting the <b><code>independent</code></b>
property which will cause execution to continue regardless of the last
executed child state.

<h4>State Operator</h4>

The <b><code>stateOperator</b></code> property changes the way in which
this jobs state reflects its child states. Oddjob currently supports the
following State Operators:
<dl>
<dt>ACTIVE</dt>
<dd>If any child is EXECUTING, ACTIVE or STARTING this job's state
will be ACTIVE. Otherwise, if a child is STARTED, this job's state
will be STARTED. Otherwise, if a child is READY, this job's state will
be READY. Otherwise, this job's state will reflect the worst state of
the child jobs.</dd>
<dt>WORST</dt>
<dd>This job's state will be EXCEPTION or INCOMPLETE if any of the
child job's are in this state. Otherwise the rules for ACTIVE apply.</dd>
<dt>SERVICES</dt>
<dd>This state operator is designed for starting services. This job
will COMPLETE when all services are STARTED. If any
services fails to start this job reflects the EXCEPTION state.
Because this job, when using this state operator, completes even though
it's children are running, this job is analogous to creating daemon
threads in that the services will not stop Oddjob from shutting down
once all other jobs have completed.</dd>
</dl>

<h4>Stopping</h4>
As with other structural jobs, when this job is stopping, either because
of a manual stop, or during Oddjob's shutdown cycle, the child jobs and
services will still be stopped in an reverse order.

<h4>Persistence</h4>
If this job has an Id and Oddjob is running with a Persister, then
this job's state will be persisted when it changes. Thus a COMPLETE
state will be persisted once all child jobs have completed. If Oddjob
is restarted at this point the COMPLETE state of this job will stop
the child job's from re-running, if though they themselves might not
have been persisted. To stop this job from being persisted set the
<code>transient</code> property to true. Not that when starting
services with this job, persistence is probably not desirable as
it will stop the services from re-starting.

<h4>Re-running Child Jobs</h4>

If the failed job is later run manually and completes this Job will
reflect the new state. As such it is useful as a trigger point for
the completion of a sequence of jobs.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [independent](#propertyindependent) | Whether the child jobs are independent or not. | 
| [jobs](#propertyjobs) | The child jobs. | 
| [name](#propertyname) | A name, can be any text. | 
| [stateOperator](#propertystateOperator) | Set the way the children's state is evaluated and reflected by the parent. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 
| [transient](#propertytransient) | Is this job transient. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A simple sequence of two jobs. |
| [Example 2](#example2) | Starting two services. |


### Property Detail
#### independent <a name="propertyindependent"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Default is dependent child jobs.</td></tr>
</table>

Whether the child jobs are independent or not.

#### jobs <a name="propertyjobs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No, but pointless if missing.</td></tr>
</table>

The child jobs.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### stateOperator <a name="propertystateOperator"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, default is WORST.</td></tr>
</table>

Set the way the children's state is
evaluated and reflected by the parent. Values can be WORST,
ACTIVE, or SERVICES.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Read only view of the internal stop flag.
This flag is cleared with a reset.

#### transient <a name="propertytransient"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, default is false.</td></tr>
</table>

Is this job transient. If true state will not
be persisted.


### Examples
#### Example 1 <a name="example1"></a>

A simple sequence of two jobs.

```xml
<oddjob>
    <job>
        <sequential name="A sequence of two jobs">
            <jobs>
                <echo>This runs first.</echo>
                <echo>This runs after.</echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Starting two services. To perform odd jobs, in a workshop for instance,
this first 'job' is to turn on the lights and turn on any machines
required. The service manager encompasses this idea - and this example
embelishes the idea. Real odd jobs for Oddjob will involve activities
such as starting services such as a data source or a server connection.
The concept however is still the same.

```xml
<oddjob>
  <job>
    <sequential>
      <jobs>
      <sequential id="service-manager" stateOperator="SERVICES">
        <jobs>
          <bean id="lights" 
            class="org.oddjob.jobs.structural.ServiceManagerTest$Lights"/>
          <bean id='machine'
            class="org.oddjob.jobs.structural.ServiceManagerTest$MachineThatGoes"
            goes="ping"/>
        </jobs>
      </sequential>
      <echo>The lights are ${lights.are} and the machine goes ${machine.goes}.</echo>
      </jobs>
    </sequential>
  </job>
</oddjob>
```


The services are started in order. Once both services have started
a job is performed that requires both services. If this configuration
were running from the command line, Oddjob would stop the services
as it shut down. First the machine would be turned of and then finally
the lights would be turned out.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
