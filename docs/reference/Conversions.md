# Conversions

Conversion documentation is a Work in Progress.

| From | To | Description |
| -------- | ----------- | ----------- |
| java.io.File | java.io.File[] |  | 
| java.io.File | java.io.InputStream |  | 
| java.io.File | java.io.OutputStream |  | 
| java.io.File | java.lang.String |  | 
| java.io.File | java.net.URL |  | 
| java.io.File | java.nio.file.Path |  | 
| java.io.File[] | java.lang.String |  | 
| java.lang.Boolean | java.lang.Number |  | 
| java.lang.Boolean | java.lang.String |  | 
| java.lang.Byte | java.lang.String |  | 
| java.lang.Character | java.lang.Number |  | 
| java.lang.Character | java.lang.String |  | 
| java.lang.Double | java.lang.String |  | 
| java.lang.Double | java.math.BigDecimal |  | 
| java.lang.Enum | *Various* |  | 
| java.lang.Float | java.lang.String |  | 
| java.lang.Integer | java.lang.String |  | 
| java.lang.Integer | java.math.BigDecimal |  | 
| java.lang.Iterable | java.util.stream.Stream |  | 
| java.lang.Long | java.lang.String |  | 
| java.lang.Long | java.math.BigDecimal |  | 
| java.lang.Long | java.time.Instant |  | 
| java.lang.Long | java.util.Date |  | 
| java.lang.Number | java.lang.Boolean | 0 is false, anything else is true. | 
| java.lang.Number | java.lang.Byte |  | 
| java.lang.Number | java.lang.Character |  | 
| java.lang.Number | java.lang.Double |  | 
| java.lang.Number | java.lang.Float |  | 
| java.lang.Number | java.lang.Integer |  | 
| java.lang.Number | java.lang.Long |  | 
| java.lang.Number | java.lang.Short |  | 
| java.lang.Object | *Various* |  | 
| java.lang.Object | java.lang.String |  | 
| java.lang.Object | org.oddjob.arooa.ArooaValue |  | 
| java.lang.Object | org.oddjob.script.Invoker |  | 
| java.lang.Object[] | java.util.List |  | 
| java.lang.Short | java.lang.String |  | 
| java.lang.String | *Various* |  | 
| java.lang.String | byte[] |  | 
| java.lang.String | char[] |  | 
| java.lang.String | java.io.File |  | 
| java.lang.String | java.io.File[] |  | 
| java.lang.String | java.io.InputStream |  | 
| java.lang.String | java.lang.Boolean |  | 
| java.lang.String | java.lang.Byte |  | 
| java.lang.String | java.lang.Character |  | 
| java.lang.String | java.lang.Double |  | 
| java.lang.String | java.lang.Float |  | 
| java.lang.String | java.lang.Integer |  | 
| java.lang.String | java.lang.Long |  | 
| java.lang.String | java.lang.Short |  | 
| java.lang.String | java.lang.String[] |  | 
| java.lang.String | java.math.BigDecimal |  | 
| java.lang.String | java.net.URI |  | 
| java.lang.String | java.net.URL |  | 
| java.lang.String | java.time.Instant |  | 
| java.lang.String | java.util.Date |  | 
| java.lang.String | org.oddjob.arooa.convert.ConversionProviderFactory |  | 
| java.lang.String | org.oddjob.events.EventOperator |  | 
| java.lang.String | org.oddjob.events.When$TriggerStrategy |  | 
| java.lang.String | org.oddjob.jobs.job.ResetAction |  | 
| java.lang.String | org.oddjob.schedules.units.DayOfMonth |  | 
| java.lang.String | org.oddjob.schedules.units.DayOfWeek |  | 
| java.lang.String | org.oddjob.schedules.units.Month |  | 
| java.lang.String | org.oddjob.schedules.units.WeekOfMonth |  | 
| java.lang.String | org.oddjob.state.StateCondition |  | 
| java.lang.String | org.oddjob.state.StateOperator |  | 
| java.math.BigDecimal | java.lang.String |  | 
| java.net.URL | java.io.InputStream |  | 
| java.net.URL | java.lang.String |  | 
| java.net.URL | java.net.URI |  | 
| java.nio.file.Path | java.io.File |  | 
| java.time.Instant | java.lang.Long |  | 
| java.time.Instant | java.lang.String |  | 
| java.time.Instant | java.util.Date |  | 
| java.util.Collection | java.lang.Object[] |  | 
| java.util.Date | java.lang.Long |  | 
| java.util.Date | java.lang.String |  | 
| java.util.Date | java.sql.Date |  | 
| java.util.Date | java.sql.Time |  | 
| java.util.Date | java.sql.Timestamp |  | 
| java.util.Date | java.time.Instant |  | 
| java.util.Map | java.util.Collection |  | 
| java.util.function.Function | java.util.function.Consumer |  | 
| java.util.function.Function | java.util.function.Predicate |  | 
| java.util.function.Function | java.util.function.Supplier |  | 
| java.util.stream.Stream | java.lang.Iterable |  | 
| java.util.stream.Stream | java.util.List |  | 
| java.util.stream.Stream | java.util.Set |  | 
| javax.script.Invocable | org.oddjob.script.Invoker |  | 
| org.oddjob.arooa.reflect.BeanViewBean | org.oddjob.arooa.reflect.BeanView | Undocumented by org.oddjob.arooa.reflect.BeanViewBean | 
| org.oddjob.arooa.types.ArooaObject | *Various* |  | 
| org.oddjob.arooa.types.ClassType | java.lang.Class |  | 
| org.oddjob.arooa.types.ClassType | org.oddjob.arooa.reflect.ArooaClass |  | 
| org.oddjob.arooa.types.ConvertType | *Various* |  | 
| org.oddjob.arooa.types.IdentifiableValueType | *Various* |  | 
| org.oddjob.arooa.types.ImportType | *Various* |  | 
| org.oddjob.arooa.types.InlineType | org.oddjob.arooa.ArooaConfiguration | A conversion to a Configuration. | 
| org.oddjob.arooa.types.ListType | *Various* |  | 
| org.oddjob.arooa.types.ListType$ListConsumer | org.oddjob.arooa.types.ListType |  | 
| org.oddjob.arooa.types.MapType | *Various* |  | 
| org.oddjob.arooa.types.ValueType | *Various* |  | 
| org.oddjob.arooa.types.XMLConfigurationType | org.oddjob.arooa.ArooaConfiguration | Undocumented by org.oddjob.arooa.types.XMLConfigurationType | 
| org.oddjob.arooa.types.XMLType | java.lang.String |  | 
| org.oddjob.arooa.types.XMLType | org.oddjob.arooa.ArooaConfiguration |  | 
| org.oddjob.beanbus.destinations.BusCollect$ListContainer | java.lang.String |  | 
| org.oddjob.beanbus.destinations.BusCollect$ListContainer | java.util.List |  | 
| org.oddjob.beanbus.destinations.BusCollect$MapContainer | java.lang.String |  | 
| org.oddjob.beanbus.destinations.BusCollect$MapContainer | java.util.Map |  | 
| org.oddjob.events.WrapperOfFactory | org.oddjob.events.EventOfFactory | Undocumented by org.oddjob.events.WrapperOfFactory | 
| org.oddjob.io.AppendType | java.io.OutputStream |  | 
| org.oddjob.io.BufferType | java.io.InputStream |  | 
| org.oddjob.io.BufferType | java.io.OutputStream |  | 
| org.oddjob.io.BufferType | java.lang.String |  | 
| org.oddjob.io.BufferType | java.lang.String[] |  | 
| org.oddjob.io.FileType | java.io.File |  | 
| org.oddjob.io.FileType | java.io.File[] |  | 
| org.oddjob.io.FileType | java.nio.file.Path |  | 
| org.oddjob.io.FileType | java.nio.file.Path[] |  | 
| org.oddjob.io.FilesType | java.io.File[] |  | 
| org.oddjob.io.ResourceType | java.io.InputStream |  | 
| org.oddjob.io.ResourceType | java.lang.String |  | 
| org.oddjob.io.ResourceType | java.net.URL |  | 
| org.oddjob.io.StderrType | java.io.OutputStream |  | 
| org.oddjob.io.StderrType | java.util.function.Consumer |  | 
| org.oddjob.io.StdinType | java.io.InputStream | Undocumented by org.oddjob.io.StdinType | 
| org.oddjob.io.StdoutType | java.io.OutputStream |  | 
| org.oddjob.io.StdoutType | java.util.function.Consumer |  | 
| org.oddjob.io.TeeType | java.io.InputStream |  | 
| org.oddjob.io.TeeType | java.io.OutputStream |  | 
| org.oddjob.jmx.VanillaInterfaceHandler | org.oddjob.jmx.server.ServerInterfaceHandlerFactory | Undocumented by org.oddjob.jmx.VanillaInterfaceHandler | 
| org.oddjob.jmx.client.DirectInvocationBean | org.oddjob.jmx.client.DirectInvocationClientFactory | Undocumented by org.oddjob.jmx.client.DirectInvocationBean | 
| org.oddjob.jmx.client.UsernamePassword | java.util.Map | Undocumented by org.oddjob.jmx.client.UsernamePassword | 
| org.oddjob.jmx.server.SimpleServerSecurity | java.util.Map | Undocumented by org.oddjob.jmx.server.SimpleServerSecurity | 
| org.oddjob.logging.slf4j.LogoutType | java.io.OutputStream |  | 
| org.oddjob.logging.slf4j.LogoutType | java.util.function.Consumer |  | 
| org.oddjob.oddballs.OddballFactoryType | org.oddjob.oddballs.OddballFactory | Undocumented by org.oddjob.oddballs.OddballFactoryType | 
| org.oddjob.schedules.ScheduleType | java.util.Date |  | 
| org.oddjob.schedules.ScheduleType | org.oddjob.schedules.Interval |  | 
| org.oddjob.scheduling.ExecutorThrottleType | java.util.concurrent.ExecutorService | Undocumented by org.oddjob.scheduling.ExecutorThrottleType | 
| org.oddjob.script.InvokeType | *Various* |  | 
| org.oddjob.sql.ConnectionType | java.sql.Connection | Undocumented by org.oddjob.sql.ConnectionType | 
| org.oddjob.util.URLClassLoaderType | java.lang.ClassLoader | Undocumented by org.oddjob.util.URLClassLoaderType | 
| org.oddjob.values.properties.PropertiesType | java.util.Properties |  | 
| org.oddjob.values.types.ComparisonType | java.util.function.Predicate | Undocumented by org.oddjob.values.types.ComparisonType | 
| org.oddjob.values.types.DateType | java.util.Calendar |  | 
| org.oddjob.values.types.DateType | java.util.Date |  | 
| org.oddjob.values.types.FormatType | java.lang.String |  | 
| org.oddjob.values.types.MagicBeanType | java.lang.Object | Undocumented by org.oddjob.values.types.MagicBeanType | 
| org.oddjob.values.types.MagicClassType | org.oddjob.arooa.reflect.ArooaClass | Undocumented by org.oddjob.values.types.MagicClassType | 
| org.oddjob.values.types.PropertyType | java.lang.String |  | 
| org.oddjob.values.types.PropertyType | java.util.Properties |  | 
| org.oddjob.values.types.SequenceType | java.lang.Iterable | Undocumented by org.oddjob.values.types.SequenceType | 
| org.oddjob.values.types.TokenizerType | java.lang.String[] |  | 
| org.oddjob.values.types.TokenizerType | java.util.List |  | 

