package com.disasterrelief.frontend.views;

import com.disasterrelief.frontend.api.AuthClient;
import com.disasterrelief.frontend.api.BackendException;
import com.disasterrelief.frontend.components.Feedback;
import com.disasterrelief.frontend.session.UserSession;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/** Public sign-up page. Creates a citizen or volunteer account and signs the user straight in. */
@Route("register")
@PageTitle("DRRC - Sign up")
public class RegisterView extends VerticalLayout {

    private static final int MIN_PASSWORD = 8;

    private final AuthClient authClient;
    private final UserSession session;

    private final TextField fullName = new TextField("Full name");
    private final EmailField email = new EmailField("Email");
    private final Select<String> role = new Select<>();
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirm = new PasswordField("Confirm password");
    private final Paragraph error = new Paragraph();
    private final Button submit = new Button("Sign up");

    public RegisterView(AuthClient authClient, UserSession session) {
        this.authClient = authClient;
        this.session = session;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        role.setLabel("Account type");
        role.setItems("CITIZEN", "VOLUNTEER");
        role.setItemLabelGenerator(value -> "CITIZEN".equals(value) ? "Citizen" : "Volunteer");
        role.setValue("CITIZEN");
        password.setHelperText("At least " + MIN_PASSWORD + " characters");
        for (HasSize field : new HasSize[] {fullName, email, role, password, confirm}) {
            field.setWidth("20rem");
        }
        fullName.setRequiredIndicatorVisible(true);
        fullName.setAutofocus(true);
        email.setRequiredIndicatorVisible(true);
        password.setRequiredIndicatorVisible(true);
        confirm.setRequiredIndicatorVisible(true);
        error.getStyle().set("color", "var(--lumo-error-text-color)");
        error.setVisible(false);
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.setWidth("20rem");
        submit.addClickShortcut(Key.ENTER);
        submit.addClickListener(event -> attemptRegister());

        add(new H1("Create your account"),
                new Paragraph("Sign up as a citizen or volunteer. Staff accounts are issued by an administrator."),
                fullName, email, role, password, confirm, error, submit,
                new Paragraph("Already have an account?"), new RouterLink("Log in", LoginView.class));
    }

    private void attemptRegister() {
        error.setVisible(false);
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            show("Fill in all fields.");
            return;
        }
        if (email.isInvalid()) {
            show("Enter a valid email address.");
            return;
        }
        if (password.getValue().length() < MIN_PASSWORD) {
            show("Password must be at least " + MIN_PASSWORD + " characters.");
            return;
        }
        if (!password.getValue().equals(confirm.getValue())) {
            show("Passwords do not match.");
            return;
        }
        submit.setEnabled(false);
        try {
            session.login(authClient.register(fullName.getValue().trim(), email.getValue().trim(),
                    password.getValue(), role.getValue()));
            password.clear();
            confirm.clear();
            UI.getCurrent().navigate("");
        } catch (BackendException ex) {
            password.clear();
            confirm.clear();
            show(Feedback.message(ex));
        } finally {
            submit.setEnabled(true);
        }
    }

    private void show(String text) {
        error.setText(text);
        error.setVisible(true);
    }
}
