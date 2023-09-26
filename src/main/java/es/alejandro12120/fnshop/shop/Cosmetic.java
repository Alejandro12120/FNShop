package es.alejandro12120.fnshop.shop;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Getter
public class Cosmetic {

    private int price;
    private String name;
    private String rarity;
    private String icon;

    public Cosmetic(JsonObject object) {
        JsonObject item0 = object.get("items").getAsJsonArray().get(0).getAsJsonObject();

        this.price = object.get("finalPrice").getAsInt();
        this.name = item0.get("name").getAsString();
        this.rarity = item0.get("rarity").getAsJsonObject().get("value").getAsString();
        this.icon = item0.get("images").getAsJsonObject().get("featured").isJsonNull() ? item0.get("images").getAsJsonObject().get("icon").getAsString() : item0.get("images").getAsJsonObject().get("featured").getAsString();
    }

    @SneakyThrows
    public BufferedImage getRarityBackground() {
        try {
            return ImageIO.read(new File("assets/rarities/" + this.rarity + ".png"));
        } catch (IOException e) {
            // This image always exists so..
            return ImageIO.read(new File("assets/rarities/common.png"));
        }
    }

}
