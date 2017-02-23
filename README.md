# hilbert-curve
<a href="https://travis-ci.org/davidmoten/hilbert-curve"><img src="https://travis-ci.org/davidmoten/hilbert-curve.svg"/></a><br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/hilbert-curve/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/hilbert-curve)<br/>
[![codecov](https://codecov.io/gh/davidmoten/hilbert-curve/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/hilbert-curve)<br/>

Java utilities for transforming distance along N-dimensional Hilbert Curve to a point and back.

* supports N dimensions
* renders in 2-dimensions

Status: *deployed to Maven Central*

|  | |
| --- | --- |
|  <img src="src/docs/hilbert-2d-bits-1.png?raw=true" /> |  <img src="src/docs/hilbert-2d-bits-2.png?raw=true" />
|  <img src="src/docs/hilbert-2d-bits-3.png?raw=true" /> |  <img src="src/docs/hilbert-2d-bits-4.png?raw=true" />
|  <img src="src/docs/hilbert-2d-bits-5.png?raw=true" /> |  <img src="src/docs/hilbert-2d-bits-6.png?raw=true" />

Getting started
-----------------
Add this to your maven pom.xml:

```xml
<dependency>
    <groupId>com.github.davidmoten</groupId>
    <artifactId>hilbert-curve</artifactId>
    <version>0.1</version>
</dependency>
``` 

Usage
---------

Get index (distance along the curve in integer units) to 2-dimensional point:

```java
HilbertCurve c = 
    HilbertCurve.bits(5).dimensions(2);
long[] point = c.point(22);
```

Get the point corresponding to a particular index along the curve:

```java
HilbertCurve c = 
    HilbertCurve.bits(5).dimensions(2);
long[] point = {3, 4};
BigInteger index = c.index(point);
```

Credits
----------
Primary credit goes to John Skilling for his article "Programming the Hilbert curve" (American Institue of Physics (AIP) Conf. Proc. 707, 381 (2004)).

Thanks to Paul Chernoch for his [StackOverflow answer](http://stackoverflow.com/questions/499166/mapping-n-dimensional-value-to-a-point-on-hilbert-curve) which got me most of the way there.

My contribution was to translate the C# code to java (use `long` instead of `uint`) and to write the bit manipulations between the transposed index (not sure why Skilling favoured this representation) and the `BigInteger` index.
