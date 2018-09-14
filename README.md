# gt-ozi
[![Build Status](https://travis-ci.org/nikolaybespalov/gt-ozi.svg?branch=master)](https://travis-ci.org/nikolaybespalov/gt-ozi)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/8c3475abc76c4885a5f72875edb0fd16)](https://www.codacy.com/app/nikolaybespalov/gt-ozi)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/8c3475abc76c4885a5f72875edb0fd16)](https://www.codacy.com/app/nikolaybespalov/gt-ozi)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.nikolaybespalov/gt-ozi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.nikolaybespalov/gt-ozi)

GeoTools plugin that allows you to use [OziExplorer](http://www.oziexplorer3.com/) spatial reference file(.MAP) 
without using GDAL/[OziApi](http://www.oziexplorer3.com/oziapi/oziapi.html) or any other environment dependencies.

The main idea is to parse the projection parameters from .MAP file and provide them to [WorldImageReader](http://docs.geotools.org/stable/javadocs/org/geotools/gce/image/WorldImageReader.html).

## Supported projections
- Latitude/Longitude
- Mercator
- Transverse Mercator
- Lambert Conformal Conic
- Sinusoidal
- Albers Equal Area
- ~~(UTM) Universal Transverse Mercator~~

## Supported datums
All datums from the [list](http://www.oziexplorer3.com/namesearch/datum_list.html) are supported.

## Supported ellipsoids
All ellipsoids from the [list](http://www.oziexplorer3.com/eng/help/userdatums.html) are supported.

## What about the .ozf2/.ozf3?
Look at [imageio-ozf](https://github.com/nikolaybespalov/imageio-ozf) library.