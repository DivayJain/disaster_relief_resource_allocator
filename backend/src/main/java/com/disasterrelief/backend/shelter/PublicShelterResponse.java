package com.disasterrelief.backend.shelter;

/** What a citizen may see about an open shelter: where it is and whether it has room. No occupancy or coordinates. */
public record PublicShelterResponse(String name, String address, String district, String state, int freeBeds,
                                    boolean accessible, String facilities) {

    static PublicShelterResponse from(ShelterResponse s) {
        return new PublicShelterResponse(s.name(), s.address(), s.district(), s.state(), s.freeBeds(),
                s.accessible(), s.facilities());
    }
}
