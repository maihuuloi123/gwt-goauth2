package com.mnouswen.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.mnouswen.shared.UserInfo;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface GreetingService extends RemoteService {
    UserInfo loginWithToken(String token);
    UserInfo loginWithSession();
    void logout();
}
