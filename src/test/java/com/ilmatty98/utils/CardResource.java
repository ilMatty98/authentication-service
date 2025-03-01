package com.ilmatty98.utils;

import com.ilmatty98.entity.Card;
import com.ilmatty98.repository.CardRepository;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/card")
@RequiredArgsConstructor
public class CardResource {

    private final CardRepository cardRepository;

    @GET
    @Path("/{id}")
    public Card get(@PathParam("id") Long id) {
        return cardRepository.findById(id);
    }

    @POST
    @Transactional
    public Card save(@RequestBody Card card) {
        card.setId(null);
        cardRepository.persist(card);
        return card;
    }

    @POST
    @Path("/{id}")
    @Transactional
    public void delete(@PathParam("id") Long id) {
        cardRepository.deleteById(id);
    }
}
