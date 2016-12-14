#Abstract
---
XuanImageView extends ImageView with scaling function, rotating function, ect. Particularly, its auto-rotate-back-to-initial-state behavior mimics that in Google Photo.


#Strategies:
---
1. An image will be scaled and centered to fit the screen size at the very beginning (initial state).
2. Double-tap triggers auto-scale behavior.
3. If image's current scale level is bigger than maximum scale level or smaller than minimum scale level, the image will spring back to maximum scale level or minimum scale level.
4. The Image can only start to be rotated when it's in initial state.
5. Image will rotate back to initial state when rotation gesture is released.

# Available Setters in code
---
##setMaxScaleMultiple(float maxScaleMultiple)
An image is scaled to an InitScale to fit the size of XuanImageView at the very beginning. MaxScale = MaxScaleMultiple * InitScale holds.
##setDoubleTabScaleMultiple(float doubleTabScaleMultiple)
DoubleTapScale = DoubleTabScaleMultiple * InitScale holds. when image's current scale level is smaller than DoubleTabScale, the image will scale up to DoubleTapScale if a double-tap gesture is detected.
##setSpringBackGradientScaleUpLevel(float springBackGradientScaleUpLevel)
If current scale level is smaller than InitScale and image is not in rotation state, the image will scale up to InitScale with SpringBackGradientScaleUpLevel step by step.
Default springBackGradientScaleUpLevel is  1.01f.
##setSpringBackGradientScaleDownLevel(float springBackGradientScaleDownLevel)
If current scale level is bigger than MaxScale and image is not in rotation state, the image will scale down to MaxScale with SpringBackGradientScaleDownLevel step by step.
Default springBackGradientScaleDownLevel is 0.99f.
##setDoubleTapGradientScaleUpLevel(float doubleTapGradientScaleUpLevel)
When image's current scale level is smaller than DoubleTabScale, the image will scale up to DoubleTapScale with DoubleTapGradientScaleUpLevel step by step if a double-tap gesture is detected.
Default doubleTalGradientScaleUpLevel is 1.05f.
##setDoubleTabGradientScaleDownLevel(float doubleTapGradientScaleDownLevel)
When image's current scale level is bigger than DoubleTabScale, the image will scale down to InitScale with DoubleTapGradientScaleDownLevel step by step if a double-tap gesture is detected.
Default doubleTabGradientScaleDownLevel is 0.95f.
##setAutoRotationTrigger(float autoRotationTrigger)
When image's current rotation angle is bigger than AutoRotationTrigger, the image will rotate in the same direction and scale back to it's initial state if rotation gesture is released.
When image's current rotation angle is smaller than AutoRotationTrigger, the image will rotate in the opposite direction and scale back to it's initial state if rotation gesture is released.
Default AutoRotationTrigger is 60 (degrees).
##setSpringBackRunnableDelay(int springBackRunnableDelay)
Default SpringBackRunnableDelay is 10 (milliseconds).
##setDoubleTapScaleRunnableDelay(int delay)
Default DoubleTapScaleRunnableDelay is 10 (milliseconds).
##setAutoRotationRunnableDelay(int delay)
Default AutoRotationRunnableDelay is 5 (milliseconds).
##setAutoRotationRunnableTimes(int times)
Default AutoRotationRunnableTimes is 10 (times).

# Available Setters in xml
---
```xml
<com.allenxuan.xuanyihuang.xuanimageview.XuanImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/wallpaper1"
        android:scaleType="matrix"
        android:background="@android:color/background_dark"
        app:MaxScaleMultiple="float value"
        app:DoubleTabScaleMultiple="float value"
        app:SpringBackGradientScaleUpLevel="float value"
        app:SpringBackGradientScaleDownLevel="float value"
        app:DoubleTapGradientScaleUpLevel="float value"
        app:DoubleTapGradientScaleDownLevel="float value"
        app:AutoRotationTrigger="float value"
        app:SpringBackRunnableDelay="int value"
        app:DoubleTapScaleRunnableDelay="int value"
        app:AutoRotationRunnableDelay="int value"
        app:AutoRotationRunnableTimes="int value"
        />
```

