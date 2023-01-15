# GPXManager

Java Swing application to edit and merge GPX files

My need was to be able to merge several GPX files following a several days bike ride. Instead of having a GPX track for
each day, I wanted to have a unique GPX file with the full route.
So I started creating this application.
Currently, the application allows to:

- Open / Save GPX files
- Display and edit metadata
- Display some information about the tracks
- Display elevation graph for each track
- Merge several GPX files in 1
- Invert the tracks/routes to do it in the opposite direction

Probably the application will never display the GPX file on a map.

The application uses some code from geoCalc:
https://github.com/grumlimited/geocalc/blob/master/src/test/java/com/grum/geocalc
