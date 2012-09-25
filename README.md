# AppContext

A self sufficient utility project for creating hierarchical "contexts" (basically a maps of string-objects) sources from various sources.

Features:

 * is able to "source" (pre-populate) the map from sources like JVM system properties, OS environment variables, Java properties file loaded from File System or over URL
 * is able to transform and/or filter the keys if needed, in various ways
 * is able to "publish" the map into system properties or logs or PrintWriter (latter usually for debugging purposes)
 * has simple (very very simply) extensible lifecycle support
 * has Guice module support, with SISU Parameters binding
 * has Servlet support

Example code:

```
final AppContextRequest req = Factory.getDefaultRequest( "myapp" );
final AppContext context = Factory.create( req );  
```

The code above will create an AppContext with ID="myapp". It will receive "default" entry sources, hence, it will be ready to have it pre-populate it, typically by changing your environment. Any system property prefixed with "myapp." (example `myapp.port=80`) will be injected into map (the previous example as `port=80`, hence key="port", value=80). Same stands for OS environment variables: MYAPP_PORT=80 would also get in as "port=80".

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
