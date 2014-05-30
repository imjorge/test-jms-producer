[![Build Status](https://snap-ci.com/imjorge/test-jms-producer/branch/master/build_image)](https://snap-ci.com/imjorge/test-jms-producer/branch/master)

![Build Status](https://www.codeship.io/projects/16d92850-ca3d-0131-e4eb-121d56ccd2c2/status)

test-jms-producer
=================

Sends a JMS message to a configurable destination.

First, build the project:
> mvn clean install

Create a conf directory:
> mkdir conf

Copy examples to conf directory:
> cp examples\* conf\

Edit necessary settings inside files on `conf` folder.

Send an event like this. the text between `{` and `}` is the payload for the text message.

> echo {c123} | java -jar target\test-jms-producer-0.0.1-SNAPSHOT.one-jar.jar -c conf\application.jboss4.properties

You can also run and enter the contents afterwards:

> java -jar target\test-jms-producer-0.0.1-SNAPSHOT.one-jar.jar -c conf\application.jboss4.properties

You can write JMS headers before the first `{` in the format `key=value`. They will be read as Java properties format and injected into the JMS mesage.

> my_header_1=12

> my_header_2=abc

Afterwards enter `{`, then the payload for the text message and end with `}`.
