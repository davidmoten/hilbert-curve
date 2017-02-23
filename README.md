# hilbert-curve
Java utilities for transforming distance along N-dimensional Hilbert Curve to a point and back

* supports N dimensions
* renders in 2-dimensions

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