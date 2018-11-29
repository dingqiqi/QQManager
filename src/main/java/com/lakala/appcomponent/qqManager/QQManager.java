package com.lakala.appcomponent.qqManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.tencent.tauth.Tencent;

public class QQManager {

    public static Tencent mTencent;

    public static CallBack mCallBack;

    private String mAppId;

    public QQManager(String mAppId) {
        this.mAppId = mAppId;
    }

    public void login(Activity activity, CallBack callBack) {
        mCallBack = callBack;
        if (mTencent == null) {
            mTencent = Tencent.createInstance(mAppId, activity.getApplicationContext());
        }

        activity.startActivity(new Intent(activity, RequestActivity.class));
    }

    public void logout(Context context) {
        if (mTencent.isSessionValid()) {
            mTencent.logout(context);
        }
    }

    /**
     * 分享到朋友
     *
     * @param shareTitle  分享标题
     * @param description 分享描述
     * @param targetUrl   点击跳转地址
     * @param imageUrl    图片地址
     */
    public void sharedToFriend(Activity activity, String shareTitle, String description,
                               String targetUrl, String imageUrl, CallBack callBack) {

        share(activity, "friend", shareTitle, description,
                targetUrl, imageUrl, callBack);
    }

    /**
     * 分享到朋友圈
     *
     * @param shareTitle  分享标题
     * @param description 分享描述
     * @param targetUrl   点击跳转地址
     * @param imageUrl    图片地址
     */
    public void sharedToQzone(Activity activity, String shareTitle, String description,
                              String targetUrl, String imageUrl, CallBack callBack) {
        share(activity, "qzone", shareTitle, description,
                targetUrl, imageUrl, callBack);

    }

    /**
     * 分享
     *
     * @param shareType   分享类型 朋友or空间
     * @param shareTitle  分享标题
     * @param description 分享描述
     * @param targetUrl   点击跳转地址
     * @param imageUrl    图片地址
     */
    private void share(Activity activity, String shareType, String shareTitle, String description,
                       String targetUrl, String imageUrl, CallBack callBack) {
        mCallBack = callBack;

        if (mTencent == null) {
            mTencent = Tencent.createInstance(mAppId, activity);
        }

        Intent intent = new Intent(activity, RequestActivity.class);
        intent.putExtra("shareType", shareType);
        intent.putExtra("shareTitle", shareTitle);
        intent.putExtra("description", description);
        intent.putExtra("targetUrl", targetUrl);
        intent.putExtra("imageUrl", imageUrl);
        intent.putExtra("isShared", true);
        activity.startActivity(intent);
    }

    public interface CallBack {
        void onSuccess(Object result);

        void onFail(ErrorModel model);
    }
}
