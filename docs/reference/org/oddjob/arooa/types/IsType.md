[HOME](../../../../README.md)
# is

Create an Object that is the class of the
property. The properties class must have a public zero argument
constructor.

### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Using <code>is</code> to set a simple property. |
| [Example 2](#example2) | Using <code>is</code> to set an indexed property. |
| [Example 3](#example3) | Using <code>is</code> to set a mapped property. |


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

```java
	public static class SnackBean {
		FruitBean fruit;
		
		public void setFruit(FruitBean bean) {
			this.fruit = bean;
		}
	}
```


and the <code>fruit</code> bean is:

```java
	public static class FruitBean {
		
		private String type;
		private String colour;
				
		public void setType(String type) {
			this.type = type;
		}
		
		public void setColour(String colour) {
			this.colour = colour;
		}
	}
```


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

```java
	public static class IndexedSnack {
		
		private List<FruitBean> fruit = 
			new ArrayList<FruitBean>();
		
		public void setFruit(int index, FruitBean bean) {
			if (bean == null) {
				fruit.remove(index);
			}
			else {
				fruit.add(index, bean);
			}
		}
	}
```


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

```java
	public static class MappedSnack {

		private Map<String, FruitBean> fruit =
			new HashMap<String, FruitBean>();
		
		public void setFruit(String key, FruitBean bean) {
			if (fruit == null) {
				fruit.remove(key);
			}
			else {
				this.fruit.put(key, bean);
			}
		}
	}
```


and the <code>fruit</code> bean is as above.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
