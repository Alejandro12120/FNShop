package es.alejandro12120.fnshop;

import es.alejandro12120.fnshop.shop.ShopManager;
import lombok.Getter;

import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

public class FNShop {

    @Getter
    public static ShopManager shopManager;

    public static void main(String[] args) {
        setup();

        shopManager = new ShopManager();
    }

    private static void setup() {
        try {
            InputStream fontStream = new BufferedInputStream(new FileInputStream("assets/font.ttf"));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontStream));
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    }

    public static void log(String text) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        System.out.println("[" + dateFormat.format(new Date()) + " LOG] " + text);
    }

    public static void error(String text) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        System.out.println("[" + dateFormat.format(new Date()) + " ERROR] " + text);
    }

    public static void empty() {
        System.out.println(" ");
    }


}
