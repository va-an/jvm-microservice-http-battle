### Comparison microservices HTTP-layer performance in JVM world 

Based on the [article](https://dev.to/bufferings/springboot2-blocking-web-vs-reactive-web-46jn).
---

All apps used HTTP/1.1 for a server and client.

Fighters:
* Java + Spring Web (blocking-app)
* Java + Spring Reactive Web (reactive-app)
* Scala + http4s (reactive-cats)
* Scala + Akka HTTP (reactive-akka)

Referees:
* delay-service
* load-test
---
