# BetterVent
A tablet-based reservation app written for Computer Science House.
<br>
<img src="https://raw.githubusercontent.com/WillNilges/BetterVent/master/app/src/main/res/mipmap-hdpi/logo.png" width="200" height="200">

# What is it?
BetterVent is a lightweight, open source, customizable android app that shows the status of conference rooms.
It has three main functions:
- Display the current event happening in a room as well as when the next event is

View when the room is free:

<img src="https://raw.githubusercontent.com/WillNilges/BetterVent/master/BetterVent_Screenshots/Screenshot_20190121_042312.png" width="50%" height="50%">

<img src="https://raw.githubusercontent.com/WillNilges/BetterVent/master/BetterVent_Screenshots/Screenshot_20190302-044311_BetterVent.jpg" width="50%" height="50%">

View when the room is reserved:

<img src="https://raw.githubusercontent.com/WillNilges/BetterVent/master/BetterVent_Screenshots/Screenshot_20190121_042156.png" width="50%" height="50%">

- Show a week-view interface of events for the next seven days

<img src="https://raw.githubusercontent.com/WillNilges/BetterVent/master/BetterVent_Screenshots/Screenshot_20190121_042504.png" width="50%" height="50%">

- A quick-mode function for ad-hoc events. Has an editable title field, as well as a name-list for queuing or attendance purposes

<img src="https://raw.githubusercontent.com/WillNilges/BetterVent/master/BetterVent_Screenshots/Screenshot_20190302-045308_BetterVent.jpg" width="50%" height="50%">

<img src="https://raw.githubusercontent.com/WillNilges/BetterVent/master/BetterVent_Screenshots/Screenshot_20190302-045456_BetterVent.jpg" width="50%" height="50%">

# How do I get it?
Currently, BetterVent is not on the Play Store, but you can download the .apk file in the releases tab.
(I'll try to keep it up to date)

# Future Features
- Quality of life changes for Quick Mode
  - Add confirmation when leaving the fragment
  - Add button to clear Quick Mode without leaving the fragment
- Anti tampering
  - Require a pattern of clicks on the escape squares to activate
- Settings panel
  - Ability to filter events by keyword
    - Better parsing of event keywords
    - Set keywords that usually pertain to a location
  - Colors
  
## Device Admin
To set the app as device admin (You need to do this before kiosk features work (Thanks, Google)) Connect to a computer and in the terminal (after installing adb) do this *BEFORE SETTING UP A GOOGLE ACCOUNT*:

```
adb shell
dpm set-device-owner --user current edu.rit.csh.bettervent/.AdminReceiver                                            
```

## Setting up the API for development
If you want to develop for this app, you're going to have to set up your own dev environment. That involves getting the API set up properly. If you want to know how to do that, you can find instructions on it in here: https://github.com/WillNilges/CalendarQuickStart

Also, this command will be useful: keytool -alias androiddebugkey -keystore ~/.android/debug.keystore -list -v 

I will update this page with more detailed instructions before I die (probably).
