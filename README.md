# Contact-List-Application
## This application is written in Kotlin. The minimum android SDK version for this app to run is 23
### Features
Link to Play Store : https://play.google.com/store/apps/details?id=com.hashmaps.contact_list_application_assignment

Contact-List app provides users the ability to read existing contacts from their phone, upload existing contacts to their signed up account.
- Users can add new contacts, Edit and Delete them.
- Users will also have a phone and text option, these options will open there default set phone and text application in their phone when clicked.
- The users can also add photos to there contacts and edit them.
- An option to export the contacts to a .csv file.(Currently supports only exporting names and phone number)
- All the data will be saved to the users firebase(Database used) account. A sign page will be available for users to sign up for a free account.

### Coroutine 
We are using Coroutine instead of multi-threading to save data into Firestore once user login.
Learn more about Coroutine at https://developer.android.com/kotlin/coroutines

### Upcoming Feratures that might be added
Add favourite option, to add a favourite contact.
