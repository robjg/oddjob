[HOME](../../../README.md)
# stderr

Provide an output to the stderr stream of
the console.

### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Copy a buffer to stderr. |


### Examples
#### Example 1 <a name="example1"></a>

Copy a buffer to stderr.

```xml
<copy>
    <input>
        <buffer><![CDATA[It's all going wrong!
]]>
        </buffer>
    </input>
    <output>
        <stderr/>
    </output>
</copy>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
