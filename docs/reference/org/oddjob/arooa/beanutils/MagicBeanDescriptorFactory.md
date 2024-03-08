[HOME](../../../../README.md)
# arooa:magic-beans

Define Magic Beans. Magic Beans are beans who's
properties can be defined dynamically. Magic beans are useful when
you want to collect information in one bean so it can be kept together.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [definitions](#propertydefinitions) | Definitions for Magic Beans. | 
| [namespace](#propertynamespace) | The namespace for the magic bean element. | 
| [prefix](#propertyprefix) | The element prefix. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Creating a magic bean that define some file information. |


### Property Detail
#### definitions <a name="propertydefinitions"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>

Definitions for Magic Beans. This will be a
list of [org.oddjob.arooa.beanutils.MagicBeanDefinition](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/arooa/beanutils/MagicBeanDefinition.html)s.

#### namespace <a name="propertynamespace"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The namespace for the magic bean element.

#### prefix <a name="propertyprefix"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The element prefix.


### Examples
#### Example 1 <a name="example1"></a>

Creating a magic bean that define some file information.


This is an outer Oddjob configuration file that creates the descriptor
that defines properties for a <code>filespec</code> element.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob id="this">
    <job>
        <oddjob file="${this.dir}/MagicBeansInner.xml">
            <descriptorFactory>
                <arooa:magic-beans xmlns:arooa="http://rgordon.co.uk/oddjob/arooa"
                      namespace="oddjob:magic" prefix="magic">
                    <definitions>
                        <is element="filespec">
                            <properties>
                                <is name="description" type="java.lang.String"/>
                                <is name="file" type="java.io.File" 
                                    configured="ATTRIBUTE"/>
                                <is name="maxSize" type="java.lang.Long"/>
                            </properties>
                        </is>
                    </definitions>
                </arooa:magic-beans>
            </descriptorFactory>
        </oddjob>
    </job>
</oddjob>

```


The nested inner Oddjob configuration uses a list of <code>filespec</code>
magic beans to define information for a For Each job.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
  <job>
    <foreach>
      <values>
        <list>
          <values>
            <magic:filespec description="Big File" file="/files/big"
              maxSize="1000000" xmlns:magic="oddjob:magic" />
            <magic:filespec description="Medium File"
              file="/files/medium" maxSize="20000" xmlns:magic="oddjob:magic" />
            <magic:filespec description="Small File"
              file="/files/small" maxSize="3000" xmlns:magic="oddjob:magic" />
          </values>
        </list>
      </values>
      <configuration>
        <xml>
          <foreach id="each">
            <job>
              <echo>
Checking ${each.current.description} (${each.current.file})
less than ${each.current.maxSize} bytes...</echo>
            </job>
          </foreach>
        </xml>
      </configuration>
    </foreach>
  </job>
</oddjob>

```


The example will display the following when ran:

<pre>
Checking Big File (\files\big)
less than 1000000 bytes...

Checking Medium File (\files\medium)
less than 20000 bytes...

Checking Small File (\files\small)
less than 3000 bytes...
</pre>


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
