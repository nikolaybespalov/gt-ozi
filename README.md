# gt-ozi
[![Build Status](https://travis-ci.org/nikolaybespalov/gt-ozi.svg?branch=master)](https://travis-ci.org/nikolaybespalov/gt-ozi)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/8c3475abc76c4885a5f72875edb0fd16)](https://www.codacy.com/app/nikolaybespalov/gt-ozi)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/8c3475abc76c4885a5f72875edb0fd16)](https://www.codacy.com/app/nikolaybespalov/gt-ozi)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.nikolaybespalov/gt-ozi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.nikolaybespalov/gt-ozi)

GeoTools plugin that allows you to use [OziExplorer](http://www.oziexplorer3.com/) spatial reference file(.map) in your Java application.

_This code is based on an implementation from GDAL._

![OziExplorer](README.png "OziExplorer")

It's as easy as reading any other map file
```
    File mapFile = new File("World.map");
    
    AbstractGridFormat oziFormat = GridFormatFinder.findFormat(mapFile);

    AbstractGridCoverage2DReader oziReader = oziFormat.getReader(mapFile);
    
    GridCoverage2D coverage2D = oziReader.read(...);
```
Just add dependency to your _pom.xml_
```xml
    <dependency>
        <groupId>com.github.nikolaybespalov</groupId>
        <artifactId>gt-ozi</artifactId>
        <version>${gt-ozi.version}</version>
        <scope>runtime</scope>
    </dependency>
```
Or to your _build.gradle_
```
    dependencies {
        runtime("com.github.nikolaybespalov:gt-ozi:${gt-ozi.version}")
    }
```
And your project will be able to work with .map files!

## Supported projections
- Latitude/Longitude
- Mercator
- Transverse Mercator
- (UTM) Universal Transverse Mercator

## Supported datums
All datums from the [list](http://www.oziexplorer3.com/namesearch/datum_list.html) are supported.

## Supported ellipsoids
All ellipsoids from the [list](http://www.oziexplorer3.com/eng/help/userdatums.html) are supported.

## What about the .ozf2/.ozf3 files?
Look at [imageio-ozf](https://github.com/nikolaybespalov/imageio-ozf) library.
