package es.brasatech.fastbite.domain.user;

/**
 * Basic record to hold client information for invoices.
 */
public record Customer(
    String taxId,
    String name,
    String address
) {}
