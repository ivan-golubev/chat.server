Async Chat server
------------------------------------

Installation
-------------------------
1. Either download a zip or perform a git checkout from [github][1].
2. Install [JDK 8][2].
3. Install [Gradle][3].
4. [Set up][4] the environment variable: GRADLE\_HOME.
5. Satisfy the dependency: 'net.ivango.chat', name: 'chat.common', version: '1.0-SNAPSHOT'
by downloading the [chat.common][5] project and installing it into your local maven repository:

`> gradle clean install`

Usage
-----
To build the project:
execute this in console (cd to the project root directory first) or in you favourite IDE:

`> gradle clean build`

To run the server:

`> cd build\libs\`

`> java -jar chat.server-*.jar <host> <port>`

This will start the server at the specified address and port.
For example you may use your network ip address:

`> java -jar chat.server-*.jar 10.215.57.5 8989`

Once the server is up and running you may start to use the chat clients:
use the specified address to connect to this server.

To get a verbose output switch from INFO to DEBUG in src/main/resources/log4j.properties

[1]: https://github.com/ivan-golubev/chat.server
[2]: http://www.oracle.com/technetwork/java/javase/downloads
[3]: https://gradle.org/gradle-download/
[4]: https://docs.gradle.org/current/userguide/installation.html
[5]: https://github.com/ivan-golubev/chat.common
