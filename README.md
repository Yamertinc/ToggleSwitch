# ToggleSwitchButton

This project aims to provide a working dual toggle button implementation. 

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-ToggleSwitchButton-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/2439)

![Preview](https://github.com/Aenterhy/ToggleSwitch/blob/master/media/demo.gif)
#Usage

*For a working implementation, Have a look at the Sample Project - app*

1. Include the library as local library project:

	``` groovy
	compile 'co.aenterhy:toggleswitch:1.0.0'
	```

2. Add view to your layout file: 
	``` xml
	<!-- ... -->

    <co.aenterhy.toggleswitch.ToggleSwitchButton
        android:id="@+id/toggle"
        style="@style/ToggleSwitchButton" />

	<!-- ... -->
	```

3. Add to your ```styles.xml```: 
	``` xml

    <style name="ToggleSwitchButton">
        <item name="android:layout_width">wrap_content</item>
        <item name="android:layout_height">wrap_content</item>
        <item name="android:layout_alignParentBottom">true</item>
        <item name="android:layout_alignParentRight">true</item>
        <item name="outerRadius">50dp</item>
        <item name="handleDrawable">@drawable/ic_switch</item>
        <item name="upper">@drawable/ic_camera</item>
        <item name="bottom">@drawable/ic_audio</item>
    </style>
	```

4. Add component handler into your activity or fragment: 

	``` java
        toggle = (ToggleSwitchButton) findViewById(R.id.toggle);
        toggle.setOnTriggerListener(new ToggleSwitchButton.OnTriggerListener() {
            @Override
            public void toggledUp() {
                Toast.makeText(MainActivity.this, "Camera", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void toggledDown() {
                Toast.makeText(MainActivity.this, "Audio", Toast.LENGTH_SHORT).show();
            }
        });
	```

More options will be added soon :)

#Compatibility

  * Android 4.0+

# Changelog

### Version: 1.0

  * Initial Build

## License

    Copyright 2015, Roman Herasymenko

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

