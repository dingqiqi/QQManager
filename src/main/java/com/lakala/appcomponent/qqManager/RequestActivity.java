package com.lakala.appcomponent.qqManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RequestActivity extends Activity {

    private BaseUiListener mBaseUiListener = new BaseUiListener();

    private LoginListener mLoginListener = new LoginListener();

    private boolean mIsShared = false;

    private boolean mIsGetUserInfo = true;

    private Handler mHandler = new Handler();

    private String shareTitle;
    private String description;
    private String targetUrl;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (intent != null && intent.getBooleanExtra("isShared", false)) {
            mIsShared = true;

            shareTitle = intent.getStringExtra("shareTitle");
            description = intent.getStringExtra("description");
            targetUrl = intent.getStringExtra("targetUrl");
            imageUrl = intent.getStringExtra("imageUrl");

            //分享到 QQ 空间（无需 QQ 登录）
            if ("qzone".equals(intent.getStringExtra("shareType"))) {
                mIsShared = false;
                shareToQzone(shareTitle, description, targetUrl, imageUrl);
            } else {
                if (!QQManager.mTencent.isSessionValid()) {
                    QQManager.mTencent.login(this, "all", mBaseUiListener);
                } else {
                    shareToQQ(shareTitle, description, targetUrl, imageUrl);
                }
            }

        } else {
            QQManager.mTencent.login(this, "all", mLoginListener);
        }

    }

    /**
     * 分享到QQ好友
     */
    public void shareToQQ(String shareTitle, String description, String url, String imageUrl) {
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, shareTitle);// 标题
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, description);// 摘要
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url);// 内容地址
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);// 网络图片地址　　

        // 分享操作要在主线程中完成
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                QQManager.mTencent.shareToQQ(RequestActivity.this, params, mBaseUiListener);
            }
        });
    }

    /**
     * 分享到QQ空间
     */
    public void shareToQzone(String shareTitle, String description, String targetUrl, String imageUrl) {
        final Bundle qzoneParams = new Bundle();
        qzoneParams.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        qzoneParams.putString(QzoneShare.SHARE_TO_QQ_TITLE, shareTitle);//必填  
        qzoneParams.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, description);
        qzoneParams.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, targetUrl);//必填  

        ArrayList<String> imageUrlList = new ArrayList<>();
        imageUrlList.add(imageUrl);

        qzoneParams.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrlList);

        // 分享操作要在主线程中完成
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                QQManager.mTencent.shareToQzone(RequestActivity.this, qzoneParams, mBaseUiListener);
            }
        });
    }

    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object o) {
            //分享到qq空间
            if (mIsShared) {
                mIsShared = false;

                shareToQQ(shareTitle, description, targetUrl, imageUrl);

            } else {
                if (QQManager.mCallBack != null) {
                    QQManager.mCallBack.onSuccess(o);
                }
                finish();
            }
        }

        @Override
        public void onError(UiError e) {
            ErrorModel model = new ErrorModel();
            if (e != null) {
                model.setCode(e.errorCode);
                model.setMsg(e.errorMessage);
                model.setDetail(e.errorDetail);
            }
            if (QQManager.mCallBack != null) {
                QQManager.mCallBack.onFail(model);
            }
            finish();
        }

        @Override
        public void onCancel() {
            finish();
        }

    }

    private class LoginListener implements IUiListener {

        @Override
        public void onComplete(Object o) {

            if (mIsGetUserInfo) {
                mIsGetUserInfo = false;

                try {
                    JSONObject jsonObject = (JSONObject) o;
                    int ret = jsonObject.getInt("ret");
                    if (ret == 0) {
                        String openID = jsonObject.getString("openid");
                        String accessToken = jsonObject.getString("access_token");
                        String expires = jsonObject.getString("expires_in");
                        QQManager.mTencent.setOpenId(openID);
                        QQManager.mTencent.setAccessToken(accessToken, expires);
                    }

                    getUserInfo();
                } catch (JSONException e) {
                    e.printStackTrace();

                    ErrorModel model = new ErrorModel();
                    model.setCode(-1);
                    model.setMsg(e.getMessage());
                    model.setDetail("登录信息获取失败");

                    if (QQManager.mCallBack != null) {
                        QQManager.mCallBack.onFail(model);
                    }
                    finish();
                }
            } else {
                if (QQManager.mCallBack != null) {
                    QQManager.mCallBack.onSuccess(o);
                }
                finish();
            }

        }

        @Override
        public void onError(UiError e) {
            ErrorModel model = new ErrorModel();
            if (e != null) {
                model.setCode(e.errorCode);
                model.setMsg(e.errorMessage);
                model.setDetail(e.errorDetail);
            }

            if (QQManager.mCallBack != null) {
                QQManager.mCallBack.onFail(model);
            }
            finish();
        }

        @Override
        public void onCancel() {
            finish();
        }

    }

    public void getUserInfo() {
        UserInfo userInfo = new UserInfo(this, QQManager.mTencent.getQQToken());
        userInfo.getUserInfo(mLoginListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Tencent.onActivityResultData(requestCode, resultCode, data, mBaseUiListener);

//        if (requestCode == Constants.REQUEST_API) {
//            if (resultCode == Constants.REQUEST_LOGIN) {
//                Tencent.handleResultData(data, mBaseUiListener);
//            }
//        }

//        if (requestCode == Constants.REQUEST_LOGIN
//                || requestCode == Constants.REQUEST_QQ_SHARE) {
//            Tencent.onActivityResultData(requestCode, resultCode, data, mBaseUiListener);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        QQManager.mTencent.releaseResource();
        QQManager.mTencent = null;
    }
}
