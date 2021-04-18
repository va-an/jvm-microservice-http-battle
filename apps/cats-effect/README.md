How to build:

```shell script
$ ./gradlew -p apps/cats-effect clean shadowJar
```
---

How to run app:

```shell script
$ java -jar \
  -Xms512m \
  -Xmx512m \
  -Djava.net.preferIPv4Stack=true \
  -Djava.net.preferIPv6Addresses=false \
  apps/cats-effect/build/libs/cats-effect.jar
```
---

How to run load test:
```shell script
$ ./gradlew -p apps/load-test \
  -DTARGET_URL=http://localhost:8083/300 \
  -DSIM_USERS=1000 \
  gatlingRun
```
