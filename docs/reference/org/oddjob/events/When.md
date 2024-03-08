[HOME](../../../README.md)
# events:when

Runs a job when triggered by the arrival of an event. The job will be re-run every time
the event arrives. If the job is still running when a new event arrives, the job will attempt to be stopped
and rerun. A typical use case would be processing a file when it arrives, but which may be re-sent with more
up-to-date information.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [beDestination](#propertybeDestination) |  | 
| [eventSource](#propertyeventSource) | The source of events. | 
| [jobs](#propertyjobs) | The child jobs. | 
| [name](#propertyname) | A name, can be any text. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 
| [to](#propertyto) | Provide the event to a Bean Bus style consumer. | 
| [trigger](#propertytrigger) | The trigger event. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Evaluating greengrocer portfolios of fruit when data arrives. |


### Property Detail
#### beDestination <a name="propertybeDestination"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>



#### eventSource <a name="propertyeventSource"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The source of events. If this is not set the first child component is assumed
to be the Event Source.

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

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

Read only view of the internal stop flag.
This flag is cleared with a reset.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Provide the event to a Bean Bus style consumer.

#### trigger <a name="propertytrigger"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The trigger event.


### Examples
#### Example 1 <a name="example1"></a>

Evaluating greengrocer portfolios of fruit when data arrives.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <properties name="Properties">
                    <values>
                        <file file="${some.dir}" key="data.dir"/>
                    </values>
                </properties>
                <bean class="org.oddjob.events.example.FileFactStore" id="factStore" rootDir="${data.dir}"/>
                <events:when id="whenBookList" name="When BookList Available" xmlns:events="oddjob:events">
                    <jobs>
                        <bean class="org.oddjob.events.example.FactSubscriber" factStore="${factStore}" name="Subscribe to BookList" query="BookList:GREENGROCERS"/>
                        <foreach id="forEachBook" name="For Each Book">
                            <values>
                                <value value="${whenBookList.trigger.of.books}"/>
                            </values>
                            <configuration>
                                <inline>
                                    <foreach id="bookName">
                                        <job>
                                            <events:when id="whenBook" name="When ${bookName.current}">
                                                <jobs>
                                                    <bean class="org.oddjob.events.example.FactSubscriber" factStore="${factStore}" name="Subscribe to Book ${bookName.current}" query="Book:${bookName.current}"/>
                                                    <events:when id="priceMatch" name="When Prices for ${bookName.current}">
                                                        <jobs>
                                                            <events:for name="For Each Trade">
                                                                <configuration>
                                                                    <inline>
                                                                        <events id="trade">
                                                                            <job>
                                                                                <bean class="org.oddjob.events.example.FactSubscriber" factStore="${factStore}" name="Subscribe to Price for ${trade.current.product}" query="Price:${trade.current.product}"/>
                                                                            </job>
                                                                        </events>
                                                                    </inline>
                                                                </configuration>
                                                                <values>
                                                                    <value value="${whenBook.trigger.of.trades}"/>
                                                                </values>
                                                            </events:for>
                                                            <sequential name="Run Calculation">
                                                                <jobs>
                                                                    <bean class="org.oddjob.events.example.ValueCalculator" id="calculate">
                                                                        <trades>
                                                                            <value value="${whenBook.trigger.of.trades}"/>
                                                                        </trades>
                                                                        <prices>
                                                                            <value value="${priceMatch.trigger.ofs}"/>
                                                                        </prices>
                                                                    </bean>
                                                                    <echo><![CDATA[Value of ${bookName.current} is ${calculate.value}]]></echo>
                                                                </jobs>
                                                            </sequential>
                                                        </jobs>
                                                    </events:when>
                                                </jobs>
                                            </events:when>
                                        </job>
                                    </foreach>
                                </inline>
                            </configuration>
                        </foreach>
                    </jobs>
                </events:when>
            </jobs>
        </sequential>
    </job>
</oddjob>

```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
