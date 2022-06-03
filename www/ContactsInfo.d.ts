interface Navigator {
    /** Provides access to the device contacts database. */
    contactsInfo: ContactsInfo;
}

interface ContactsInfo {
    /**
     * The navigator.contactsInfo.list method executes asynchronously, querying the device contacts database
     * and returning an array of Contact objects. The resulting objects are passed to the onSuccess
     * callback function specified by the onSuccess parameter.
     * @param onSuccess Success callback function invoked with the array of Contact objects returned from the database
     * @param onError Error callback function, invoked when an error occurs.
     */
    list(onSuccess: (contacts: Contact[]) => void,
        onError?: (error: string) => void): void;
}

interface Contact {
    /** A globally unique identifier. */
    id?: string;
    /** The first name of this Contact. */
    firstName?: string;
    /** The last name of this Contact. */
    lastName?: string;
    /** The name of this Contact, suitable for display to end users. */
    displayName?: string;
    /** An object array of all the contact's phone numbers. */
    phoneNumbers?: ContactPhoneNumber[];
    /** An string array of all the contact's email ids. */
    emailIds?: string[];
}

declare var Contact: {
    /** Constructor of Contact object */
    new(id?: string,
        firstName?: string,
        LastName?: string,
        displayName?: string,
        phoneNumbers?: ContactPhoneNumber[],
        emailIds?: string[]): Contact
};

/** Contains different kinds of information about a Contact object's phone number. */
interface ContactPhoneNumber {
    /** The contact phone number's number. */
    number?: string;
    /** The contact phone number's normalized number. */
    normalizedNumber?: string;
    /** The contact phone number's type (WORK, MOBILE, HOME or OTHER). */
    type?: string;
}

declare var ContactPhoneNumber: {
    /** Constructor for ContactPhoneNumber object */
    new (number?: string,
        normalizedNumber?: string,
        type?: string): ContactPhoneNumber
};
