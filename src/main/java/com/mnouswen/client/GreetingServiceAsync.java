package com.mnouswen.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.mnouswen.shared.UserInfo;

public interface GreetingServiceAsync {

    void loginWithToken(String token, AsyncCallback<UserInfo> async);

    void loginWithSession(AsyncCallback<UserInfo> async);

    void logout(AsyncCallback<Void> async);
}
