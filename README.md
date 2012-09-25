# AppContext

A self sufficient utility project for creating hierarchical "contexts" (basically a maps of string-objects) sources from various sources.
This project "organically" grew from Nexus and the use of Plexus (the oldie one) from within.

Generally, the idea is that context (that is the map) is something like a "meta configuration", containing some 
bootstrap needed things, like location of configuration, some flags etc. It is not meant to hold _the_ configuration.
Similar as with "oldie" Plexus, you usually have a properties file lying somewhere that is used during application
bootstrap. But, some times you do want to override that properties file (maybe because it's "wired in" into some JAR for example,
and would be hard to unpack-edit-repack the JAR) using "conventional" means like setting some JVM System Property
or such.

Using JVM System Properties for these purposes is bad, as they are JVM-wide. Think about running samw WAR mounted on different
context paths in same webapp container. With System Properties, you would be affecting them _both_. AppContext gives you ability
to "target" only one of them.

Basically, an AppContext is identified by a String ID. When created, this ID also serves multiple purposes: when
using the "default" way to create an AppContext (this is customizable, but for simplicity's sake I'll omit 
custom use cases for now), you will get out-of-the-box prepopulated EntrySources too. So, in short, what you usually do:

 * choose unique ID for application context (not a hard requirement)
 * create an request for AppContext Factory
 * if applicable (see the story about properties file above), add your properties file source
 * create AppContext instance

What AppContext in this case gives you is:

 * AppContext (which implements a `Map<String,Object>`) instance, prepopulated with properties file
 * automatic overrides with values coming from OS environment variables having the prefix of ID (example: ID="nexus", env variable NEXUS_PORT=80, it would result in entry `port=80`).
 * automatic overrides with values coming from JVM System properties, having keys with prefix of ID + "."  (example: ID="nexus", system property "nexus.port=80", it would result in entry of `port=80`).

If your application is a WAR, you can keep your "overrides" in your OS user's environment who's running the application for example,
upgrade is painless: just delete old WAR, add new WAR, no need to tamper with them.

## Features

 * is able to "source" (pre-populate) the map from sources like JVM system properties, OS environment variables, Java properties file loaded from File System or over URL
 * is able to transform and/or filter the keys if needed, in various ways
 * is able to "publish" the map into system properties or logs or PrintWriter (latter usually for debugging purposes)
 * has simple (very very simply) extensible lifecycle support
 * has Guice module support, with SISU Parameters binding
 * has Servlet support

# Examples

Plain creation of context, sourced from a properties file (using `java.io.File` or `java.net.URL` if it is comming from classpath):

```
final AppContextRequest request = Factory.getDefaultRequest( "myapp" );
request.getSources().add( 0, new PropertiesFileEntrySource( new File("/some/path/myapp.properties") ) );
final AppContext context = Factory.create( req );  
```

The code above will create an AppContext with ID="myapp". It will get _prepopulated_ with properties file on
path `/some/path/myapp.properties`. But, the resulting map might be affected too, 
typically by changing your environment. Any system property prefixed with "myapp." 
(example `myapp.port=80`) will be injected into map (the previous example as `port=80`, hence key="port", value=80). 
Same stands for OS environment variables: MYAPP_PORT=80 would also get in as "port=80". And no need to change the
property files for these overrides!

When in SISU container, you could do following in your component:

```
public class MyComponent ...

@Inject
@Named("port")
private int port;
```

And value 80 would get injected into your field.


Have fun, 
~t~
