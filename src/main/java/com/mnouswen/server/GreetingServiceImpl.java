package com.mnouswen.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.mnouswen.client.GreetingService;
import com.mnouswen.shared.UserInfo;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
@TransactionManagement(TransactionManagementType.CONTAINER)
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {

    private static Logger log = Logger.getLogger(GreetingServiceImpl.class.getCanonicalName());

    //@PersistenceContext(name="wildfly-example")

    EntityManager em = PersistenceManager.INSTANCE.getEntityManager();

    @Resource
    UserTransaction ut;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public UserInfo loginWithToken(final String token) {
        final UserInfo userInfo = new UserInfo();
        try {
            final StringBuilder response = requestUserInfo(token);

            final JsonFactory f = new JsonFactory();
            JsonParser jp;
            jp = f.createJsonParser(response.toString());
            jp.nextToken();
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                final String fieldname = jp.getCurrentName();
                jp.nextToken();
                log.info(fieldname + ":" + jp.getText());
                if ("picture".equals(fieldname)) {
                    userInfo.setPictureUrl(jp.getText());
                } else if ("name".equals(fieldname)) {
                    userInfo.setName(jp.getText());
                } else if ("email".equals(fieldname)) {
                    userInfo.setEmail(jp.getText());
                }
            }

            saveUser(userInfo);
            //loginWithSession(userInfo);


        } catch (Exception e) {
            log.log(Level.SEVERE, e.toString());
            throw new Error(e);
        }
        return storeUserInSession(userInfo);
    }
    private UserInfo  storeUserInSession(UserInfo user)
    {
        HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
        HttpSession session = httpServletRequest.getSession(true);
        user.setSessionId(session.getId());//add this line to save session id into UserDto
        session.setAttribute("user", user);
        log.info(user.toString());
        return user;
    }
    private void saveUser(UserInfo userInfo) {
        try {
            ut.begin();
            em.merge(userInfo);
            ut.commit();
        } catch (Exception e) {
            log.severe(e.getMessage());
            log.severe(e.getStackTrace().toString());
        }
    }


    @Override
    public UserInfo loginWithSession() {
        return getSessionUser();
    }

    @Override
    public void logout() {
        deleteUserFromSession();
    }

    private UserInfo getSessionUser()
    {
        UserInfo user = null;
        HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
        HttpSession session = httpServletRequest.getSession();
        Object userObj = session.getAttribute("user");
        if (userObj != null && userObj instanceof UserInfo)
        {
            user = (UserInfo) userObj;
            log.info("Login success with session");
        }
        return user;
    }

    private StringBuilder requestUserInfo(String token) throws IOException {
        String userInfoUrlString = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=" + token;
        log.severe("userInfoUrlString: " + userInfoUrlString);
        final StringBuilder response = new StringBuilder();
        final URL userInfoUrl = new URL(userInfoUrlString);
        final URLConnection uc = userInfoUrl.openConnection();
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            isr = new InputStreamReader(uc.getInputStream());
            br = new BufferedReader(isr);
            while ((userInfoUrlString = br.readLine()) != null) {
                response.append(userInfoUrlString).append('\n');
            }

        } finally {
            try {
                br.close();
            } catch (final Exception ex) {
                log.log(Level.SEVERE, ex.toString());
            }
        }

        return response;
    }

    private void deleteUserFromSession() {
        HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
        HttpSession session = httpServletRequest.getSession();
        session.removeAttribute("user");
    }
}
