MediaMultiSelector
===========

This library provides media picker for selecting photos or videos. It can also be used to provide
functionality to play local video files or stream remote video url.

Screenshots

![Alt text](/screenshots/Screenshot_a.png?raw=true "")
![Alt text](/screenshots/Screenshot_b.png?raw=true "")
![Alt text](/screenshots/Screenshot_c.png?raw=true "")
![Alt text](/screenshots/Screenshot_d.png?raw=true "")

Download
--------

```groovy
dependencies {
  compile 'com.droid.mediamultiselector:mediamultiselector:1.0.0'
}
```

###Quick Start

* Add required permissions to your `AndroidManifest.xml`. Internet permission is required for streaming DASH video url.
```xml
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET" />
```
and also the selector activity
```xml
    <activity android:name="com.android.mediamultiselector.activity.MediaSelectorActivity" />
```

* To show media picker
```java
// mediaPathsSelected is the List of String of file paths of previously selected images/videos
MediaSelectorActivity.startActivityForResult(this, REQUEST_CODE_MEDIA_SELECT,
            MediaSelectorActivity.SELECTION_MODE_MULTI, 4, MediaSelectorActivity.MEDIA_TYPE_ALL,
            true, mediaPathsSelected);
```

it will return result of List of String of selected media paths
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_MEDIA_SELECT) {
        mediaPathsSelected = data.getStringArrayListExtra(MediaSelectorActivity.RESULTS_SELECTED_MEDIA);
    }
}
```

## License
Originally forked from [lovetuzitong/MultiImageSelector](https://github.com/lovetuzitong/MultiImageSelector).

Copyright 2016, Roxxy Rafael, 2015, Nereo, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the   License.
You may obtain a copy of the License in the LICENSE file, or at:

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS   IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.