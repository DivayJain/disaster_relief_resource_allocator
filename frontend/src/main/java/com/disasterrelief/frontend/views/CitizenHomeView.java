package com.disasterrelief.frontend.views;

import com.disasterrelief.frontend.api.Api;
import com.disasterrelief.frontend.components.AppView;
import com.disasterrelief.frontend.components.Formats;
import com.disasterrelief.frontend.components.J;
import com.disasterrelief.frontend.components.MapGrid;
import com.disasterrelief.frontend.layouts.MainLayout;
import com.disasterrelief.frontend.session.UserSession;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Home page for citizens and volunteers, the only screen outside the staff area. Shows a welcome note and the
 * open shelters that still have room. Staff-only screens are blocked for these roles by AuthGuard and the server.
 */
@Route(value = "home", layout = MainLayout.class)
@PageTitle("DRRC - Home")
public class CitizenHomeView extends AppView {

    private final MapGrid grid;

    public CitizenHomeView(UserSession session, Api api) {
        super(session, api, "Welcome", "Find a nearby relief shelter that still has room.");
        Paragraph note = new Paragraph("Relief requests and disaster operations are handled by official staff. "
                + "In an emergency, call your local emergency number.");
        note.addClassName("page-sub");
        grid = new MapGrid(session)
                .text("Shelter", "name")
                .text("Address", "address")
                .col("Place", s -> J.str(s, "district") + (J.str(s, "state").isEmpty() ? "" : ", " + J.str(s, "state")))
                .col("Free beds", s -> Formats.number(J.lng(s, "freeBeds")))
                .col("Accessible", s -> J.bool(s, "accessible") ? "Yes" : "No")
                .text("Facilities", "facilities")
                .loader(page -> api.page(token(), "/api/shelters/open", page, 20));
        add(note, grid);
    }

    @Override
    protected void enter(BeforeEnterEvent event) {
        grid.reload(0);
    }
}
