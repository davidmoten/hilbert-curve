# hilbert-curve
<a href="https://travis-ci.org/davidmoten/hilbert-curve"><img src="https://travis-ci.org/davidmoten/hilbert-curve.svg"/></a><br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/hilbert-curve/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/hilbert-curve)<br/>
[![codecov](https://codecov.io/gh/davidmoten/hilbert-curve/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/hilbert-curve)<br/>

Java utilities for transforming distance along an N-dimensional Hilbert Curve to a point and back.

* supports N dimensions
* renders in 2-dimensions
* benchmarked with `jmh` (improvement PRs welcome!)

Status: *deployed to Maven Central*

Maven [reports](https://davidmoten.github.io/hilbert-curve/index.html) including [javadocs](https://davidmoten.github.io/hilbert-curve/apidocs/index.html)

Background
-------------
A [Hilbert curve](https://en.wikipedia.org/wiki/Hilbert_curve) is a continuous fractal space-filling curve first described by David Hilbert in 1891.

This library supports *approximations* to the Hilbert curve. *H<sub>n</sub>* is the n-th approximation to the Hilbert curve and is a path of 2<sup>n</sup>-1 straight line segments of length 1.

A Hilbert curve can be used to index multiple dimensions and has useful locality properties. In short, 

    Points with indexes close to an index will be close to the point corresponding to that index.

**Figure 1. 2D Hilbert curves with 1 to 6 bits (H<sub>1</sub> to H<sub>6</sub>)**

| | |
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
    <version>0.1.1</version>
</dependency>
``` 

Usage
---------

The maximum index on the Hilbert curve is 2<sup>dimensions*bits</sup> - 1. If your 
`bits * dimensions` is <= 63 then you can increase performance and reduce allocations by using the small option which uses `long` values for indexes rather than `BigInteger` values. 
JMH benchmarks show up to 30% better throughput using `small`. 


### Index from point

Get the index (distance along the curve in integer units) for a 2-dimensional point:

```java
HilbertCurve c = 
    HilbertCurve.bits(5).dimensions(2);
long[] point = {3, 4};
BigInteger index = c.index(point);
```

Small option:

```java
SmallHilbertCurve c = 
    HilbertCurve.small().bits(5).dimensions(2);
long[] point = {3, 4};
//returns long rather than BigInteger
long index = c.index(point);

```

### Point from index

Get the point corresponding to a particular index along the curve:

```java
HilbertCurve c = 
    HilbertCurve.bits(5).dimensions(2);
long[] point = c.point(22);
//or
long[] point = c.point(BigInteger.valueOf(22));
```

Small option:

```java
SmallHilbertCurve c = 
    HilbertCurve.small().bits(5).dimensions(2);
long[] point = c.point(22);
```

### Render a curve

To render a curve (for 2 dimensions only) to a PNG of 800x800 pixels:

```java
HilbertCurveRenderer.renderToFile(bits, 800, "target/image.png");
```

## Benchmarks

To run benchmarks:

```bash
mvn clean install -P benchmark
```

Credits
----------
Primary credit goes to John Skilling for his article "Programming the Hilbert curve" (American Institue of Physics (AIP) Conf. Proc. 707, 381 (2004)).

Thanks to Paul Chernoch for his [StackOverflow answer](http://stackoverflow.com/questions/499166/mapping-n-dimensional-value-to-a-point-on-hilbert-curve) which got me most of the way there.

My contribution was to translate the C# code to java (use `long` instead of `uint`) and to write the bit manipulations between the transposed index (not sure why Skilling favoured this representation) and the `BigInteger` index.
