#XuanImageView
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/index.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-red.svg)](http://www.apache.org/licenses/LICENSE-2.0)
####XuanImageView extends ImageView with scaling function, rotating function, ect. Particularly, its auto-rotate-back-to-initial-state behavior mimics that in Google Photo.

##Screenshot
![demo](/screenshots/XuanImageViewDemo.gif)

##XuanImageView demo video - YouTube
<a href="https://youtu.be/wxFAdm8J_bE" target="_blank">
  <img alt="Youtube"
       src="/art/youtube_icon.png"
       width="20%">
</a>

##Get demo app
Demo app is available on Googl Play :

<a href="https://play.google.com/store/apps/details?id=com.allenxuan.xuanyihuang.xuanimageviewproject" target="_blank">
  <img alt="Google Play"
       src="/art/get_it_on_googleplay.png"
       width="20%"
       >
</a>

Or you can get demo apk under /demo/demo-release.apk.

##Strategies:
1. An image will be scaled and centered to fit the screen size at the very beginning (initial state).
2. Double-tap triggers auto-scale behavior.
3. If image's current scale level is bigger than maximum scale level or smaller than minimum scale level, the image will spring back to maximum scale level or minimum scale level.
4. The Image can only start to be rotated when it's in initial state.
5. Image will rotate back to initial state when rotation gesture is released.

##Get started
###Gradle dependency
This library is available on JCenter, so you need add this to your project's build.gradle (usually it is already there by default).
```
allprojects {
    repositories {
        jcenter()
    }
}
```
and add this to your module's build.gradle.
```
dependencies {
    compile 'com.github.allenxuan:xuanimageview:0.2.0'
}
```
###Basic use (just like a normal ImageView)
In xml, .e.g.,
```xml
<com.allenxuan.xuanyihuang.xuanimageview.XuanImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/wallpaper1" />
```
In code, e.g.,
```java
XuanImageView xuanImageView = new XuanImageView(context);
xuanImageView.setImageResource(resId);
```


##Available Setters

###Available Setters in code
####setRotationToggle(boolean toggle)
Set a boolean value to determine whether rotation function is turned on.
####setAutoRotateCategory(int category)
Set AutoRotateCategory, there are two alternative values of it : XuanImageViewSettings.AUTO_ROTATE_CATEGORY_RESTORATION, XuanImageViewSettings.AUTO_ROTATE_CATEGORY_MAGNETISM.
####setMaxScaleMultiple(float maxScaleMultiple)
An image is scaled to an InitScale to fit the size of XuanImageView at the very beginning. MaxScale = MaxScaleMultiple * InitScale holds.
####setDoubleTabScaleMultiple(float doubleTabScaleMultiple)
DoubleTapScale = DoubleTabScaleMultiple * InitScale holds. when image's current scale level is smaller than DoubleTabScale, the image will scale up to DoubleTapScale if a double-tap gesture is detected.
####setSpringBackGradientScaleUpLevel(float springBackGradientScaleUpLevel)
If current scale level is smaller than InitScale and image is not in rotation state, the image will scale up to InitScale with SpringBackGradientScaleUpLevel step by step.
Default springBackGradientScaleUpLevel is  1.01f.
####setSpringBackGradientScaleDownLevel(float springBackGradientScaleDownLevel)
If current scale level is bigger than MaxScale and image is not in rotation state, the image will scale down to MaxScale with SpringBackGradientScaleDownLevel step by step.
Default springBackGradientScaleDownLevel is 0.99f.
####setDoubleTapGradientScaleUpLevel(float doubleTapGradientScaleUpLevel)
When image's current scale level is smaller than DoubleTabScale, the image will scale up to DoubleTapScale with DoubleTapGradientScaleUpLevel step by step if a double-tap gesture is detected.
Default doubleTalGradientScaleUpLevel is 1.05f.
####setDoubleTabGradientScaleDownLevel(float doubleTapGradientScaleDownLevel)
When image's current scale level is bigger than DoubleTabScale, the image will scale down to InitScale with DoubleTapGradientScaleDownLevel step by step if a double-tap gesture is detected.
Default doubleTabGradientScaleDownLevel is 0.95f.
####setAutoRotationTrigger(float autoRotationTrigger)
When image's current rotation angle is bigger than AutoRotationTrigger, the image will rotate in the same direction and scale back to it's initial state if rotation gesture is released.
When image's current rotation angle is smaller than AutoRotationTrigger, the image will rotate in the opposite direction and scale back to it's initial state if rotation gesture is released.
Default AutoRotationTrigger is 60 (degrees).
####setSpringBackRunnableDelay(int springBackRunnableDelay)
Default SpringBackRunnableDelay is 10 (milliseconds).
####setDoubleTapScaleRunnableDelay(int delay)
Default DoubleTapScaleRunnableDelay is 10 (milliseconds).
####setAutoRotationRunnableDelay(int delay)
Default AutoRotationRunnableDelay is 5 (milliseconds).
####setAutoRotationRunnableTimes(int times)
Default AutoRotationRunnableTimes is 10 (times).

###Available Setters in xml
```xml
<com.allenxuan.xuanyihuang.xuanimageview.XuanImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/wallpaper1"
        android:scaleType="matrix"
        android:background="@android:color/background_dark"
        app:RotationToggle="boolean value"
        app:AutoRotateCategory="int value (1 for AUTO_ROTATE_CATEGORY_RESTORATION, 2 for AUTO_ROTATE_CATEGORY_MAGNETISM)"
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

##Available Getters (in code)
#### getRotationToggle()
return current RotationToggle.
####getAutoRotateCategory()
return current AutoRotateCategory.
####getMaxScaleMultiple()
Return current MaxScaleMultiple.
####getDoubleTabScaleMultiple()
Return current DoubleTabScaleMultiple.
####getSpringBackGradientScaleUpLevel()
Return current SpringBackGradientScaleUpLevel.
####getSpringBackGradientScaleDownLevel()
Return current SpringBackGradientScaleDownLevel.
####getDoubleTapGradientScaleUpLevel()
Return current DoubleTapGradientScaleUpLevel.
####getDoubleTapGradientScaleDownLevel()
Return current DoubleTapGradientScaleDownLevel.
####getAutoRotationTrigger()
Return current AutoRotationTrigger.
####getSpringBackRunnableDelay()
Return springBackRunnableDelay;
####getDoubleTabScaleRunnableDelay()
Return doubleTabScaleRunnableDelay.
####getAutoRotationRunnalbleDelay()
return current AutoRotationRunnableDelay.
####getAutoRotationRunnableTimes()
Return current AutoRotationRunnableTimes.

#License
```
Copyright 2016 Xuanyi Huang

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
