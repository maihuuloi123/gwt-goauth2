package com.mnouswen.client;

import com.google.api.gwt.oauth2.client.Auth;
import com.google.api.gwt.oauth2.client.AuthRequest;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.mnouswen.shared.UserInfo;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MainStart implements EntryPoint {
    //TODO: update GOOGLE_CLIENT_ID
    private static final String GOOGLE_CLIENT_ID = "332532359760-7qjt73adn8e3v11ibb6oha6ra6vjriuv.apps.googleusercontent.com";
    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String[] PLUS_ME_SCOPE = new String[]{"profile","email"} ;
    private static final Auth AUTH = Auth.get();

    private Logger logger = Logger.getLogger(MainStart.class.getName());
    private final VerticalPanel userInfoPanel = new VerticalPanel();
    private final Button logoutBtn = new Button("Sign out");
    private final Image loginImage = new Image();
    private RootPanel userInfoContainer;
    private RootPanel gSignInButton;
    private RootPanel gSignInPenal;
    /**
     * The message displayed to the user when the server cannot be reached or returns an error.
     */
    private static final String SERVER_ERROR = "An error occurred while "
            + "attempting to contact the server. Please check your network "
            + "connection and try again.";

    private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);
    private Label emailAdressLabel = new Label();
    private Label userNameLabel = new Label();

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {

        userInfoContainer = RootPanel.get("userInfoContainer");
        userInfoContainer.add(userInfoPanel);
        gSignInButton = RootPanel.get("gSignInButton");
        gSignInPenal = RootPanel.get("gSignInPanel");
        //Building user info panel
        userInfoPanel.add(loginImage);
        userInfoPanel.add(userNameLabel);
        userInfoPanel.add(emailAdressLabel);
        userInfoPanel.add(logoutBtn);
        logoutBtn.setStyleName("btn");
        userInfoPanel.setSpacing(4);

        display();

    }

    private void display() {
        String sessionID = Cookies.getCookie("sid");
        if(sessionID == null){
            loadLoginPanel();
        } else {
            greetingService.loginWithSession(new AsyncCallback<UserInfo>() {
                @Override
                public void onFailure(Throwable throwable) {
                    logger.info("Session expired!!!");
                    loadLoginPanel();
                }

                @Override
                public void onSuccess(UserInfo userInfo) {
                    logger.info("Session valid!!!");
                    loadUserInfoPanel(userInfo);
                }
            });
        }
        //login();
    }

    private void loadUserInfoPanel(UserInfo userLoginInfo) {
        logger.log(Level.SEVERE, userLoginInfo.toString());

        userNameLabel.setText("Nick Name: " + userLoginInfo.getName());
        emailAdressLabel.setText("Email: " + userLoginInfo.getEmail());
        logoutBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                greetingService.logout(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        logger.severe("Failed to log out");
                    }
                    @Override
                    public void onSuccess(Void result) {
                        logger.severe("Log out success");
                        loadLoginPanel();
                        Cookies.removeCookie("sid");
                    }
                });
            }
        });
        if (userLoginInfo.getPictureUrl() != null && !userLoginInfo.getPictureUrl().isEmpty()) {
            loginImage.setUrl(userLoginInfo.getPictureUrl());
            loginImage.setVisible(false);
            loginImage.addLoadHandler(new LoadHandler() {
                @Override
                public void onLoad(final LoadEvent event) {
                    final int newWidth = 100;
                    final com.google.gwt.dom.client.Element element = event
                            .getRelativeElement();
                    if (element.equals(loginImage.getElement())) {
                        final int originalHeight = loginImage.getOffsetHeight();
                        final int originalWidth = loginImage.getOffsetWidth();
                        if (originalHeight > originalWidth) {
                            loginImage.setHeight(newWidth + "px");
                        } else {
                            loginImage.setWidth(newWidth + "px");
                        }
                        loginImage.setVisible(true);
                    }
                }
            });
        } else {
            logger.log(Level.SEVERE, "loadUserInfoPanel -> empty url image!");
            loginImage.setUrl("");
            loginImage.setAltText("Profile picture");
        }
        togglePanels(true);
    }

    void togglePanels(boolean isLoggedIn) {
        gSignInPenal.setVisible(!isLoggedIn);
        userInfoContainer.setVisible(isLoggedIn);
    }

    private void loadLoginPanel() {
        gSignInButton.sinkEvents(Event.ONCLICK);
        gSignInButton.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                login();
            }
        }, ClickEvent.getType());
        togglePanels(false);
    }

    private void login() {
        final AuthRequest req = new AuthRequest(GOOGLE_AUTH_URL, GOOGLE_CLIENT_ID)
                .withScopes(PLUS_ME_SCOPE);
        AUTH.clearAllTokens();
        AUTH.login(req, new Callback<String, Throwable>() {
            @Override
            public void onSuccess(final String token) {
                if (!token.isEmpty()) {
                    greetingService.loginWithToken(token, new AsyncCallback<UserInfo>() {
                        @Override
                        public void onFailure(final Throwable caught) {
                            logger.log(Level.SEVERE, "loginWithToken -> onFailure:" + caught);
                        }

                        @Override
                        public void onSuccess(final UserInfo userInfo) {
                            logger.log(Level.SEVERE, "loginWithToken -> onSuccess");
                            loadUserInfoPanel(userInfo);
                            storeSession(userInfo.getSessionId());
                        }
                    });
                }
            }

            @Override
            public void onFailure(final Throwable caught) {
            }
        });
    }

    private void storeSession(String sessionId) {
        final long DURATION = 1000 * 60 * 60 * 24 * 1;
        Date expires = new Date(System.currentTimeMillis() + DURATION);
        Cookies.setCookie("sid", sessionId, expires, null, "/", false);
    }

}
