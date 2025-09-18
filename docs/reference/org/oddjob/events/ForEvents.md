[HOME](../../../README.md)
# events:for

An Event Source For a variable set of child
Event Sources. Required when the list of events to wait for changes dynamically - such as the set of files
required to run a job.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [configuration](#propertyconfiguration) | The configuration that will be parsed for each value. | 
| [eventOperator](#propertyeventOperator) | Event Operator to filter events. | 
| [file](#propertyfile) | The name of the configuration file. | 
| [index](#propertyindex) | The current index in the values. | 
| [last](#propertylast) | The last event to be passed to a consumer. | 
| [name](#propertyname) | A name, can be any text. | 
| [purgeAfter](#propertypurgeAfter) | The number of completed jobs to keep. | 
| [to](#propertyto) | The destination events will be sent to. | 
| [values](#propertyvalues) | Any stream of values. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Wait for prices to be available to price some fruit trades. |


### Property Detail
#### configuration <a name="propertyconfiguration"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The configuration that will be parsed
for each value.

#### eventOperator <a name="propertyeventOperator"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, default to ALL.</td></tr>
</table>

Event Operator to filter events. ANY/ALL.

#### file <a name="propertyfile"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of the configuration file.
to use for configuration.

#### index <a name="propertyindex"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>R/O.</td></tr>
</table>

The current index in the
values.

#### last <a name="propertylast"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The last event to be passed to a consumer.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A name, can be any text.

#### purgeAfter <a name="propertypurgeAfter"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Defaults to no complete jobs being purged.</td></tr>
</table>

The number of completed jobs to keep. Oddjob configurations
can be quite memory intensive, mainly due to logging, purging complete jobs
will stop too much memory being taken.


Setting this property to 0
means that no complete jobs will be purged.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Maybe. Set automatically by some parent components.</td></tr>
</table>

The destination events will be sent to.

#### values <a name="propertyvalues"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Any stream of values.


### Examples
#### Example 1 <a name="example1"></a>

Wait for prices to be available to price some fruit trades. This resulted as an experiment in turning Oddjob
into a rules engine.

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



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
