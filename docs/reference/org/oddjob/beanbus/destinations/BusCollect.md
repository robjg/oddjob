[HOME](../../../../README.md)
# bus:collect

A component that collects what it consumes. By default, results are collected into
a container that provides indexed access and conversions that allows it to be used as a list by other components.
Alternatively a `keyMapper` or a `valueMapper` function may be provided that creates
a container with map like access to the incoming data.
These containers are available with the `list` and `map` properties respectively.
If the `output` property is set the results of the captured objects are written to the provided
output as text lines.


This component acn be the final destination of a [bus:bus](../../../../org/oddjob/beanbus/bus/BasicBusService.md) or
it may be used in the middle of other components so is can act as a Wire Tap.



### Property Summary

| Property | Description |
| -------- | ----------- |
| [beans](#propertybeans) | Deprecated, use list instead. | 
| [count](#propertycount) | Count of items collected. | 
| [keyMapper](#propertykeymapper) | A function that will extract a key from an item. | 
| [list](#propertylist) | The collected items as list container. | 
| [map](#propertymap) | The collected items as a map container. | 
| [name](#propertyname) | The name of this component. | 
| [output](#propertyoutput) | An output stream that items will be written to as strings. | 
| [to](#propertyto) | The next component in a bus. | 
| [valueMapper](#propertyvaluemapper) | A function that will extract a value from an item to put in the map. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Collecting values into a list. |
| [Example 2](#example2) | Collecting values into a map using a Key Mapper. |
| [Example 3](#example3) | Collecting values into a map using a Value Mapper. |
| [Example 4](#example4) | Collecting values into a map using a Key and Value Mapper. |
| [Example 5](#example5) | Collecting values into an Output Stream. |
| [Example 6](#example6) | There are many examples elsewhere. |


### Property Detail
#### beans <a name="propertybeans"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

Deprecated, use list instead.

#### count <a name="propertycount"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>R/O.</td></tr>
</table>

Count of items collected.

#### keyMapper <a name="propertykeymapper"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A function that will extract a key from an item. If this property is set, items will
be available as a map, not a list.

#### list <a name="propertylist"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The collected items as list container. Conversions exist so that this list
property can be used as a list or the values can be accessed using an indexed accessor on the value property.

#### map <a name="propertymap"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
      <tr><td><i>Required</i></td><td>Read only.</td></tr>
</table>

The collected items as a map container. Conversions exist so that this map
property can be used as a map or the values can be accessed using a mapped accessor on the value property.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of this component.

#### output <a name="propertyoutput"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An output stream that items will be written to as strings.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The next component in a bus. Set automatically in a
[bus:bus](../../../../org/oddjob/beanbus/bus/BasicBusService.md).

#### valueMapper <a name="propertyvaluemapper"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A function that will extract a value from an item to put in the map.


### Examples
#### Example 1 <a name="example1"></a>

Collecting values into a list. The `echo` job shows how to access the list by element,
display its size, and its contents. The script checks the result for us. The list property is converted
into a Java List as it is bound to the script.
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <bus:bus xmlns:bus="oddjob:beanbus">
                    <of>
                        <bus:driver>
                            <values>
                                <list>
                                    <values>
                                        <value value="Apple"/>
                                        <value value="Orange"/>
                                        <value value="Pear"/>
                                    </values>
                                </list>
                            </values>
                        </bus:driver>
                        <bus:collect id="collect"/>
                    </of>
                </bus:bus>
                <echo><![CDATA[Index 2: ${collect.list.value[2]}
Size: ${collect.list.size}
As Text: ${collect.list}]]></echo>
                <script name="Check List" resultForState="true" resultVariable="retval">
                    <bind>
                        <value key="actual" value="${collect.list}"/>
                    </bind><![CDATA[var expected = Java.type("java.util.List").of("Apple", "Orange", "Pear");
var retval = 0;
if (!expected.equals(actual)){
 print("Excpeted: " + expected + ", but got: " + actual);
 retval = 1;
}
retval;]]></script>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Collecting values into a map using a Key Mapper. The mapping function uses a JavaScript
expression to use the first letter of the data as the key. The `echo` job shows how to access the
map by element, display its size, and its contents. The script checks the result for us. The map property is converted
into a Java Map as it is bound to the script.
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <bus:bus xmlns:bus="oddjob:beanbus">
                    <of>
                        <bus:driver>
                            <values>
                                <list>
                                    <values>
                                        <value value="Apple"/>
                                        <value value="Orange"/>
                                        <value value="Pear"/>
                                    </values>
                                </list>
                            </values>
                        </bus:driver>
                        <bus:collect id="collect">
                            <keyMapper>
                                <value value="#{function(x) { return x.charAt(0) }}"/>
                            </keyMapper>
                        </bus:collect>
                    </of>
                </bus:bus>
                <echo><![CDATA[Element 'O': ${collect.map.value(O)}
Size: ${collect.map.size}
As Text: ${collect.map}]]></echo>
                <script name="Check List" resultForState="true" resultVariable="retval">
                    <bind>
                        <value key="actual" value="${collect.map}"/>
                    </bind><![CDATA[var expected = Java.type("java.util.Map").of("A", "Apple", "O", "Orange", "P", "Pear");
var retval = 0;
if (!expected.equals(actual)){
 print("Excpeted: " + expected + ", but got: " + actual);
 retval = 1;
}
retval;]]></script>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 3 <a name="example3"></a>

Collecting values into a map using a Value Mapper. The mapping function uses a JavaScript
expression to calculate the square of the numbers passed by the Bus Driver. The `echo` job shows how we
can't access the map by element because Oddjob expression treat a mapped property key as a string. We show
a roundabout way that access can be done using a JavaScript expression.
As above, the script checks the result for us.
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <bus:bus xmlns:bus="oddjob:beanbus">
                    <of>
                        <bus:driver>
                            <values>
                                <list>
                                    <values>
                                        <value value="#{1}"/>
                                        <value value="#{2}"/>
                                        <value value="#{3}"/>
                                    </values>
                                </list>
                            </values>
                        </bus:driver>
                        <bus:collect id="collect">
                            <valueMapper>
                                <value value="#{function(x) { return x * x }}"/>
                            </valueMapper>
                        </bus:collect>
                    </of>
                </bus:bus>
                <echo><![CDATA[Element '2': ${collect.map.value(2)}
Element 2: #{collect.get('map').getValue(2)}
Size: ${collect.map.size}
As Text: ${collect.map}]]></echo>
                <script name="Check List" resultForState="true" resultVariable="retval">
                    <bind>
                        <value key="actual" value="${collect.map}"/>
                    </bind><![CDATA[var expected = Java.type("java.util.Map").of(1, 1.0, 2, 4.0, 3, 9.0);
var retval = 0;
if (!expected.equals(actual)){
 print("Excpeted: " + expected + ", but got: " + actual);
 retval = 1;
}
retval;]]></script>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 4 <a name="example4"></a>

Collecting values into a map using a Key and Value Mapper. As above except that the
key is the number as a String, so it is accessible as an Oddjob mapped property.
As above, the script checks the result for us.
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <bus:bus xmlns:bus="oddjob:beanbus">
                    <of>
                        <bus:driver>
                            <values>
                                <list>
                                    <values>
                                        <value value="#{1}"/>
                                        <value value="#{2}"/>
                                        <value value="#{3}"/>
                                    </values>
                                </list>
                            </values>
                        </bus:driver>
                        <bus:collect id="collect">
                            <keyMapper>
                                <value value="#{function(x) { return x.toString() }}"/>
                            </keyMapper>
                            <valueMapper>
                                <value value="#{function(x) { return x * x }}"/>
                            </valueMapper>
                        </bus:collect>
                    </of>
                </bus:bus>
                <echo><![CDATA[Element '2': ${collect.map.value(2)}
Element 2: #{collect.get('map').getValue(2)}
Size: ${collect.map.size}
As Text: ${collect.map}]]></echo>
                <script name="Check List" resultForState="true" resultVariable="retval">
                    <bind>
                        <value key="actual" value="${collect.map}"/>
                    </bind><![CDATA[var expected = Java.type("java.util.Map").of("1", 1.0, "2", 4.0, "3", 9.0);
var retval = 0;
if (!expected.equals(actual)){
 print("Excpeted: " + expected + ", but got: " + actual);
 retval = 1;
}
retval;]]></script>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 5 <a name="example5"></a>

Collecting values into an Output Stream. Here we use a buffer. The buffer is declared as a
variable to we can access its properties to display the text it contains and validate that text
as lines using a script.
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <text>
                        <buffer/>
                    </text>
                </variables>
                <bus:bus xmlns:bus="oddjob:beanbus">
                    <of>
                        <bus:driver>
                            <values>
                                <list>
                                    <values>
                                        <value value="Apple"/>
                                        <value value="Orange"/>
                                        <value value="Pear"/>
                                    </values>
                                </list>
                            </values>
                        </bus:driver>
                        <bus:collect id="collect">
                            <output>
                                <value value="${vars.text}"/>
                            </output>
                        </bus:collect>
                    </of>
                </bus:bus>
                <echo><![CDATA[${vars.text.text}]]></echo>
                <script name="Check Result" resultForState="true" resultVariable="retval">
                    <bind>
                        <value key="actual" value="${vars.text.lines}"/>
                    </bind><![CDATA[var expected = ["Apple", "Orange", "Pear"];
var actualJs = Java.from(actual)
var retval = 0;
for (i = 0; i < expected.length; ++i) {
  if (expected[i] != actual[i]) {
    retval = 1;
  }
}
if (retval != 0){
  print("Excpeted: " + expected + ", but got: " + actualJs);
}
retval;]]></script>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


#### Example 6 <a name="example6"></a>

There are many examples elsewhere.

- [org.oddjob.beanbus.destinations.Batcher](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/beanbus/destinations/Batcher.html)
- [org.oddjob.beanbus.destinations.BeanCopy](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/beanbus/destinations/BeanCopy.html)
- [bus:queue](../../../../org/oddjob/beanbus/destinations/BusQueue.md)
- [bus:limit](../../../../org/oddjob/beanbus/destinations/BusLimit.md)



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
