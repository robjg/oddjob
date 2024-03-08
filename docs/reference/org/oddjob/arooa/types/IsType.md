[HOME](../../../../README.md)
# is

Create an Object that is the class of the
property. The properties class must have a public zero argument
constructor.

### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Using <code>is</code>to set a simple property. |
| [Example 2](#example2) | Using <code>is</code>to set an indexed property. |
| [Example 3](#example3) | Using <code>is</code>to set a mapped property. |


### Examples
#### Example 1 <a name="example1"></a>

Using <code>is</code> to set a simple property.

```xml
<snack>
    <fruit>
        <is type='apple' colour='red'/>
    </fruit>
</snack>
        

```


Where the <code>snack</code> bean is:

{@oddjob.java.resource org/oddjob/arooa/types/IsTypeTest.java#simpleBean}

and the <code>fruit</code> bean is:

{@oddjob.java.resource org/oddjob/arooa/types/IsTypeTest.java#fruitBean}

#### Example 2 <a name="example2"></a>

Using <code>is</code> to set an indexed property.

```xml
<snack>
    <fruit>
        <is type='apple' colour='red'/>
        <is type='pear' colour='green'/>
    </fruit>
</snack>
        

```


Where the <code>snack</code> bean is:

{@oddjob.java.resource org/oddjob/arooa/types/IsTypeTest.java#indexedBean}

and the <code>fruit</code> bean is as above.

#### Example 3 <a name="example3"></a>

Using <code>is</code> to set a mapped property.

```xml
<snack>
    <fruit>
        <is key='morning' type='apple' colour='red'/>
        <is key='afternoon' type='grapes' colour='white'/>
    </fruit>
</snack>
        

```


Where the <code>snack</code> bean is:

{@oddjob.java.resource org/oddjob/arooa/types/IsTypeTest.java#mappedBean}

and the <code>fruit</code> bean is as above.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
