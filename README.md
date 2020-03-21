# ScreenShade

<img src="/app/images/demo_part_1.gif" width="200"> <img
src="/app/images/demo_part_2.gif" width="200">

[Link to Play Store](https://play.google.com/store/apps/details?id=com.armpatch.android.secretscreen&hl=en_US "Play Store")

ScreenShade is an app I created to experiment with overlays and
animations. This effect is made possible by drawing over other apps. The
effect is most pronounced on phones with OLED screens, which provide
deeper blacks.

I implemented the TapTargetView library for guiding the user through the
basics elements of the app.

## Getting Started

To enable the overlay, single tap the floating button.

Once the overlay is covering the screen, double tap the screen or hit
the back button to hide the overlay.

You can peek behind the shade overlay by tapping and holding the screen.

The floating button can be removed by dragging it to the bottom of the screen.

## App Details

The minimum Android SDK version is 23.  
The "draw over apps" permission is required to run.

## How it works

Screen Shade is able to create Views that persist between apps by adding
Views to the Window Manager system service. When the enable button in
the main activity is clicked, a service is started that handles all of
the logic and overlay creation.

### The Window Manager

The WindowManager is an extension of the ViewManager, which has three
methods available for use:
- addView()
- updateViewLayout()
- removeView()

These methods provide no exception/error handling by themselves (ex: in
the case of adding a view twice, or removing a view that is not yet
added to the window manager). For this reason, I created an
[Overlay](/app/src/main/java/com/armpatch/android/screenshade/overlay/Overlay.java)
class that would handle these methods safely and abstract away
functionality shared between the various overlays in this app.

### OverlayService and OverlayManager

When the app is started, an instance of an
[OverlayService](/app/src/main/java/com/armpatch/android/screenshade/service/OverlayService.java)
is created, which holds an instance of
[OverlayManager](/app/src/main/java/com/armpatch/android/screenshade/overlay/OverlayManager.java),
which supervises the various overlays and handles their interactions.



