[HOME](../../../../README.md)
# bean

Create an Object of the given class. The class
is specified with the class attribute. If no class is specified a
java.lang.Object is created.


The class must be a true Java Bean, and
have a no argument public constructor.


Properties of the bean are
attributes for the eight Java primitive types and their associated Objects,
or a String, and elements for all other types, as is the Oddjob standard.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [class](#propertyclass) | The class to create. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Creating a bean. |


### Property Detail
#### class <a name="propertyclass"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to java.lang.Object.</td></tr>
</table>

The class to create. Must have a public zero
argument constructor. Note that this attribute value must be
constant - it can not contain ${} property place holders.


### Examples
#### Example 1 <a name="example1"></a>

Creating a bean.

```xml
<bean class="org.oddjob.arooa.types.PersonBean" name="John">
    <friends>
        <list>
            <values>
                <value value="Rod"/>
                <value value="Jane"/>
                <value value="Freddy"/>
            </values>
        </list>
    </friends>
</bean>
```


Where the bean is:

```java
package org.oddjob.arooa.types;

public class PersonBean {

	private String name;
	
	private String[] friends;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getFriends() {
		return friends;
	}

	public void setFriends(String[] friends) {
		this.friends = friends;
	}
}
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
