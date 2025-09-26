[HOME](../../../README.md)
# state:if

This job implements an if/then/else logic based on job state. This job can
contain any number of child jobs. The first provides the state for
the condition.
If this state matches the given state, the second job is
executed. If it doesn't, then the third job is executed, (if it exists).


The completion state is that of the then or else job. If either don't
exist then the Job is flagged as complete.


If any more than three jobs are provided the extra jobs are ignored.


If the first job enters an ACTIVE state then condition will not be
evaluated until the first job leaves the ACTIVE state. This job will
not block while this is happening. The thread of execution will pass
to its next sibling and this job will also enter the ACTIVE state.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [executorService](#propertyexecutorservice) | Used for an asynchronous evaluation of the if. | 
| [jobs](#propertyjobs) | The child jobs. | 
| [name](#propertyname) | A name, can be any text. | 
| [state](#propertystate) | The state condition to check against. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | If a file exists. |
| [Example 2](#example2) | An example showing lots of if's. |
| [Example 3](#example3) | Asynchronous evaluation. |


### Property Detail
#### executorService <a name="propertyexecutorservice"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Will be provided by the framework.</td></tr>
</table>

Used for an asynchronous evaluation of the if.

#### jobs <a name="propertyjobs"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>At least one.</td></tr>
</table>

The child jobs.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### state <a name="propertystate"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to COMPLETE.</td></tr>
</table>

The state condition to check against.
See the Oddjob User guide for a full list of state conditions.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Read only view of the internal stop flag.
This flag is cleared with a reset.


### Examples
#### Example 1 <a name="example1"></a>

If a file exists.


```xml
<oddjob xmlns:state="http://rgordon.co.uk/oddjob/state"
        id="this">
    <job>
        <state:if>
            <jobs>
                <exists name="Check File Exists"
                        file="${this.dir}/data/some.txt"/>
                <echo id="then"
                      name="Echo to Console">File Exists</echo>
                <echo id="else"
                      name="Echo to Console">File Doesn't Exist</echo>
            </jobs>
        </state:if>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

An example showing lots of if's. All these if's go to COMPLETE state
when run.


```xml
<oddjob>
    <job>
        <sequential xmlns:state="http://rgordon.co.uk/oddjob/state">
            <jobs>
                <state:if>
                    <jobs>
                        <echo>Hello</echo>
                        <echo>Good Bye</echo>
                    </jobs>
                </state:if>
                <state:if>
                    <jobs>
                        <state:flag name="Exception" state="EXCEPTION"/>
                        <state:flag name="Unexpected 1" state="EXCEPTION"/>
                        <echo>No Hello</echo>
                    </jobs>
                </state:if>
                <state:if>
                    <jobs>
                        <echo>Only Hello</echo>
                    </jobs>
                </state:if>
                <state:if state="!COMPLETE">
                    <jobs>
                        <state:flag name="Exception" state="EXCEPTION"/>
                        <echo>No Hello</echo>
                    </jobs>
                </state:if>
                <state:if state="!COMPLETE">
                    <jobs>
                        <echo>Hello</echo>
                        <state:flag name="Unexpected 2" state="EXCEPTION"/>
                    </jobs>
                </state:if>
                <state:if state="!EXCEPTION">
                    <jobs>
                        <echo>Hello</echo>
                        <echo>Good Bye</echo>
                        <state:flag name="Unexpected 3" state="EXCEPTION"/>
                    </jobs>
                </state:if>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 3 <a name="example3"></a>

Asynchronous evaluation. Only when the first job moves beyond it's ACTIVE
state will the condition be evaluated and the then job (second job)
be executed. The execution of the second job is also asynchronous.


```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <state:if id="if-job" xmlns:state="http://rgordon.co.uk/oddjob/state">
            <jobs>
                <parallel>
                    <jobs>
                        <state:flag/>
                    </jobs>
                </parallel>
                <echo id="then-job"><![CDATA[That Worked!]]></echo>
                <echo id="else-job"><![CDATA[This should never be shown.]]></echo>
            </jobs>
        </state:if>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
