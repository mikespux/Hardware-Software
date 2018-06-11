package com.easyweigh.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.easyweigh.app.AppConst;

/**
 * Created by Michael on 8/19/2015.
 */
public class UserPreference {

    public static final float text_size_message = 13.0F;
    public static final float text_size_conversation_message = 13.0F;
    public static final float text_size_conversation_name = 17.0F;
    public static final String text_color_conversation_message = "#727272";
    public static final String text_color_conversation_name = "#212121";
    public static final String background_messages = "#FFFFFF";
    public static SharedPreferences userPreference(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static float getTextSizeMessage(Context context) {
        return Float.parseFloat(userPreference(context).getString(AppConst.text_size_message_key, "" + text_size_message));
    }
    //background  color of messaging activity
    public static String getBackgroundMessages(Context context) {
        return (userPreference(context).getString(AppConst.background_messages_key, "" + background_messages));
    }
    //Text color of Body and date of conversation list
    public static String getTextColorConversationBody(Context context) {
        return (userPreference(context).getString(AppConst.text_color_conversation_message_key, "" + text_color_conversation_message));
    }
    //Name  color
    public static String getTextColorConversationName(Context context) {
        return (userPreference(context).getString(AppConst.text_color_conversation_name_key, "" + text_color_conversation_name));
    }
    //Text size of Body and date of conversation list
    public static float getTextSizeConversationBody(Context context) {
        return Float.parseFloat(userPreference(context).getString(AppConst.text_size_conversation_message_key, "" + text_size_conversation_message));
    }
    //Name color
    public static float getTextSizeConversationName(Context context) {
        return Float.parseFloat(userPreference(context).getString(AppConst.text_size_conversation_name_key, "" + text_size_conversation_name));
    }
  // Hide keyboard
    public static boolean hideKeyboardIsEnabled(Context context) {
        return userPreference(context).getBoolean(AppConst.hide_keyboard_key, true);
    }

    // visibility of avatar in messages list
    public static boolean AvatarMessages(Context context) {
        return userPreference(context).getBoolean(AppConst.avatar_messages_visibility_key, true);
    }
    // visibility of avatar in toolbar
    public static boolean AvatarToolbar(Context context) {
        return userPreference(context).getBoolean(AppConst.avatar_toolbar_visibility_key, true);
    }
    // visibility of avatar in conversation list
    public static boolean AvatarConversation(Context context) {
        return userPreference(context).getBoolean(AppConst.avatar_conversation_visibility_key, true);
    }
    //Animation Y of conversation list
    public static boolean setAnimationY(Context context) {
        return userPreference(context).getBoolean(AppConst.set_animation_y_key, true);
    }
    //Floating button
    public static boolean fabButtonAnimation(Context context) {
        return userPreference(context).getBoolean(AppConst.fab_button_key, true);
    }
    //Ads visibility
    public static boolean adsVisibility(Context context) {
        return userPreference(context).getBoolean(AppConst.ads_visibility_key, true);
    }
    //set  first Upper letter
    public static boolean firstUpperLetterIsEnabled(Context context) {
        return userPreference(context).getBoolean(AppConst.first_uppercase_key, true);
    }

    //Enabling Feeds notification
    public static boolean notificationFeedsIsEnabled(Context context){
        return userPreference(context).getBoolean(AppConst.receiver_notification_feeds, true);
    }
    //Enabling messages notification
    public static boolean notificationMessagesIsEnabled(Context context){
        return userPreference(context).getBoolean(AppConst.receiver_notification_messages, true);
    }
    // Change Status
    public static boolean ChangeStatus(Context context) {
        return userPreference(context).getBoolean(AppConst.change_status, true);
    }
}
