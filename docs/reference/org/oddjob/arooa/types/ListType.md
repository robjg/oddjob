[HOME](../../../../README.md)
# list

A list provides a way of setting properties that are
either [java.util.List](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/List.html) types or arrays. A list can include any other
type including another list or array type.


Handling of multidimensional arrays has not been considered. Such properties
are probably best defined with a custom [org.oddjob.arooa.ArooaValue](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/arooa/ArooaValue.html).

### Property Summary

| Property | Description |
| -------- | ----------- |
| [elementType](#propertyelementType) | The required element type. | 
| [merge](#propertymerge) | If the element is a list or array the values are merged into this list. | 
| [unique](#propertyunique) | Ensures the list contains only unique elements. | 
| [values](#propertyvalues) | Any values. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | A simple list of things. |
| [Example 2](#example2) | A Merged list. |
| [Example 3](#example3) | A Converted list. |
| [Example 4](#example4) | Add to a list the fly. |


### Property Detail
#### elementType <a name="propertyelementType"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Elements will be left being what they want to
 be.</td></tr>
</table>

The required element type. If this is specified
all elements of the array will attempt to be converted to this type.

#### merge <a name="propertymerge"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to not merging.</td></tr>
</table>

If the element is a list or array
the values are merged into this list.

#### unique <a name="propertyunique"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Ensures the list contains only
unique elements.

#### values <a name="propertyvalues"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Any values.


### Examples
#### Example 1 <a name="example1"></a>

A simple list of things. The list contains 3 things two Strings and a
nested list that contains one String.

```xml
<oddjob>
  <job>
    <sequential>
      <jobs>
        <variables id="vars">
          <ourList>
            <list>
              <values>
                <value value="Hello World" />
                <value value="Goodbye World" />
                <list>
                  <values>
                    <value value="I'm in another list" />
                  </values>
                </list>
              </values>
            </list>
          </ourList>
        </variables>
        <repeat id="each">
          <values>
            <value value="${vars.ourList}" />
          </values>
          <job>
            <echo>${each.current}</echo>
          </job>
        </repeat>
      </jobs>
    </sequential>
  </job>
</oddjob>
```


The output is:

```
Hello World
Goodbye World
[I'm in another list]
```


#### Example 2 <a name="example2"></a>

A Merged list. This list merges a plain value, a sub list
and array into a list of 5 separate values.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <aList>
                        <list merge="true">
                            <values>
                                <value value="apples"/>
                                <list>
                                    <values>
                                        <value value="oranges"/>
                                        <value value="bananas"/>
                                    </values>
                                </list>
                                <tokenizer text="kiwis, mangos"/>
                            </values>
                        </list>
                    </aList>
                </variables>
                <foreach>
                    <values>
                        <value value="${vars.aList}"/>
                    </values>
                    <configuration>
                        <xml>
                            <foreach id="loop">
                                <job>
                                    <echo>${loop.current}</echo>
                                </job>
                            </foreach>
                        </xml>
                    </configuration>
                </foreach>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


The output is:

```
apples
oranges
bananas
kiwis
mangos
```


#### Example 3 <a name="example3"></a>

A Converted list. The elements of the list are converted to an array of
Strings.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <aList>
                        <list>
                            <elementType>
                                <class name="[Ljava.lang.String;"/>
                            </elementType>
                            <values>
                                <value
                                    value='"grapes, red", "grapes, white", gratefruit'/>
                                <list>
                                  <values>
                                    <value value="apples"/>
                                    <value value="pears"/>
                                  </values>
                                </list>
                            </values>
                        </list>
                    </aList>
                </variables>
                <foreach>
                    <values>
                        <value value="${vars.aList}"/>
                    </values>
                    <configuration>
                        <xml>
                            <foreach id="loop">
                                <job>
                                    <foreach>
                                        <values>
                                            <value value="${loop.current}"/>
                                        </values>
                                        <configuration>
                                            <xml>
                                                <foreach id="inner">
                                                    <job>
                                                        <echo>${inner.current}</echo>
                                                    </job>
                                                </foreach>
                                            </xml>
                                        </configuration>
                                    </foreach>
                                </job>
                            </foreach>
                        </xml>
                    </configuration>
                </foreach>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


The output is:

```
grapes, red
grapes, white
gratefruit
apples
pears
```


Although it can't be seen in the output, but can be seen when this
example is run in Oddjob Explorer, the list contains to String array
elements.

#### Example 4 <a name="example4"></a>

Add to a list the fly. This example demonstrates setting the
hidden 'add' property. The property is hidden so that it can't be set
via configuration which could be confusing. A side effect of this is that
it is also hidden from the Reference Guide generator.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <aList>
                        <list/>
                    </aList>
                </variables>
                <set>
                    <values>
                        <value key="vars.aList.add" value="apples"/>
                    </values>
                </set>
                <set>
                    <values>
                        <value key="vars.aList.add" value="bananas"/>
                    </values>
                </set>
                <repeat id="each">
                    <values>
                        <value value="${vars.aList}"/>
                    </values>
                    <job>
                        <echo><![CDATA[${each.current}]]></echo>
                    </job>
                </repeat>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


The output is:

```
apples
bananas
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
