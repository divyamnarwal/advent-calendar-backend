package com.divyam.advent.dto;

/** Raw social handles/URLs as entered; the client normalizes them to full links. */
public record SocialLinksDto(
        String vk,
        String telegram,
        String whatsapp,
        String instagram,
        String twitter
) {}
