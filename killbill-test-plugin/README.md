# killbill-test-plugin
A simple plugin for killbill that exposes a REST endpoint.
It is based on the killbill-hello-world-java-plugin.
It present a single endpoint `/plugins/killbill-test-plugin/tests/v1`
that deserializes a simple JSON payload into a Java Pojo.  When any corruption of
the HTTP payload occurs, Jackson will throw an exception that it cannot deserialize the 
HTTP body into the object.