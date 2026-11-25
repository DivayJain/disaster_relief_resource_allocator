package com.disasterrelief.frontend.security;

import com.disasterrelief.frontend.session.UserSession;
import com.disasterrelief.frontend.views.CitizenHomeView;
import com.disasterrelief.frontend.views.LoginView;
import com.disasterrelief.frontend.views.RegisterView;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/** Every page except the login and sign-up pages requires a valid login; otherwise the user is sent to the login page. */
@Component
public class AuthGuard implements VaadinServiceInitListener {

    private final UserSession session;

    public AuthGuard(UserSession session) {
        this.session = session;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> uiEvent.getUI().addBeforeEnterListener(this::beforeEnter));
    }

    void beforeEnter(BeforeEnterEvent event) {
        Class<?> target = event.getNavigationTarget();
        boolean publicPage = LoginView.class.equals(target) || RegisterView.class.equals(target);
        if (!publicPage && session.current().isEmpty()) {
            event.forwardTo(LoginView.class);
            return;
        }
        // Citizens and volunteers have one screen; every staff screen is out of reach for them (the server
        // refuses their calls too, this just avoids sending them to a page that can only show an error).
        if (!publicPage && !CitizenHomeView.class.equals(target)
                && session.current().isPresent() && !Roles.in(session.user().role(), Roles.STAFF)) {
            event.forwardTo(CitizenHomeView.class);
        }
    }
}
