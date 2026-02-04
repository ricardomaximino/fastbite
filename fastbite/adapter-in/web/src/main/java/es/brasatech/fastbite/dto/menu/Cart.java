package es.brasatech.fastbite.dto.menu;

import es.brasatech.fastbite.domain.order.CartItem;

import java.util.List;

public record Cart(List<CartItem> cartItemList) {
}
