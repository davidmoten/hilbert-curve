# hilbert-curve
<a href="https://travis-ci.org/davidmoten/hilbert-curve"><img src="https://travis-ci.org/davidmoten/hilbert-curve.svg"/></a><br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/hilbert-curve/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/hilbert-curve)<br/>
[![codecov](https://codecov.io/gh/davidmoten/hilbert-curve/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/hilbert-curve)<br/>

Java utilities for 

* transforming distance along an N-dimensional Hilbert Curve to a point and back.
* bounding box (N-dimensional) query support (bounding box is mapped to a number of intervals on the hilbert index for single column lookup)

Features

* supports multiple dimensions
* method chaining
* renders in 2-dimensions
* benchmarked with `jmh`

Status: *deployed to Maven Central*

Maven [reports](https://davidmoten.github.io/hilbert-curve/index.html) including [javadocs](https://davidmoten.github.io/hilbert-curve/apidocs/index.html)

Background
-------------
A [Hilbert curve](https://en.wikipedia.org/wiki/Hilbert_curve) is a continuous fractal space-filling curve first described by David Hilbert in 1891.

This library supports *approximations* to the Hilbert curve. *H<sub>n</sub>* is the n-th approximation to the Hilbert curve and is a path of 2<sup>n</sup>-1 straight line segments of length 1.

A Hilbert curve can be used to index multiple dimensions and has useful locality properties. In short, 

* *Points with indexes close to an index will be close to the point corresponding to that index.*

**Figure 1. 2D Hilbert curves with 1 to 6 bits (H<sub>1</sub> to H<sub>6</sub>)**

| | |
| --- | --- |
|  <img src="src/docs/hilbert-2d-bits-1.png?raw=true" /> |  <img src="src/docs/hilbert-2d-bits-2.png?raw=true" />
|  <img src="src/docs/hilbert-2d-bits-3.png?raw=true" /> |  <img src="src/docs/hilbert-2d-bits-4.png?raw=true" />
|  <img src="src/docs/hilbert-2d-bits-5.png?raw=true" /> |  <img src="src/docs/hilbert-2d-bits-6.png?raw=true" />

```java
HilbertCurveRenderer.renderToFile(bits, 200, filename);
```

**Figure 2. 2D Hilbert curves with 1 to 4 bits colorized and labelled**

| | |
| --- | --- |
|  <img src="src/docs/hilbert-color-1.png?raw=true" /> |  <img src="src/docs/hilbert-color-2.png?raw=true" />
|  <img src="src/docs/hilbert-color-3.png?raw=true" /> |  <img src="src/docs/hilbert-color-4.png?raw=true" />

```java
HilbertCurveRenderer.renderToFile(bits, 600, filename, Option.COLORIZE, Option.LABEL);
```

**Figure 3. 2D Hilbert curves with 5 bits colorized and labelled**

<img src="src/docs/hilbert-color-5.png?raw=true" />


```java
HilbertCurveRenderer.renderToFile(5, 1000, filename, Option.COLORIZE, Option.LABEL);
```

Getting started
-----------------
Add this to your maven pom.xml:

```xml
<dependency>
    <groupId>com.github.davidmoten</groupId>
    <artifactId>hilbert-curve</artifactId>
    <version>0.1.3</version>
</dependency>
``` 

Usage
---------
### Limits
Maximum bits is 63 (index ranges from 0 to ~10<sup>19</sup>), max dimensions is not practically limited (sure you can specify 10<sup>9</sup> dimensions but that's a lot of data per point!).

### Small

The maximum index on the Hilbert curve is 2<sup>bits * dimensions</sup> - 1. If your 
`bits * dimensions` is <= 63 then you can increase performance and reduce allocations by using the <b>small</b> option which uses `long` values for indexes rather than `BigInteger` values. 
JMH benchmarks show up to 30% better throughput using `small`. 

### Points
The hilbert curve wiggles around your n-dimensional grid happily visiting each cell. The ordinates in each dimension are integers in the range 0 .. 2<sup>bits</sup>-1.
 
### Index from point

Get the index (distance along the curve in integer units) for a 2-dimensional point:

```java
HilbertCurve c = 
    HilbertCurve.bits(5).dimensions(2);
BigInteger index = c.index(3, 4);
```

Small option:

```java
SmallHilbertCurve c = 
    HilbertCurve.small().bits(5).dimensions(2);
//returns long rather than BigInteger
long index = c.index(3, 4);

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

You can save allocations if you use this method (available on `HilbertCurve` and `SmallHilbertCurve`) to calculate a point:

```java
long[] x = new long[dimensions];
c.point(index, x);
```

Benchmarks indicate that throughput is increased about 25% using this method but only with the `small()` option.

### Render a curve

To render a curve (for 2 dimensions only) to a PNG of 800x800 pixels:

```java
HilbertCurveRenderer.renderToFile(bits, 800, "target/image.png");
```

### Querying N-dimensional space

```java
SmallHilbertCurve c = HilbertCurve.small().bits(5).dimensions(2);
long[] point1 = new long[] {3, 3};
long[] point2 = new long[] {8, 10};
int numSplits = 0;
List<Range> ranges = c.query(point1, point2, numSplits);
ranges.stream().forEach(System.out::println);
```
Result:
```
Range [low=10, high=229]
```
We can improve the ranges by increasing `numSplits`.


`numSplits` is 1
```java
Range [low=10, high=53]
Range [low=69, high=132]
Range [low=210, high=229]
```

`numSplits` is 2
```java
Range [low=10, high=10]
Range [low=26, high=53]
Range [low=69, high=69]
Range [low=122, high=132]
Range [low=210, high=221]
Range [low=227, high=229]
```

`numSplits` is 3
```java
Range [low=10, high=10]
Range [low=26, high=53]
Range [low=69, high=69]
Range [low=122, high=128]
Range [low=131, high=132]
Range [low=210, high=221]
Range [low=227, high=229]
```

`numSplits` is 4
```java
Range [low=10, high=10]
Range [low=26, high=28]
Range [low=31, high=48]
Range [low=51, high=53]
Range [low=69, high=69]
Range [low=122, high=124]
Range [low=127, high=128]
Range [low=131, high=132]
Range [low=210, high=221]
Range [low=227, high=229]
```

When using querying do experiments with the number of bits and `splitDepth` to get your ideal run time. The perimeter traversal used by the `query` method is O(2<sup>bits*(dimensions-1)</sup>.

## Benchmarks

To run benchmarks:

```bash
mvn clean install -P benchmark
```

Result 23/11/2017
```
Benchmark                                         Mode  Cnt     Score     Error  Units
Benchmarks.pointSmallTimes1000                   thrpt   10  7035.676 ± 303.699  ops/s
Benchmarks.pointSmallTimes1000LowAllocation      thrpt   10  9134.077 ± 439.232  ops/s
Benchmarks.pointTimes1000                        thrpt   10  6346.989 ±  32.957  ops/s
Benchmarks.pointTimes1000LowAllocation           thrpt   10  6451.754 ±  30.435  ops/s
Benchmarks.roundTripSmallTimes1000               thrpt   10  4008.059 ± 197.650  ops/s
Benchmarks.roundTripSmallTimes1000LowAllocation  thrpt   10  5136.638 ±  22.340  ops/s
Benchmarks.roundTripTimes1000                    thrpt   10  3138.075 ±  20.720  ops/s
Benchmarks.roundTripTimes1000LowAllocation       thrpt   10  3261.835 ±  11.208  ops/s

```

Credits
----------
Primary credit goes to John Skilling for his article "Programming the Hilbert curve" (American Institue of Physics (AIP) Conf. Proc. 707, 381 (2004)).

Thanks to Paul Chernoch for his [StackOverflow answer](http://stackoverflow.com/questions/499166/mapping-n-dimensional-value-to-a-point-on-hilbert-curve) which got me most of the way there.

Dave Moten's contribution:

* translate the C# code to java (use `long` instead of `uint`)
* write the bit manipulations between the transposed index and the `BigInteger` index
* lots of unit tests
* made a simple easy-to-read API (hopefully)
* TODO supplement with utilities for using a Hilbert Index with bounding box searches
