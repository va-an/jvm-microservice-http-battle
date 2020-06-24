How to build:

```shell script
$ ./gradlew -p apps/reactive-akka clean shadowJar
```
---

How to run app:

```shell script
$ java -jar \
  -Xms512m \
  -Xmx512m \
  -Djava.net.preferIPv4Stack=true \
  -Djava.net.preferIPv6Addresses=false \
  -Dspring.output.ansi.enabled=ALWAYS \
  apps/reactive-akka/build/libs/reactive-akka.jar
```
---

How to run load test:
```shell script
$ ./gradlew -p apps/load-test \
  -DTARGET_URL=http://localhost:8084/300 \
  -DSIM_USERS=1000 \
  gatlingRun
```
