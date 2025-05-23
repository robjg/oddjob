# Oddjob Reference

### Jobs

- [archive](org/oddjob/persist/ArchiveJob.md) - A Job that is capable of taking a snapshot of the state of it's child jobs.
- [archive-browser](org/oddjob/persist/ArchiveBrowserJob.md) - Browse archives previously create with an [archive](.//org/oddjob/persist/ArchiveJob.md).
- [bean](org/oddjob/arooa/types/BeanType.md) - Create an Object of the given class.
- [bean-report](org/oddjob/jobs/BeanReportJob.md) - Create a simple listing of the properties of beans.
- [cascade](org/oddjob/state/CascadeJob.md) - A job which triggers the next job after the previous one completes.
- [check](org/oddjob/jobs/CheckJob.md) - Checks a value for certain criteria.
- [copy](org/oddjob/io/CopyJob.md) - A Copy job.
- [delete](org/oddjob/io/DeleteJob.md) - Delete a file or directory, or files and directories.
- [depends](org/oddjob/jobs/job/DependsJob.md) - This job is deprecated, use [run](.//org/oddjob/jobs/job/RunJob.md) instead.
- [echo](org/oddjob/jobs/EchoJob.md) - Echo text to the console.
- [exec](org/oddjob/jobs/ExecJob.md) - Execute an external program.
- [exists](org/oddjob/io/ExistsJob.md) - Test if a file exists.
- [explorer](org/oddjob/monitor/MultiExplorerLauncher.md) - A container that allows multiple [org.oddjob.monitor.OddjobExplorer](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/monitor/OddjobExplorer.html)s to run.
- [file-watch](org/oddjob/io/FileWatchService.md) - Provide a service for subscribers to watch a file system for Files existing, being created or being modified.
- [folder](org/oddjob/jobs/structural/JobFolder.md) - Holds a collection of jobs but does not execute them.
- [foreach](org/oddjob/jobs/structural/ForEachJob.md) - A job which executes its child jobs for each of the provided values.
- [grab](org/oddjob/jobs/GrabJob.md) - Grab work to do.
- [grep](org/oddjob/io/GrepJob.md) - Search files or an input stream for lines containing a text value or matches for a regular expression.
- [input](org/oddjob/input/InputJob.md) - Ask for input from the user.
- [invoke](org/oddjob/script/InvokeJob.md) - Invoke a java method or script snippet.
- [is](org/oddjob/arooa/types/IsType.md) - Create an Object that is the class of the property.
- [java](org/oddjob/jobs/JavaJob.md) - Execute a Java Program in a separate process.
- [launch](org/oddjob/jobs/LaunchJob.md) - Launch an application via it's main method.
- [mkdir](org/oddjob/io/MkdirJob.md) - Make a directory, including any necessary but nonexistent parent directories.
- [oddjob](org/oddjob/Oddjob.md) - The starting point for a hierarchy of jobs.
- [parallel](org/oddjob/jobs/structural/ParallelJob.md) - A job which executes it's child jobs in parallel.
- [properties](org/oddjob/values/properties/PropertiesJob.md) - Creates properties that can used to configure other jobs.
- [rename](org/oddjob/io/RenameJob.md) - Rename a file or directory.
- [repeat](org/oddjob/jobs/structural/RepeatJob.md) - This job will repeatedly run its child job.
- [reset](org/oddjob/jobs/job/ResetJob.md) - A job which resets another job.
- [rmireg](org/oddjob/rmi/RMIRegistryJob.md) - A job which creates an RMI registry.
- [run](org/oddjob/jobs/job/RunJob.md) - A job which runs another job.
- [script](org/oddjob/script/ScriptJob.md) - Execute a script.
- [sequence](org/oddjob/jobs/SequenceJob.md) - Provide a sequence number which is incremented each time the job is executed.
- [sequential](org/oddjob/jobs/structural/SequentialJob.md) - Executes it's children in a sequence one after the other.
- [services](org/oddjob/framework/ServicesJob.md) - Allows objects to be registered that will automatically be injected into subsequent components that are configured for automatic dependency injection.
- [set](org/oddjob/values/SetJob.md) - A job which sets properties in other jobs when it executes.
- [sql](org/oddjob/sql/SQLJob.md) - Runs one or more SQL statements.
- [sql-keeper-service](org/oddjob/sql/SQLKeeperService.md) - Provides a [org.oddjob.scheduling.Keeper](http://rgordon.co.uk/oddjob/1.6.0/api/org/oddjob/scheduling/Keeper.html) that uses a database table.
- [sql-persister-service](org/oddjob/sql/SQLPersisterService.md) - Persists job state to a database.
- [start](org/oddjob/jobs/job/StartJob.md) - This job will run another job.
- [stop](org/oddjob/jobs/job/StopJob.md) - A job which stops another job.
- [switch](org/oddjob/jobs/structural/SwitchJob.md) - Switch Based on a predicate.
- [task-request](org/oddjob/jobs/tasks/TaskRequest.md) - This job requests a task be performed with optional properties.
- [task-service](org/oddjob/jobs/tasks/TaskExecutionService.md) - Provide a very simple task execution service.
- [variables](org/oddjob/values/VariablesJob.md) - This job provides a 'variables' like declaration within Oddjob.
- [wait](org/oddjob/jobs/WaitJob.md) - This Job will either wait a given number of milliseconds or will wait for a property or job to become available.
- [bus:bus](org/oddjob/beanbus/bus/BasicBusService.md) - Links components in a data pipeline.
- [bus:collect](org/oddjob/beanbus/destinations/BusCollect.md) - A component that collects beans in a list.
- [bus:driver](org/oddjob/beanbus/drivers/IterableBusDriver.md) - Drives data from an iterable (such as a [list](.//org/oddjob/arooa/types/ListType.md)) through a Bean Bus.
- [bus:filter](org/oddjob/beanbus/destinations/BeanFilter.md) - Filter out beans passing through the bus according to an `java.util.function.Predicate`.
- [bus:limit](org/oddjob/beanbus/destinations/BusLimit.md) - Only allow a certain number of beans passed.
- [bus:map](org/oddjob/beanbus/destinations/BusMap.md) - Apply a [java.util.function.Function](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/function/Function.html) to beans in a Bean Bus.
- [bus:queue](org/oddjob/beanbus/destinations/BusQueue.md) - A Queue for beans.
- [events:for](org/oddjob/events/ForEvents.md) - An Event Source For a variable set of child Event Sources.
- [events:list](org/oddjob/events/ListSource.md) - An event source that aggregates a list of child event sources.
- [events:trigger](org/oddjob/events/Trigger.md) - Trigger on an event.
- [events:watch](org/oddjob/events/EventWatchComponent.md) - Provides a component wrapper around a value type event source such as [state:watch](.//org/oddjob/state/expr/StateExpressionType.md).
- [events:when](org/oddjob/events/When.md) - Runs a job when triggered by the arrival of an event.
- [jmx:client](org/oddjob/jmx/JMXClientJob.md) - Connect to an Oddjob [jmx:server](.//org/oddjob/jmx/JMXServerJob.md).
- [jmx:server](org/oddjob/jmx/JMXServerJob.md) - A service which allows a job hierarchy to be monitored and managed remotely using a [jmx:client](.//org/oddjob/jmx/JMXClientJob.md).
- [jmx:service](org/oddjob/jmx/JMXServiceJob.md) - Expose a JMX Server so that Oddjob jobs can interact with it.
- [scheduling:retry](org/oddjob/scheduling/Retry.md) - This is a timer that runs it's job according to the schedule until the schedule expires or the job completes successfully.
- [scheduling:timer](org/oddjob/scheduling/Timer.md) - Provides a simple timer for periodic or once only execution of the child job.
- [scheduling:trigger](org/oddjob/scheduling/Trigger.md) - A trigger runs its job when the job being triggered on enters the state specified.
- [state:and](org/oddjob/state/AndState.md) - A job who's return state is a logical AND of the child states.
- [state:cascade](org/oddjob/state/CascadeJobDeprecated.md) - The namespace version this job is deprecated.
- [state:equals](org/oddjob/state/EqualsState.md) - Runs it's child job and then compares the state of the child job to the given state.
- [state:evaluate](org/oddjob/state/expr/StateExpressionJob.md) - Evaluate a state expression and become COMPLETE if it is true or INCOMPLETE otherwise.
- [state:flag](org/oddjob/state/FlagState.md) - When run it's state becomes the given state.
- [state:if](org/oddjob/state/IfJob.md) - This job implements an if/then/else logic based on job state.
- [state:join](org/oddjob/state/JoinJob.md) - Waits for a COMPLETE state from it's child job before allowing the thread of execution to continue.
- [state:mirror](org/oddjob/state/MirrorState.md) - When run this job mirrors the state of the given job.
- [state:or](org/oddjob/state/OrState.md) - A job who's return state is a logical OR of the child states.
- [state:resets](org/oddjob/state/Resets.md) - Captures Reset actions propagating down a job tree and either hardens soft resets to hard resets or softens hard resets to soft resets before passing them on to the child job.

### Types

- [append](org/oddjob/io/AppendType.md) - Specify a file for appending to.
- [bean](org/oddjob/arooa/types/BeanType.md) - Create an Object of the given class.
- [buffer](org/oddjob/io/BufferType.md) - A buffer can be used to accumulate output from one or more jobs which can then be used as input to another job.
- [class](org/oddjob/arooa/types/ClassType.md) - Returns a Class for the given name.
- [comparison](org/oddjob/values/types/ComparisonType.md) - Provides a Predicate from simple checks.
- [connection](org/oddjob/sql/ConnectionType.md) - Definition for a Database connection.
- [convert](org/oddjob/arooa/types/ConvertType.md) - Convert a value to the given Java Class.
- [date](org/oddjob/values/types/DateType.md) - Define a Date.
- [file](org/oddjob/io/FileType.md) - Specify a file.
- [file-persister](org/oddjob/persist/FilePersister.md) - Persist and load jobs from and to a file.
- [files](org/oddjob/io/FilesType.md) - Specify files using a wild card pattern, or a list.
- [format](org/oddjob/values/types/FormatType.md) - A type which can either format a number or a date into the given text format.
- [identify](org/oddjob/arooa/types/IdentifiableValueType.md) - Register a value with an Id.
- [import](org/oddjob/arooa/types/ImportType.md) - Import XML which is processed as if it's in-line.
- [inline](org/oddjob/arooa/types/InlineType.md) - A type that provides configuration.
- [input-confirm](org/oddjob/input/requests/InputConfirm.md) - A request for a yes/no confirmation.
- [input-file](org/oddjob/input/requests/InputFile.md) - A request for a file or directory.
- [input-message](org/oddjob/input/requests/InputMessage.md) - A Message with a prompt to continue.
- [input-password](org/oddjob/input/requests/InputPassword.md) - An input request for a password.
- [input-text](org/oddjob/input/requests/InputText.md) - A request for a simple line of input.
- [invoke](org/oddjob/script/InvokeType.md) - Invoke a java method or script snippet, or JMX operation.
- [is](org/oddjob/arooa/types/IsType.md) - Create an Object that is the class of the property.
- [list](org/oddjob/arooa/types/ListType.md) - A list provides a way of setting properties that are either [java.util.List](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/List.html) types or arrays.
- [logout](org/oddjob/logging/slf4j/LogoutType.md) - Provide an output to a logger.
- [magic-class](org/oddjob/values/types/MagicClassType.md) - Definition for a Magic Bean, which is a bean that can be defined dynamically.
- [map](org/oddjob/arooa/types/MapType.md) - A map allows a map of strings to values to be created.
- [oddballs](org/oddjob/oddballs/OddballsDescriptorFactory.md) - Create Oddjob job definition descriptors from any number of directories that follow the Oddball format.
- [properties](org/oddjob/values/properties/PropertiesType.md) - A type that evaluates to a java Properties object.
- [resource](org/oddjob/io/ResourceType.md) - Specify a resource on the class path.
- [schedule](org/oddjob/schedules/ScheduleType.md) - Applies a schedule to a given date to provide a calculated date.
- [sequence](org/oddjob/values/types/SequenceType.md) - A sequence.
- [sql-results-bean](org/oddjob/sql/SQLResultsBean.md) - Captures SQL results in a bean that has properties to provide those results to other jobs.
- [sql-results-sheet](org/oddjob/sql/SQLResultsSheet.md) - Writes SQL results to an output stream.
- [stderr](org/oddjob/io/StderrType.md) - Provide an output to the stderr stream of the console.
- [stdin](org/oddjob/io/StdinType.md) - Provide an output to the console.
- [stdout](org/oddjob/io/StdoutType.md) - Provide an output to stdout stream of the console.
- [tee](org/oddjob/io/TeeType.md) - Split output to multiple other outputs.
- [throttle](org/oddjob/scheduling/ExecutorThrottleType.md) - Throttle parallel execution.
- [tokenizer](org/oddjob/values/types/TokenizerType.md) - Tokenizes text.
- [url-class-loader](org/oddjob/util/URLClassLoaderType.md) - A simple wrapper for URLClassloader.
- [value](org/oddjob/arooa/types/ValueType.md) - A simple value.
- [xml](org/oddjob/arooa/types/XMLType.md) - A type that converts it's XML contents into a String.
- [arooa:annotation](org/oddjob/arooa/deploy/AnnotationDefinitionBean.md) - Create a synthetic annotation for a method.
- [arooa:bean-def](org/oddjob/arooa/deploy/BeanDefinitionBean.md) - Provide an element to class name mapping for a java bean.
- [arooa:configuration](org/oddjob/arooa/types/XMLConfigurationType.md) - Provide Configuration in XML format.
- [arooa:conversion](org/oddjob/arooa/deploy/ConversionDescriptorBean.md) - Provide a Bean for use in an [arooa:descriptor](.//org/oddjob/arooa/deploy/ArooaDescriptorBean.md) that provides conversions.
- [arooa:descriptor](org/oddjob/arooa/deploy/ArooaDescriptorBean.md) - A definition of an Arooa descriptor.
- [arooa:descriptors](org/oddjob/arooa/deploy/ListDescriptorBean.md) - An Arooa Descriptor Factory that is a container for a collection of other descriptors.
- [arooa:magic-beans](org/oddjob/arooa/beanutils/MagicBeanDescriptorFactory.md) - Define Magic Beans.
- [arooa:property](org/oddjob/arooa/deploy/PropertyDefinitionBean.md) - Provide a definition for a property within an [arooa:bean-def](.//org/oddjob/arooa/deploy/BeanDefinitionBean.md).
- [design:form](org/oddjob/arooa/design/layout/LtMainForm.md) - A form for designing a component.
- [design:group](org/oddjob/arooa/design/layout/LtFieldGroup.md) - A group of form items.
- [design:indexed](org/oddjob/arooa/design/layout/LtIndexedTypeSelection.md) - A Form Item for an indexed property.
- [design:mapped](org/oddjob/arooa/design/layout/LtMappedTypeSelection.md) - A form item for a mapped property.
- [design:radio](org/oddjob/arooa/design/layout/LtRadioSelection.md) - A radio button selection of form items.
- [design:single](org/oddjob/arooa/design/layout/LtSingleTypeSelection.md) - Form item for a property set from a selection of values.
- [design:tabs](org/oddjob/arooa/design/layout/LtTabGroup.md) - A group of tabs.
- [design:text](org/oddjob/arooa/design/layout/LtTextField.md) - A text field.
- [design:textarea](org/oddjob/arooa/design/layout/LtTextArea.md) - A text area for a text property.
- [events:file](org/oddjob/io/FileWatchEventSource.md) - Watch for a file and fire an event if/when it exists.
- [jmx:client-credentials](org/oddjob/jmx/client/UsernamePassword.md) - Provide a JMX simple security credentials environment for a [jmx:client](.//org/oddjob/jmx/JMXClientJob.md).
- [jmx:server-security](org/oddjob/jmx/server/SimpleServerSecurity.md) - Provide a JMX simple security environment for a [jmx:server](.//org/oddjob/jmx/JMXServerJob.md).
- [schedules:after](org/oddjob/schedules/schedules/AfterSchedule.md) - Schedule something after the given schedule.
- [schedules:broken](org/oddjob/schedules/schedules/BrokenSchedule.md) - This schedule allows a normal schedule to be broken by the results of another schedule.
- [schedules:count](org/oddjob/schedules/schedules/CountSchedule.md) - This schedule returns up to count number of child schedules.
- [schedules:daily](org/oddjob/schedules/schedules/DailySchedule.md) - A schedule for each day at, or from a given time.
- [schedules:date](org/oddjob/schedules/schedules/DateSchedule.md) - Provide a schedule for a specific date or define an interval between two dates.
- [schedules:day-after](org/oddjob/schedules/schedules/DayAfterSchedule.md) - A schedule that returns the day after when it's parent schedule is due.
- [schedules:day-before](org/oddjob/schedules/schedules/DayBeforeSchedule.md) - A schedule that returns the day before when it's parent schedule is due.
- [schedules:interval](org/oddjob/schedules/schedules/IntervalSchedule.md) - This schedule returns an interval from the given time to the interval time later.
- [schedules:last](org/oddjob/schedules/schedules/LastSchedule.md) - This schedule will return it's last due nested schedule within the given parent interval.
- [schedules:list](org/oddjob/schedules/ScheduleList.md) - Provide a schedule based on a list of schedules.
- [schedules:monthly](org/oddjob/schedules/schedules/MonthlySchedule.md) - A schedule for monthly intervals.
- [schedules:now](org/oddjob/schedules/schedules/NowSchedule.md) - Schedule something now.
- [schedules:time](org/oddjob/schedules/schedules/TimeSchedule.md) - Provide a schedule for an interval of time.
- [schedules:weekly](org/oddjob/schedules/schedules/WeeklySchedule.md) - A schedule for weekly intervals specified by days of the week.
- [schedules:yearly](org/oddjob/schedules/schedules/YearlySchedule.md) - A schedule for a range of months, or a month.
- [state:watch](org/oddjob/state/expr/StateExpressionType.md) - Evaluate a state expression that becomes an event source for triggering other jobs.

-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
