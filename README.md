# Killbill Issue Reproduction

## Summary

My company has been experiencing an issue with Killbill 0.22 after upgrading from 0.20.
There is an intermittent issue where the payload of the HTTP request is corrupt before handing it off to Jackson for 
deserialization.
The repo is intended to reproduce with issue with the most basic configuration possible.
This repo builds a very simple Killbill plugin that exposes a single REST endpoint.
The endpoint really doesn't do anything, it does depend on the ability to obtain the Tenant from request attributes, 
and a Java pojo from the HTTP body (using Jackson).

After building the plugin a docker image of killbill is created with the plugin deployed. Running a container and 
repeatedly sending requests will reproduce the problem.  With this setup, the frequency of the issue is extremely rare.
It occurs approximately once in every 5-10 thousand requests.  The problem is that in a test environment, using our 
production configuration of our Killbill deployment, the issue occurs much more frequently, approximately once in every 
50 - 100 requests, which is unacceptable.  We are working around the problem by using retries for HTTP 400 responses 
(which typically are non-retryable requests).  This is not a viable long-term solution as it requires modification of 
our standard production monitoring and alerting standards.

## How to Reproduce

### Prerequisites

Using this project will require the following:
1. A local Java 8 JDK (for building)
2. A local Maven 3 installation (for building)
3. A local docker environment with docker-compose support (for running Killbill)
4. A local Python 3.7+  installation (for running the test-harness)

### Build the plugin

With the repo checked out, `cd` into the `killbill-test-plugin` directory.
Build the plugin by issuing the following command:
```shell
mvn clean package
```
This should generate the file `killbill-test-plugin-0.22.32.jar` in the directory `killbill-test-plugin/target`.

### Prepare the Killbill (and Kaui) Database(s)

From the project root, we'll start a Postgres database container in docker and then 
run the Killbill .sql scripts to prepare a database.  Note that `docker-compose.yml` mounts 
a volume inside the project root (`./mnt/postgresql/data`), so that the data files are stored locally.   This means the 
database state will be preserved between runs of the postgres container, and the setup scripts 
only need to run once, the first time the docker environment is started.

Issue the commands:
```shell
docker-compose up --detach postgres
```
Allow several seconds (10-15) for the database to startup and initialize.  After it is 
initialized, you can proceed to run the setup scripts:
```shell
./setup_database.sh
```
This command will run several scripts in the `./killbill-db` directory.  Scan the output for any 
errors and troubleshoot as necessary.  Once the database has been initialized, proceed with bringing up 
killbill (and optionally, kaui).

### Build and start the Killbill Docker image

Building the docker image uses the base image `killbill/killbill:0.22.32`.  
All it does is add the plugin that we built in the previous step.  
The resulting image (with the plugin added) is: `sceaj/killbill-issue:0.22.32`.

_All of the following commands should be issued from the project root directory._

From the project root, issue the command:
```shell
docker-compose up --detach --build killbill
```
You can monitor the killbill logs using:
```shell
docker-compose logs --follow killbill
```

You can optionally bring up Kaui if desired using:
```shell
docker-compose up --detach kaui
```

### Reproducing the issue

Once Killbill has started successfully, you can run the test-harness using the command:
```shell
python killbill-issue.py <request-count>
```
_Depending on your environment, you may need to use the command "python3" instead of just "python"._

where `<requests-count>` is the number of requests the script should send to the plugin before exiting.
If a request fails (non HTTP 200 response), the test-harness will retry the same request up to 5 times.  In our 
experience the second attempt always succeeds.

The first time you run the script, you'll probably want to use a request-count of 1 or 2 just to make sure things are 
working.  A clean execution will just return to command prompt (whereas an error would include addition 
output about the error).

A clean execution of 1 request looks something like:
```shell
$python killbill-issue.py 1
$
```

Once a small number of requests are successful, you can attempt a longer run.  If you specify a large enough
request count (10-20k) you will probably see output something like:
```shell
$killbill-issue % python killbill-issue.py 20000
 Completed 500 requests...
 Completed 1000 requests...
 Completed 1500 requests...
 Retry: 0  Request Failed - status: 500
                request: {"externalId": "ca316ad6-8740-4106-9d06-3d716331fa09", "someDate": "2023-02-21", "someValue": 3.1415926535}
               response: {'message': 'Cannot construct instance of `sceaj.killbill.test.plugin.model.TestData` (although at least one Creator exists): no String-argument constructor/factory method to deserialize from String value (&#39;externalId&#39;)\n at [Source: (byte[])&quot;&quot;externalId&quot;: &quot;ca316ad6-8740-4106-9d06-3d716331fa09&quot;, &quot;someDate&quot;: &quot;2023-02-21&quot;, &quot;someValue&quot;: 3.1415926535}&quot;; line: 1, column: 1]', 'stacktrace': ["com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot construct instance of `sceaj.killbill.test.plugin.model.TestData` (although at least one Creator exists): no String-argument constructor/factory method to deserialize from String value ('externalId')", ' at [Source: (byte[])""externalId": "ca316ad6-8740-4106-9d06-3d716331fa09", "someDate": "2023-02-21", "someValue": 3.1415926535}"; line: 1, column: 1]', ...
```
That "Request Failed" is an example of the issue.  If you debug you can find that Jackson is being presented with a body that appears something like:
```json
"externalId": "ca316ad6-8740-4106-9d06-3d716331fa09", ...
```
In other words, the opening "{" is missing and is caused by the ServletRequest object having "skipped over" the opening brace.  There is an internal buffer that 
shows all of the data that has been read off the network, and it definitely includes the opening "{", but is some cases it is skipped.
