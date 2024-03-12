[HOME](../../../../README.md)
# magic-class

Definition for a Magic Bean, which is a bean that
can be defined dynamically.


See also [arooa:magic-beans](../../../../org/oddjob/arooa/beanutils/MagicBeanDescriptorFactory.md).

### Property Summary

| Property | Description |
| -------- | ----------- |
| [classLoader](#propertyclassLoader) | The class loader. | 
| [name](#propertyname) | The name of the class. | 
| [properties](#propertyproperties) | The bean properties. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Using a magic-class to create a magic bean. |


### Property Detail
#### classLoader <a name="propertyclassLoader"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. Set automatically by Oddjob.</td></tr>
</table>

The class loader.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The name of the class.

#### properties <a name="propertyproperties"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The bean properties. A mapping of name to
class name.


### Examples
#### Example 1 <a name="example1"></a>

Using a magic-class to create a magic bean.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential>
            <jobs>
                <variables id="vars">
                    <myClass>
                        <magic-class name="fruit">
                            <properties>
                                <value key="type" value="java.lang.String"/>
                                <value key="quantity" value="java.lang.Integer"/>
                            </properties>
                        </magic-class>
                    </myClass>
                    <myBean>
                        <convert>
                            <value>
                                <bean class="org.oddjob.values.types.MagicBeanType">
                                    <magicClass>
                                        <value value="${vars.myClass}"/>
                                    </magicClass>
                                    <properties>
                                        <value key="type" value="Apple"/>
                                        <value key="quantity" value="24"/>
                                    </properties>
                                </bean>
                            </value>
                        </convert>
                    </myBean>
                </variables>
                <echo id="e"><![CDATA[We have ${vars.myBean.is.quantity} ${vars.myBean.is.type}(s).]]></echo>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
