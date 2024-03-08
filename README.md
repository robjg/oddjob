# Oddjob

Oddjob is a task automation and scheduling solution.

### Running

Full Oddjob distributions can be [Downloaded](http://rgordon.co.uk/oddjob/download.html)
and [Run from the Console](http://rgordon.co.uk/oddjob/1.6.0/userguide/started.html#running)

Or Oddjob can be started from your favorite IDE with this Maven dependency

```xml
<dependency>
    <groupId>uk.co.rgordon</groupId>
    <artifactId>oddjob</artifactId>
    <version>1.6.0</version>
</dependency>
```

And this main `org.oddjob.Main` with 

```xml
<oddjob>
    <job>
        <echo>Hello World</echo>        
    </job>
</oddjob>
```

saved as `oddjob.xml` in your projects Working Directory.

Oddjob can also be [Embedded](http://rgordon.co.uk/oddjob/1.6.0/devguide/embedding.html)
in your own applications.

### Configuration

The [Reference Pages](docs/reference/README.md) provide lots of 
details for configuring Oddjob's various jobs.

### Modules

This is the main module for Oddjob, however extra functionality is
provided via Oddjob plugins called Oddballs.

- [oj-ant](../oj-ant)
: Run Ant from within Oddjob

- [oj-net](../oj-net) 
: FTP jobs

- [oj-mail](../oj-mail)
: Mail jobs

- [oj-ssh](../oj-ssh)
: SSH jobs

- [oj-web](../oj-ssh)
: HTTP client and server jobs. Includes the server side for the 
web UI.

### Building

To build this module as a Shapshot you will first need to clone and 
Maven Install [oj-parent](../oj-parent) and [arooa](../arooa).

[oj-assembly](../oj-assembly) provides a module POM capable of building
all the Oddjob modules included in the distribution.


Other modules that contribute to the final Oddjob distribution are
[run-oddjob](../run-oddjob) - the launch framework for Oddjob,
[oj-reactjs](../oj-reactjs) - the Web UI front end, [oj-doc](../oj-doc)
which builds the Reference pages amongst other things, and 
[oj-examples](../oj-examples) which are examples and also the
source of the Oddjob documentation.


### More Info

More information can be found on [Oddjob's Home Page](http://rgordon.co.uk/oddjob).

