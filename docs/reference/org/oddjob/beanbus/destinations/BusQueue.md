[HOME](../../../../README.md)
# bus:queue

A Queue for beans. A work in progress.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [capacity](#propertycapacity) | Capacity of the queue before it blocks. | 
| [name](#propertyname) | The name of this component. | 
| [put](#propertyput) |  | 
| [size](#propertysize) | The size of the queue. | 
| [taken](#propertytaken) | The number of items taken from the queue. | 
| [waitingConsumers](#propertywaitingconsumers) | The number of consumers waiting. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A simple example. |
| [Example 2](#example2) | An example in BeanBus. |


### Property Detail
#### capacity <a name="propertycapacity"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to no limit.</td></tr>
</table>

Capacity of the queue before it blocks.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of this component.

#### put <a name="propertyput"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>WRITE_ONLY</td></tr>
</table>



#### size <a name="propertysize"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The size of the queue.

#### taken <a name="propertytaken"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The number of items taken from the queue.

#### waitingConsumers <a name="propertywaitingconsumers"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The number of consumers waiting.


### Examples
#### Example 1 <a name="example1"></a>

A simple example.


```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <results>
                        <list/>
                    </results>
                </variables>
                <bean class="org.oddjob.beanbus.destinations.BusQueue" id="queue"/>
                <parallel id="parallel">
                    <jobs>
                        <sequential>
                            <jobs>
                                <bean class="org.oddjob.beanbus.drivers.IterableBusDriver" id="producer" name="Producer">
                                    <values>
                                        <list>
                                            <values>
                                                <value value="apple"/>
                                                <value value="orange"/>
                                                <value value="pear"/>
                                            </values>
                                        </list>
                                    </values>
                                    <to>
                                        <value value="${queue}"/>
                                    </to>
                                </bean>
                                <stop job="${queue}" name="Stop Queue"/>
                            </jobs>
                        </sequential>
                        <bean class="org.oddjob.beanbus.drivers.IterableBusDriver" id="consumer" name="Consumer">
                            <values>
                                <value value="${queue}"/>
                            </values>
                            <to>
                                <value value="${vars.results}"/>
                            </to>
                        </bean>
                    </jobs>
                </parallel>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

An example in BeanBus.


```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <parallel id="parallel" join="true">
            <jobs>
                <bus:bus xmlns:bus="oddjob:beanbus">
                    <of>
                        <bus:driver xmlns:bus="oddjob:beanbus">
                            <values>
                                <list>
                                    <values>
                                        <value value="Apple"/>
                                        <value value="Orange"/>
                                        <value value="Banana"/>
                                        <value value="Pear"/>
                                        <value value="Kiwi"/>
                                    </values>
                                </list>
                            </values>
                        </bus:driver>
                        <bus:queue id="queue" xmlns:bus="oddjob:beanbus"/>
                    </of>
                </bus:bus>
                <bus:bus xmlns:bus="oddjob:beanbus">
                    <of>
                        <bus:driver xmlns:bus="oddjob:beanbus">
                            <values>
                                <value value="${queue}"/>
                            </values>
                        </bus:driver>
                        <bus:collect id="results" xmlns:bus="oddjob:beanbus"/>
                    </of>
                </bus:bus>
            </jobs>
        </parallel>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
