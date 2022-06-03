# Cordova Plugin Contacts Info
Cross-platform plugin for Cordova / PhoneGap to list all the contacts with at least a phone number along with email address. It is a fork of [cordova-plugin-contacts-phonenumbers](https://github.com/dbaq/cordova-plugin-contacts-phone-numbers).

## Installing the plugin ##
```
cordova plugin add cordova-plugin-contacts-info
```
or use this repository (unstable)
```
cordova plugin add https://github.com/frozenex/cordova-plugin-contacts-info.git
```

### iOS Quirks

Since iOS 10 it's mandatory to provide an usage description in the `info.plist` if trying to access privacy-sensitive data. When the system prompts the user to allow access, this usage description string will displayed as part of the permission dialog box, but if you didn't provide the usage description, the app will crash before showing the dialog. Also, Apple will reject apps that access private data but don't provide an usage description.

 This plugins requires the following usage description:

 * `NSContactsUsageDescription` describes the reason that the app accesses the user's contacts.

 To add this entry into the `info.plist`, you can use the `edit-config` tag in the `config.xml` like this:

```
<edit-config target="NSContactsUsageDescription" file="*-Info.plist" mode="merge">
    <string>need contacts access to search friends</string>
</edit-config>
```

## Using the plugin ##
The plugin creates the object `navigator.contactsPhoneNumbers` with the methods

  `list(success, fail)`
  
Usage in typescript file

`declare let navigator: any;`

A full example could be:

```
   // after deviceready
   navigator.contactsInfo.list(function(contacts) {
      console.log(contacts.length + ' contacts found');
      for(var i = 0; i < contacts.length; i++) {
         console.log(contacts[i].id + " - " + contacts[i].displayName);
         for(var j = 0; j < contacts[i].phoneNumbers.length; j++) {
            var phone = contacts[i].phoneNumbers[j];
            console.log("===> " + phone.type + "  " + phone.number + " (" + phone.normalizedNumber+ ")");
         }
         for(var j = 0; j < contacts[i].emailIds.length; j++) {
            var emailId = contacts[i].emailIds[j];
            console.log("===> " + emailId);
         }
      }
   }, function(error) {
      console.error(error);
   });

```

## JSON Response format

The success callback function contains an array of contacts.

Each entry contains:

   * the unique contact id
   * the name of the contact (first name, last name, display name)
   * an array containing the number, the normalizedNumber and the type of the number (```WORK```, ```MOBILE```, ```HOME``` or ```OTHER```)

Here is a sample of what you can get:

```
    [{
        "id": "1",
        "firstName": "Kate",
        "middleName": "",
        "lastName": "Bell",
        "displayName": "Kate Bell",
        "thumbnail": null,
        "phoneNumbers": [{
            "number": "(555) 564-8583",
            "normalizedNumber": "(555) 564-8583",
            "type": "MOBILE"
        }, {
            "number": "(415) 555-3695",
            "normalizedNumber": "(415) 555-3695",
            "type": "OTHER"
        }],
        "emailIds": ["email@example.com"]
    }, {
        "id": "2",
        "firstName": "Daniel",
        "middleName": "",
        "lastName": "Higgins",
        "displayName": "Daniel Higgins",
        "thumbnail": null,
        "phoneNumbers": [{
            "number": "555-478-7672",
            "normalizedNumber": "555-478-7672",
            "type": "HOME"
        }, {
            "number": "(408) 555-5270",
            "normalizedNumber": "(408) 555-5270",
            "type": "MOBILE"
        }, {
            "number": "(408) 555-3514",
            "normalizedNumber": "(408) 555-3514",
            "type": "OTHER"
        }],
        "emailIds": ["email@example.com"]
    }, {
        "id": "3",
        "firstName": "John",
        "middleName": "Paul",
        "lastName": "Appleseed",
        "displayName": "John Paul Appleseed",
        "thumbnail": "content://com.android.contacts/contacts/49/photo",
        "phoneNumbers": [{
            "number": "888-555-5512",
            "normalizedNumber": "888-555-5512",
            "type": "MOBILE"
        }, {
            "number": "888-555-1212",
            "normalizedNumber": "888-555-1212",
            "type": "HOME"
        }],
        "emailIds": ["email@example.com"]
    }]
```

## Behaviour

The plugin retrieves **ONLY** the contacts containing one or more phone numbers. It does not allow to modify them (use [the official cordova contacts plugin for that](https://github.com/apache/cordova-plugin-contacts)).

With the official plugin, it is difficult and inefficient[1] to retrieve the list of all the contacts with at least a phone number (for Android at least). I needed a fastest way to retrieve a simple list containing just the name and the list of phone numbers, email ids.

## iOS and Android

The plugin works with iOS and Android.

iOS does not provide a normalized number like Android. So number === normalizedNumber for iOS.

The thumbnail is not returned on iOS, if you want iOS support, feel free to open a PR.

The Android code is heavily inspired from the official plugin with some tweaks to improve the performances.

## Licence ##

The MIT License

Copyright (c) 2013 Didier Baquier

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
