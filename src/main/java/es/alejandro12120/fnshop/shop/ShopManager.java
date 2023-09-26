package es.alejandro12120.fnshop.shop;

import lombok.Getter;

@Getter
public class ShopManager {

    private Shop shop;

    public ShopManager() {
        this.shop = new Shop();
    }

}
