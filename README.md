#Abstract
---
XuanImageView is an extended ImageView with scaling, rotating function and ect.


#Rotating Strategy
---
1. An image will be showed with an InitScale to fit the screen size at the very beginning. The Image can only start to be rotated when it's scale level is InitScale.



# Available Setters
---
##setAutoRotationRunnableDelay(long delay)
default autoRotationRunnableDelay is 10 milliseconds.
##setAutoRotationRunnableTimes(long times)
default autoRotationRunnableTimes is 10.
##setDoubleTapScaleRunnableDelay(long delay)
default doubleTapScaleRunnableDelay is 10 milliseconds.
##setSpringBackGradientScaleUpLevel(float springBackGradientScaleUpLevel)
defalut springBackGradientScaleUpLevel is  1.01f.
##setSpringBackGradientScaleDownLevel(float springBackGradientScaleDownLevel)
default springBackGradientScaleDownLevel is 0.99f.
##setDoubleTapGradientScaleUpLevel(float doubleTapGradientScaleUpLevel)
default doubleTalGradientScaleUpLevel is 1.05f.
##setDoubleTabGradientScaleDownLevel(float doubleTapGradientScaleDownLevel)
default doubleTabGradientScaleDownLevel is 0.95f.
