# GPXManager

Java Swing application to edit, merge GPX files and get your GPX Files from Strava

My need was to be able to merge several GPX files following a several days bike ride. Instead of having a GPX track for
each day, I wanted to have a unique GPX file with the full route.
So I started creating this application.
Currently, the application allows to:

- Open / Save GPX files
- Display and edit metadata
- Display some information about the tracks and routes
- Display elevation graph for each track and routes
- Merge several GPX files in 1
- Invert the tracks/routes to do it in the opposite direction
- Access Strava activities: Download the GPX of activities
- Display statistics based on Strava activities

Probably the application will never display the GPX file on a map.

The application uses some code from geoCalc:
https://github.com/grumlimited/geocalc/blob/master/src/test/java/com/grum/geocalc

### Running the application

Running MyGPXManagerLauncher.jar is enough to run the program. By using this jar, the program will be updated
automatically. You can download the MyGPXManagerLauncher.jar in the Release page. Then double-click on the jar file
or execute : java -jar MyGPXManagerLauncher.jar

If you want to invert a GPX file or merge several files via command line, it's possible.
Run MyGPXManagerCommand.jar with options.
Usages:  
-invert -file <.file.> -target <.file.>  
-merge -files <file,file...> -target <.file.>

Supported languages:

- English
- Nederlands
- Français

### Java is required

https://www.oracle.com/java/technologies/downloads/