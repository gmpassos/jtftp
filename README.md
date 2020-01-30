JTFTP - Server
=============================

Java TFTP (Trivial File Transfer Protocol) Server.

## USAGE:


```
import org.gracilianomp.jtftp.*;

...

  int port = 69 ;
  File dir = new File("/tmp/tftp") ;

  new TFTPDaemon(port , dir) ;

...
```

## Features and bugs

Please file feature requests and bugs at the [issue tracker][tracker].

[tracker]: https://github.com/gmpassos/jtftp/issues

## Author

Graciliano M. Passos: [gmpassos@GitHub][github].

[github]: https://github.com/gmpassos

Based in original work (MIT LICENSE) of:

- John Herrlin: https://github.com/jherrlin/tftp-server-java

## License

MIT license.
