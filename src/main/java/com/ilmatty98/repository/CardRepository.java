package com.ilmatty98.repository;

import com.ilmatty98.entity.Card;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CardRepository extends VaultRepository<Card> {
}