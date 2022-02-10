
# This is a simple google map integration that uses google places api.

Basically, what I did here is I use geofire to capture nearby data and maps android utility sdk to parse the directions. 

Please see reference below for the libraries I used for this particular app<br />
- I use the lifecycle arch for this app. 

Maps SDK for Android Utility Library
- https://github.com/googlemaps/android-maps-utils

Java Client for Google Maps Services
- https://github.com/googlemaps/google-maps-services-java

Google Places for Android
- https://developers.google.com/maps/documentation/places/android-sdk/start

Geofire
- https://github.com/firebase/geofire-android

Firebase
- https://firebase.google.com/

<br />
Design pattern approach (MVVM)<br />
View <-> ViewModel <-> Repository <-> Local/Remote Data Source<br />
<br />
Features:<br />
  - The user would be able to see all available restuarants using the map and has option to filter the data through nearby feature. User also be able to see the direction from device current location going to the restaurant.
<br />
<br />
Possible additional features and enhancements:<br />
- I could use view bindings and implement the single activity architecture. i also might use coroutines or rxjava but the requirement is pretty much basic and straight forward



