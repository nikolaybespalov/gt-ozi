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
- ~~(UTM) Universal Transverse Mercator~~
- ~~(BNG) British National Grid~~
- ~~(IG) Irish Grid~~
- ~~(NZG) New Zealand Grid~~
- ~~(NZTM2) New Zealand TM 2000~~
- ~~(SG) Swedish Grid~~
- ~~(SUI) Swiss Grid~~
- ~~(I) France Zone I~~
- ~~(II) France Zone II~~
- ~~(III) France Zone III~~
- ~~(IV) France Zone IV~~
- Lambert Conformal Conic
- ~~(A)Lambert Azimuthual Equal Area~~
- ~~(EQC) Equidistant Conic~~
- Sinusoidal
- ~~Polyconic (American)~~
- Albers Equal Area
- Van Der Grinten
- ~~Vertical Near-Sided Perspective~~
- ~~(WIV) Wagner IV~~
- ~~Bonne~~
- ~~(MT0) Montana State Plane Zone 2500~~
- ~~(ITA1) Italy Grid Zone 1~~
- ~~(ITA2) Italy Grid Zone 2~~
- ~~(VICMAP-TM) Victoria Aust.(pseudo AMG)~~
- ~~(VICGRID) Victoria Australia~~
- ~~(VG94) VICGRID94 Victoria Australia~~
- ~~Gnomonic~~

## Supported datums
All datums from the [list](http://www.oziexplorer3.com/namesearch/datum_list.html) are supported.

## Supported ellipsoids
All ellipsoids from the [list](http://www.oziexplorer3.com/eng/help/userdatums.html) are supported.

## What about the .ozf2/.ozf3?
Look at [imageio-ozf](https://github.com/nikolaybespalov/imageio-ozf) library.
