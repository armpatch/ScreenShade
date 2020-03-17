# ScreenShade

<img src="/app/images/demo_part_1.gif" width="200"> <img
src="/app/images/demo_part_2.gif" width="200">

Play Store: https://play.google.com/store/apps/details?id=com.armpatch.android.secretscreen&hl=en_US

ScreenShade is an app that draws over other apps to cover content on your screen, giving the appearance that the display is off. This effect is most effective on OLED screens, which produce no light when black. 

To enable the overlay, single tap the floating button. Once the overlay is covering the screen, double tap the screen or hit the back button to hide the overlay.

The floating button can be removed by dragging it to the bottom of the screen.

## Getting Started

The current version of the app is available for free on the play store.

### Prerequisites

The minimum Android SDK version is 23.

### Permissions

This app requires "draw over apps" to run.

## How it works

Screen Shade is able to create Views that persist between apps by adding Views through the Window Manager system service, which are created and destroyed through a background service.

### The Window Manager

The WindowManager is an extension of the ViewManager, which has three methods available for use:
- addView()
- updateViewLayout()
- removeView()

These methods provide no exception/error handling by themselves (ex: in the case of adding a view twice, or removing a view that is not yet added to the window manager). For this reason, I created an [Overlay](/app/src/main/java/com/armpatch/android/screenshade/overlay/Overlay.java) class that would handle these methods safely and abstract away functionality shared between the various overlays in this app.

### OverlayService and OverlayManager

When the app is started, an instance of an [OverlayService](/app/src/main/java/com/armpatch/android/screenshade/service/OverlayService.java) is created, which holds an [OverlayManager](/app/src/main/java/com/armpatch/android/screenshade/overlay/OverlayManager.java). This OverlayManager supervises the various instances of Overlays, and implements callbacks so that overlays are not communicating with eachother, only to the manager.

For example, when the [ButtonOverlay](/app/src/main/java/com/armpatch/android/screenshade/overlay/ButtonOverlay.java) is tapped, it sends a callback to the OverlayManager, which then tells the [ShadeOverlay](/app/src/main/java/com/armpatch/android/screenshade/overlay/ShadeOverlay.java) to start a reveal animation from the center of the button's location. The Button and Shade overlays have no knowledge of eachother's functionality.



