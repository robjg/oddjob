[HOME](../../../README.md)
# events:when

Runs a job when triggered by the arrival of an event. The job will be re-run every time
the event arrives. If the job is still running when a new event arrives, the job will attempt to be stopped
and rerun. A typical use case would be processing a file when it arrives, but which may be re-sent with more
up-to-date information.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [beDestination](#propertybedestination) | This is to be a destination. | 
| [eventSource](#propertyeventsource) | The source of events. | 
| [haltOn](#propertyhalton) | The State Condition of the child job on which to halt event subscription. | 
| [jobs](#propertyjobs) | The child jobs. | 
| [name](#propertyname) | A name, can be any text. | 
| [stop](#propertystop) | Read only view of the internal stop flag. | 
| [to](#propertyto) | Provide the event to a Bean Bus style consumer. | 
| [trigger](#propertytrigger) | The trigger event. | 
| [triggerStrategy](#propertytriggerstrategy) | How to handle triggers before the child job has completed. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Evaluating greengrocer portfolios of fruit when data arrives. |
| [Example 2](#example2) | Being a destination in a pipeline. |


### Property Detail
#### beDestination <a name="propertybedestination"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

This is to be a destination. A destination is a component in a
[bus:bus](../../../org/oddjob/beanbus/bus/BasicBusService.md) pipeline.

#### eventSource <a name="propertyeventsource"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The source of events. If this is not set the first child component is assumed
to be the Event Source, unless [org.oddjob.events.EventJobBase](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/events/EventJobBase.html) is set.

#### haltOn <a name="propertyhalton"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to FAILURE, i.e. an EXCEPTION or INCOMPLETE state.</td></tr>
</table>

The State Condition of the child job on which to halt event subscription.

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

#### triggerStrategy <a name="propertytriggerstrategy"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to STOP_AND_RERUN.</td></tr>
</table>

How to handle triggers before the child job has completed. Built in options
are currently STOP_AND_RERUN and QUEUE.


### Examples
#### Example 1 <a name="example1"></a>

Evaluating greengrocer portfolios of fruit when data arrives.


```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="oddjob">
    <job>
        <sequential>
            <jobs>
                <properties name="Properties">
                    <values>
                        <file file="${oddjob.dir}/data" key="data.dir"/>
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


#### Example 2 <a name="example2"></a>

Being a destination in a pipeline.


```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <bus:bus xmlns:bus="oddjob:beanbus">
                    <of>
                        <bean class="org.oddjob.events.WhenTest$PretendFileWatcher" id="fileWatcher"/>
                        <bus:filter id="filter">
                            <predicate>
                                <bean class="org.oddjob.events.WhenTest$OnlyTxtFiles"/>
                            </predicate>
                        </bus:filter>
                        <events:when beDestination="true" id="when" xmlns:events="oddjob:events">
                            <jobs>
                                <echo id="result">
                                    <![CDATA[Result: ${when.trigger}]]>
                                </echo>
                            </jobs>
                        </events:when>
                    </of>
                </bus:bus>
                <folder>
                    <jobs>
                        <set id="set1" name="Set File Name 1">
                            <values>
                                <value key="fileWatcher.someFileName" value="Fruit.txt"/>
                            </values>
                        </set>
                        <set id="set2" name="Set File Name 2">
                            <values>
                                <value key="fileWatcher.someFileName" value="Names.doc"/>
                            </values>
                        </set>
                        <set id="set3" name="Set File Name 3">
                            <values>
                                <value key="fileWatcher.someFileName" value="Prices.txt"/>
                            </values>
                        </set>
                    </jobs>
                </folder>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
