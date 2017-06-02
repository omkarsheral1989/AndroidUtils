package com.sheral.omkar.addcontacts;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class Contact {

  public void addContact(Context context, String name, String number) throws RemoteException, OperationApplicationException {
    ArrayList<ContentProviderOperation> ops = new ArrayList<>();

    ops.add(ContentProviderOperation.newInsert(
        ContactsContract.RawContacts.CONTENT_URI)
        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
        .build());

    //------------------------------------------------------ Names
    ops.add(ContentProviderOperation.newInsert(
        ContactsContract.Data.CONTENT_URI)
        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
        .withValue(ContactsContract.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        .withValue(
            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
            name).build());

    //------------------------------------------------------ Mobile Number
    if (number != null) {
      ops.add(ContentProviderOperation.
          newInsert(ContactsContract.Data.CONTENT_URI)
          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
          .withValue(ContactsContract.Data.MIMETYPE,
              ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
          .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
          .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
              ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
          .build());
    }

    // Asking the Contact provider to create a new contact
    context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
  }

  public void deleteContact(Context ctx, String nameStartingWith) {
    Log.e("qwe", "searching for " + nameStartingWith);
//    Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(nameStartingWith));
//    Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, Uri.encode(nameStartingWith));
//    Cursor cur = ctx.getContentResolver().query(contactUri, null, null, null, null);
    Cursor cur = ctx.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
    try {
      if (cur.moveToFirst()) {
        do {
          String name = cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
          Log.e("qwe", "name = " + name);
          if (name.startsWith(nameStartingWith)) {
            String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
            int delete = ctx.getContentResolver().delete(uri, null, null);
            Log.e("qwe", "deleted row = " + delete);
          }
        } while (cur.moveToNext());
      }

    } catch (Exception e) {
      System.out.println(e.getStackTrace());
    } finally {
      closeCursor(cur);
    }
    Log.e("qwe", "done deleting");
  }

  //  @RequiresPermission(android.Manifest.permission.READ_CONTACTS)
  public ArrayList<String> getContactId(Context context, String displayNameLike) {
    ArrayList<String> ids = new ArrayList<>();
    Cursor cursor = null;
    try {
      cursor = getContactDataCursor(
          context,
          ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME + " LIKE ?",
          new String[]{displayNameLike});
      if (cursor != null) {
        while (cursor.moveToNext()) {
          Log.e("qwe", "name = " + cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)));
          ids.add(cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)));
        }
      }
    } finally {
      if (null != cursor) {
        cursor.close();
      }
    }

    return ids;
  }

  @Nullable
  public String getDisplayName(Context context, String contactId) {
    Cursor cursor = null;
    try {
      cursor = getContactDataCursor(
          context,
          ContactsContract.Data.CONTACT_ID + " = ?",
          new String[]{contactId});
      if (cursor != null) {
        if (cursor.moveToNext()) {
          String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
          Log.e("qwe", "name = " + name);
          return name;
        }
      }
    } finally {
      if (null != cursor) {
        cursor.close();
      }
    }

    return null;
  }

  public List<String> getPhoneNumbers(Context context, String contactId) {
    ArrayList<String> numbers = new ArrayList<>();
    Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    String[] projection = {
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
    String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
    String[] selectionArgs = {contactId};
    Cursor cursor = null;
    try {
      cursor = context.getContentResolver()
          .query(uri,
              projection,
              selection,
              selectionArgs, null);
      if (null != cursor) {
        while (cursor.moveToNext()) {
          String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
          String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
          number = formatPhoneNumber(number);
          Log.e("qwe", String.format("name = %s\tnumber = %s",
              name,
              number));
          numbers.add(number);
        }
      }
    } finally {
      closeCursor(cursor);
    }
    return numbers;
  }

  private String formatPhoneNumber(String number) {
    number = number.trim();

    //remove in between spaces
    StringBuffer stringBuffer = new StringBuffer();
    for(int i=0; i<number.length(); i++) {
      char c = number.charAt(i);
      if (isNumber(c) || c=='+') {
        stringBuffer.append(c);
      }
    }

    number = stringBuffer.toString();

    //remove +91
    if (number.length()==13 && number.startsWith("+91")) {
      number = number.substring(3);
    }

    return number;
  }

  private boolean isNumber(char c) {
    return c >= '0' && c <= '9';
  }

  private void closeCursor(Cursor cursor) {
    if (cursor != null) {
      cursor.close();
    }
  }

  private Cursor getContactDataCursor(Context context, String selection, String[] selectionArguments) {
    ContentResolver contentResolver = context.getContentResolver();
    Uri uri = ContactsContract.Data.CONTENT_URI;
//    Uri uri = ContactsContract.Contacts.CONTENT_URI;
    return contentResolver.query(uri, null, selection, selectionArguments, null);
  }
}
