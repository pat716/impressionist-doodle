# impressionist-doodle

This impressionist drawing app was built using [Android Studio](http://developer.android.com/sdk/index.html?gclid=CjwKEAjw_ci3BRDSvfjortr--DQSJADU8f2jlIC6xbvMKN_ytRR2n6k0DDXhrn_M89zavaoyjZEzBBoCPXTw_wcB) with no external libraries used besides the code skeleton provided and the default toolset included with the software.

My demo of the features with audio narration can be seen [here](https://youtu.be/HaqQkQK3e80).

# Environment

The minimum Android version supported by this app is Android 4.1 Jelly Bean (API Level 16), while the target version is Android 6.0 Marshmallow (API Level 23). Testing was performed using an emulated Nexus 9 running Android 6.0 x86. 

# Running

This app should be run by importing the source code (all of which is included in this repository) into a new project in Android Studio with the settings described above, performing the default Gradle build process by selecting Build->Make Project in the Android Studio IDE, and running the app using a real or virtual Android device that mets the minimum specifications.

# Functionality

When the app is first loaded, both sides of the screen will be blank. An existing image must first be loaded into the left panel using the "Load" button at the bottom, at which point drawing will be enabled. The starting brush mode is a square-shaped drawing tool, but other brush modes can be selected using the "Brush" button including soft square, circle, soft circle, circle splatter, and line. The color sampling mode can be toggled using the "Color Sample" button. The default mode is fast, which takes a sample only from the pixel at the center of a given stroke. The other mode, accurate, will consider all pixels in the original image within the current brush stroke radius when determining the color used to paint. This will usually result in strokes whose color more closely match the original image, but performance is sometimes noticeably worse and the final product can look less "impressionistic" than with fast color sampling. 

Finally, the major feature I've worked on here is the spray paint mode. This mode, rather than being selected from the brush menu, is toggled on and off via the button. When spray paint mode is on, an intensity slider will appear to the right of the button. At the highest intensity, the brush paints a very hazy, flecked stroke, and larger strokes will leave more and more paint trails behind, which run down the screen creating a "dripping" effect. At the lowest intensity, the effect produced is essentially indistinguishable from the circle brush. If a large number of paint trails are in motion at once, performance can occasionally suffer since the screen is being constantly redrawn. However, the results (in my opinion) look cool enough for it to be worth it.

# References

The resources I consulted while building this project are as follows:

    Honestly, for this project, the in-class tutorials and exercises taught me 90% of what I needed to know in order to do the project. It also didn't hurt that nearly all of the programming I do in my free time is done using [Processing](https://processing.org) which has a lot of similarities to the kind of things I did here, so I was able to put a lot of that experience to use.
        
    My main resource outside of what was provided in class was (naturally) the Android API, with these entries in particular being especially helpful:
        http://developer.android.com/reference/android/graphics/Bitmap.html
        http://developer.android.com/reference/android/graphics/Paint.html
        http://developer.android.com/reference/android/graphics/Canvas.html
