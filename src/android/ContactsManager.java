package com.frozenex.cordova.contactsInfo;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.pm.PackageManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.util.Log;

public class ContactsManager extends CordovaPlugin {

    private CallbackContext callbackContext;

    private JSONArray executeArgs;

    private static final int READ_CONTACTS_REQ_CODE = 0;

    public static final String ACTION_LIST_CONTACTS = "list";

    private static final String LOG_TAG = "Contacts Info";

    public ContactsManager() {}

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArray of arguments for the plugin.
     * @param callbackContext   The callback context used when calling back into JavaScript.
     * @return                  True if the action was valid, false otherwise.
     */
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

        this.callbackContext = callbackContext;
        this.executeArgs = args;

        if (ACTION_LIST_CONTACTS.equals(action)) {
            if (cordova.hasPermission(android.Manifest.permission.READ_CONTACTS)) {
                execHelper();
            } else {
                cordova.requestPermission(this, READ_CONTACTS_REQ_CODE, android.Manifest.permission.READ_CONTACTS);
            }
            return true;
        }
        return false;
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                Log.d(LOG_TAG, "Permission denied");
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "User has denied permission"));
                return;
            }
        }
        execHelper();
    }

    private void execHelper() {
        this.cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                callbackContext.success(list());
            }
        });
    }

    private JSONArray list() {
        JSONArray contactsInfo = new JSONArray();
        ContentResolver cr = this.cordova.getActivity().getContentResolver();
        String[] projection = new String[] {
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.MIMETYPE
        };
        
        // Retrieve only the contacts with a phone number at least
        Cursor cursor = cr.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1",
            null,
            ContactsContract.Data.CONTACT_ID + " ASC"
        );

        contactsInfo = populateContactsInfoArray(cursor);
        return contactsInfo;
    }


    /**
     * Creates an array of contacts info from the cursor you pass in
     *
     * @param c            the cursor
     * @return             a JSONArray of contacts
     */
    private JSONArray populateContactsInfoArray(Cursor c) {

        JSONArray contactsInfo = new JSONArray();

        String contactId = null;
        String oldContactId = null;
        boolean newContact = true;
        String mimetype = null;

        JSONObject contact = new JSONObject();
        JSONArray phones = new JSONArray();
        JSONArray emails = new JSONArray();

        try {
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    contactId = c.getString(c.getColumnIndex(ContactsContract.Data.CONTACT_ID));

                    if (c.getPosition() == 0) // If we are in the first row set the oldContactId
                        oldContactId = contactId;

                    // When the contact ID changes we need to push the Contact object to the array of contacts and create new objects.
                    if (!oldContactId.equals(contactId)) {
                        // Populate the Contact object with it's arrays and push the contact into the contacts array
                        contact.put("phoneNumbers", phones);
                        contact.put("emails", emails);
                        contactsInfo.put(contact);
                        // Clean up the objects
                        contact = new JSONObject();
                        phones = new JSONArray();
                        emails = new JSONArray();

                        // Set newContact to true as we are starting to populate a new contact
                        newContact = true;
                    }

                    // When we detect a new contact set the ID. These fields are available in every row in the result set returned.
                    if (newContact) {
                        newContact = false;
                        contact.put("id", contactId);
                    }

                    mimetype = c.getString(c.getColumnIndex(ContactsContract.Data.MIMETYPE)); // Grab the mimetype of the current row as it will be used in a lot of comparisons

                    if (mimetype.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                        contact.put("firstName", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)));
                        contact.put("lastName", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)));
                        contact.put("middleName", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME)));
                        contact.put("displayName", c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                        contact.put("thumbnail", c.getString(c.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)));
                    } else if (mimetype.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                        phones.put(getPhoneNumber(c));
                    } else if (mimetype.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                        emails.put(getEmail(c));
                    }

                    // Set the old contact ID
                    oldContactId = contactId;
                }
                // Push the last contact into the contacts array
                contact.put("phoneNumbers", phones);
                contact.put("emails", emails);
                contactsInfo.put(contact);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        c.close();
        return contactsInfo;
    }

    /**
     * Create a phone number JSONObject
     * @param cursor the current database row
     * @return a JSONObject representing a phone number
     */
    private JSONObject getPhoneNumber(Cursor cursor) throws JSONException {
        JSONObject phoneNumber = new JSONObject();
        String number = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
        String normalizedNumber = cursor.getString(cursor.getColumnIndex(Phone.NORMALIZED_NUMBER));
        phoneNumber.put("number", number);
        phoneNumber.put("normalizedNumber", (normalizedNumber == null) ? number : normalizedNumber);
        phoneNumber.put("type", getPhoneTypeLabel(cursor.getInt(cursor.getColumnIndex(Phone.TYPE))));
        return phoneNumber;
    }

    /**
     * Retrieve the type of the phone number based on the type code
     * @param type the code of the type
     * @return a string in caps representing the type of phone number
     */
    private String getPhoneTypeLabel(int type) {
        String label = "OTHER";
        if (type == Phone.TYPE_HOME)
            label = "HOME";
        else if (type == Phone.TYPE_MOBILE)
            label = "MOBILE";
        else if (type == Phone.TYPE_WORK)
            label = "WORK";
        return label;
    }

    /**
     * Create a email JSONObject
     * @param cursor the current database row
     * @return a JSONObject representing a email
     */
    private JSONObject getEmail(Cursor cursor) throws JSONException {
        JSONObject email = new JSONObject();
        email.put("address", cursor.getString(cursor.getColumnIndex(Email.ADDRESS)));
        email.put("type", getEmailTypeLabel(cursor.getInt(cursor.getColumnIndex(Email.TYPE))));
        return email;
    }

    /**
     * Retrieve the type of the email based on the type code
     * @param type the code of the type
     * @return a string in caps representing the type of email
     */
    private String getEmailTypeLabel(int type) {
        String label = "OTHER";
        if (type == Email.TYPE_HOME)
            label = "HOME";
        else if (type == Email.TYPE_WORK)
            label = "WORK";
        return label;
    }
}