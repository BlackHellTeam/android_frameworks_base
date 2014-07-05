/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.telecomm;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.Rlog;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.MissingResourceException;

/**
 * Represents a distinct account, line of service or call placement method that
 * the system can use to place phone calls.
 */
public class PhoneAccount implements Parcelable {

    private static final int NO_DENSITY = -1;

    private static final String LOG_TAG = "Account";

    private final ComponentName mComponentName;
    private final String mId;
    private final Uri mHandle;
    private final int mLabelResId;
    private final int mShortDescriptionResId;
    private final int mIconResId;
    private final boolean mIsEnabled;
    private final boolean mIsSystemDefault;

    public PhoneAccount(
            ComponentName componentName,
            String id,
            Uri handle,
            String label,
            String shortDescription,
            boolean isEnabled,
            boolean isSystemDefault) {
        mComponentName = componentName;
        mId = id;
        mHandle = handle;
        mLabelResId = 0;  // labelResId;
        mShortDescriptionResId = 0;  // shortDescriptionResId;
        mIconResId = 0;  // iconResId;
        mIsSystemDefault = isSystemDefault;
        mIsEnabled = isEnabled;
    }

    /**
     * The {@code ComponentName} of the {@link android.telecomm.ConnectionService} which is
     * responsible for making phone calls using this {@code PhoneAccount}.
     *
     * @return A suitable {@code ComponentName}.
     */
    public ComponentName getComponentName() {
        return mComponentName;
    }

    /**
     * A unique identifier for this {@code PhoneAccount}, generated by and meaningful to the
     * {@link android.telecomm.ConnectionService} that created it.
     *
     * @return A unique identifier for this {@code PhoneAccount}.
     */
    public String getId() {
        return mId;
    }

    /**
     * The handle (e.g., a phone number) associated with this {@code PhoneAccount}. This represents
     * the destination from which outgoing calls using this {@code PhoneAccount} will appear to come
     * from, if applicable, and the destination to which incoming calls using this
     * {@code PhoneAccount} may be addressed.
     *
     * @return A handle expressed as a {@code Uri}, for example, a phone number.
     */
    public Uri getHandle() {
        return mHandle;
    }

    /**
     * A short string label describing this {@code PhoneAccount}.
     *
     * @param context The invoking {@code Context}, used for retrieving resources.
     *
     * @return A label for this {@code PhoneAccount}.
     */
    public String getLabel(Context context) {
        return getString(context, mLabelResId);
    }

    /**
     * A short paragraph describing this {@code PhoneAccount}.
     *
     * @param context The invoking {@code Context}, used for retrieving resources.
     *
     * @return A description for this {@code PhoneAccount}.
     */
    public String getShortDescription(Context context) {
        return getString(context, mShortDescriptionResId);
    }

    /**
     * An icon to represent this {@code PhoneAccount} in a user interface.
     *
     * @param context The invoking {@code Context}, used for retrieving resources.
     *
     * @return An icon for this {@code PhoneAccount}.
     */
    public Drawable getIcon(Context context) {
        return getIcon(context, mIconResId, NO_DENSITY);
    }

    /**
     * An icon to represent this {@code PhoneAccount} in a user interface.
     *
     * @param context The invoking {@code Context}, used for retrieving resources.
     * @param density A display density from {@link DisplayMetrics}.
     *
     * @return An icon for this {@code PhoneAccount}.
     */
    public Drawable getIcon(Context context, int density) {
        return getIcon(context, mIconResId, density);
    }

    /**
     * Whether this {@code PhoneAccount} is enabled for use.
     *
     * @return {@code true} if this {@code PhoneAccount} is enabled.
     */
    public boolean isEnabled() {
        return mIsEnabled;
    }

    /**
     * Whether this {@code PhoneAccount} is the system default.
     *
     * @return {@code true} if this {@code PhoneAccount} is the system default.
     */
    public boolean isSystemDefault() {
        return mIsSystemDefault;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(mComponentName, flags);
        out.writeString(mId);
        out.writeString(mHandle != null ? mHandle.toString() : "");
        out.writeInt(mLabelResId);
        out.writeInt(mShortDescriptionResId);
        out.writeInt(mIconResId);
        out.writeInt(mIsEnabled ? 1 : 0);
        out.writeInt(mIsSystemDefault ? 1 : 0);
    }

    public static final Creator<PhoneAccount> CREATOR
            = new Creator<PhoneAccount>() {
        public PhoneAccount createFromParcel(Parcel in) {
            return new PhoneAccount(in);
        }

        public PhoneAccount[] newArray(int size) {
            return new PhoneAccount[size];
        }
    };

    private PhoneAccount(Parcel in) {
        mComponentName = in.readParcelable(getClass().getClassLoader());
        mId = in.readString();
        String uriString = in.readString();
        mHandle = uriString.length() > 0 ? Uri.parse(uriString) : null;
        mLabelResId = in.readInt();
        mShortDescriptionResId = in.readInt();
        mIconResId = in.readInt();
        mIsEnabled = in.readInt() == 1;
        mIsSystemDefault = in.readInt() == 1;
    }

    private String getString(Context context, int resId) {
        Context packageContext;
        try {
            packageContext = context.createPackageContext(mComponentName.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            if (Rlog.isLoggable(LOG_TAG, Log.WARN)) {
                Rlog.w(LOG_TAG, "Cannot find package " + mComponentName.getPackageName());
            }
            return null;
        }
        String result = packageContext.getString(resId);
        if (result == null && Rlog.isLoggable(LOG_TAG, Log.WARN)) {
            Rlog.w(LOG_TAG, "Cannot find string " + resId + " in package " +
                    mComponentName.getPackageName());
        }
        return result;
    }

    private Drawable getIcon(Context context, int resId, int density) {
        Context packageContext;
        try {
            packageContext = context.createPackageContext(mComponentName.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            if (Rlog.isLoggable(LOG_TAG, Log.WARN)) {
                Rlog.w(LOG_TAG, "Cannot find package " + mComponentName.getPackageName());
            }
            return null;
        }
        try {
            return density == NO_DENSITY ?
                    packageContext.getResources().getDrawable(resId) :
                    packageContext.getResources().getDrawableForDensity(resId, density);
        } catch (MissingResourceException e) {
            Rlog.e(LOG_TAG, "Cannot find icon " + resId + " in package " +
                    mComponentName.getPackageName() + ": " + e.toString());
            return null;
        }
    }
}