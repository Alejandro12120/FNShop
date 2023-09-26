package es.alejandro12120.fnshop.shop;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.alejandro12120.fnshop.FNShop;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Getter
public class Shop {

    private String date;
    private String bruteDate;
    private JsonArray featuredArray;
    private JsonArray dailyArray;

    private List<Cosmetic> featured = new ArrayList<>();
    private List<Cosmetic> daily = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    private final String token = "";

    public Shop() {
        try {
            /* Date, FeaturedArray & DailyArray */
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet("https://fortnite-api.com/v2/shop/br");
            httpGet.addHeader("x-api-key", token);

            HttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader(entity.getContent()));
            JsonObject rootobj = root.getAsJsonObject();
            String date = rootobj.getAsJsonObject("data").get("date").getAsString().split("T")[0];
            String[] spplited = date.split("-");

            this.date = spplited[2] + "/" + spplited[1] + "/" + spplited[0];

            this.featuredArray = rootobj.getAsJsonObject("data").getAsJsonObject("featured").getAsJsonArray("entries");
            this.featuredArray.addAll(rootobj.getAsJsonObject("data").getAsJsonObject("specialFeatured").getAsJsonArray("entries")); //merge two arrays

            this.dailyArray = rootobj.getAsJsonObject("data").getAsJsonObject("daily").getAsJsonArray("entries");

            /* Brute Date */
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            this.bruteDate = dateFormat.format(new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }

        getFeaturedArray().forEach(jsonElement -> getFeatured().add(new Cosmetic(jsonElement.getAsJsonObject())));

        getDailyArray().forEach(jsonElement -> getDaily().add(new Cosmetic(jsonElement.getAsJsonObject())));

        createImage();
    }

    /*
    The shop is split in 3 parts.
    - The first part which includes the date and 4 featured and daily items.
    - The second part will be repeated following the next equation:
         - (The amount of daily or featured items - 4) / 4
    - And the final part
     */

    /*
    We need to calculate the final height of the image to create it, and paste there everything.
     */

    public void createImage() {
        try {
            int layers = Math.round((float) Math.ceil((float) (this.getGreatest().size() - 4) / 4));
            int totalWidth = 3840;
            int totalHeight = 1147 + ((layers) * 592) + 80; // first phase height + (layers * mid phase height) + final phase height

            FNShop.log("Creating image... (Layers: " + layers + ". Width: " + totalWidth + ". Height: " + totalHeight + ")");

            BufferedImage temp = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);

            Graphics graphics = temp.getGraphics();

            // first stage
            FNShop.log("Drawing initial phase at X: 0 Y: 0");
            graphics.drawImage(ImageIO.read(new File("assets/shop/initial_phase.png")), 0, 0, null);

            // mid stage
            for (int i = 0; i < layers; i++) {
                FNShop.log("Drawing mid phase (stage " + i + ") at X: 0 Y: " + (1147 + (i * 592)));
                graphics.drawImage(ImageIO.read(new File("assets/shop/mid_phase.png")), 0, 1147 + (i * 592), null);
            }

            // final stage
            FNShop.log("Drawing final phase at X: 0 Y: " + (totalHeight - 80));
            graphics.drawImage(ImageIO.read(new File("assets/shop/final_phase.png")), 0, totalHeight - 80, null);

            // Draw date
            FNShop.log("Drawing date at X: " + (totalWidth / 2 + 100) + " Y: 250");
            graphics.setFont(new Font("Burbank Big Cd Bd", Font.BOLD, 158));
            graphics.drawString(getDate(), (totalWidth / 2 + 100), 250);

            // Draw feature
            FNShop.empty();
            FNShop.log("Drawing featured items (" + this.getFeatured().size() + " items): ");

            int yLocation = 560;
            int index = 0;
            for (int i = 0; i < this.getFeatured().size(); i++) {
                Cosmetic cosmetic = this.getFeatured().get(i);
                int xLocation = (90 + (425 * index));

                FNShop.log("Drawing " + cosmetic.getName() + " at X: " + xLocation + " Y: " + yLocation);

                BufferedImage background = cosmetic.getRarityBackground();

                // Draw images
                graphics.drawImage(background.getScaledInstance(401, 557, Image.SCALE_DEFAULT), xLocation, yLocation, null);
                if (parseIcon(cosmetic.getIcon()) != null)
                    graphics.drawImage(parseIcon(cosmetic.getIcon()).getScaledInstance(420, 420, Image.SCALE_DEFAULT), (401 / 2) - (420 / 2) + xLocation, (390 / 2) - (420 / 2) + yLocation, null);

                // To center the name in the card we are going to use this equation:
                // (Card Width / 2) - (Name Width / 2) + xLocation
                graphics.setFont(new Font("Burbank Big Cd Bd", Font.BOLD, calculateWidthName(graphics, cosmetic)));
                graphics.drawString(cosmetic.getName(), ((401 / 2) - graphics.getFontMetrics().stringWidth(cosmetic.getName()) / 2) + xLocation, yLocation + 485);

                graphics.drawImage(ImageIO.read(new File("assets/vbucks.png")).getScaledInstance(55, 55, Image.SCALE_DEFAULT), (((401 / 2) - graphics.getFontMetrics().stringWidth(String.valueOf(cosmetic.getPrice())) / 2) + xLocation) - 55, yLocation + 500, null);

                graphics.setFont(new Font("Burbank Big Cd Bd", Font.BOLD, 55));
                graphics.drawString(String.valueOf(cosmetic.getPrice()), ((401 / 2) - graphics.getFontMetrics().stringWidth(String.valueOf(cosmetic.getPrice())) / 2) + xLocation, yLocation + 545);

                // Use this to parse layers
                index++;

                if ((i + 1) % 4 == 0) {
                    yLocation += 580;
                    index = 0;
                }
            }

            // Draw daily
            FNShop.empty();
            FNShop.log("Drawing daily items (" + this.getDaily().size() + " items): ");

            yLocation = 560;
            index = 0;
            for (int i = 0; i < this.getDaily().size(); i++) {
                Cosmetic cosmetic = this.getDaily().get(i);
                int xLocation = (2065 + (425 * index));

                FNShop.log("Drawing " + cosmetic.getName() + " at X: " + xLocation + " Y: " + yLocation);

                BufferedImage background = cosmetic.getRarityBackground();

                graphics.drawImage(background.getScaledInstance(401, 557, Image.SCALE_DEFAULT), xLocation, yLocation, null);
                if (parseIcon(cosmetic.getIcon()) != null)
                    graphics.drawImage(parseIcon(cosmetic.getIcon()).getScaledInstance(420, 420, Image.SCALE_DEFAULT), (401 / 2) - (420 / 2) + xLocation, (390 / 2) - (420 / 2) + yLocation, null);

                graphics.setFont(new Font("Burbank Big Cd Bd", Font.BOLD, calculateWidthName(graphics, cosmetic)));
                graphics.drawString(cosmetic.getName(), ((401 / 2) - graphics.getFontMetrics().stringWidth(cosmetic.getName()) / 2) + xLocation, yLocation + 485);

                graphics.drawImage(ImageIO.read(new File("assets/vbucks.png")).getScaledInstance(55, 55, Image.SCALE_DEFAULT), (((401 / 2) - graphics.getFontMetrics().stringWidth(String.valueOf(cosmetic.getPrice())) / 2) + xLocation) - 55, yLocation + 500, null);

                graphics.setFont(new Font("Burbank Big Cd Bd", Font.BOLD, 55));
                graphics.drawString(String.valueOf(cosmetic.getPrice()), ((401 / 2) - graphics.getFontMetrics().stringWidth(String.valueOf(cosmetic.getPrice())) / 2) + xLocation, yLocation + 545);

                index++;
                if ((i + 1) % 4 == 0) {
                    yLocation += 580;
                    index = 0;
                }
            }

            // Saving image
            graphics.dispose();

            FNShop.empty();
            FNShop.log("Saving image at shops/" + this.getBruteDate() + ".png");
            File f = new File("shops/" + this.getBruteDate() + ".png");
            ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();

            ImageOutputStream ios = ImageIO.createImageOutputStream(f);
            writer.setOutput(ios);
            writer.write(temp);

            FNShop.log("Image saved!");
        } catch (IOException ex) {
            FNShop.error("An error ocurred while creating shop image. Error stacktrace:");
            ex.printStackTrace();
        }
    }

    private JsonArray getGreatest() {
        return this.getFeaturedArray().size() > this.getDailyArray().size() ? this.getFeaturedArray() : this.getDailyArray();
    }


    private BufferedImage parseIcon(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

            return ImageIO.read(urlConnection.getInputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private int calculateWidthName(Graphics graphics, Cosmetic cosmetic) {
        int size = 70;
        graphics.setFont(new Font("Burbank Big Cd Bd", Font.BOLD, size));
        while (graphics.getFontMetrics().stringWidth(cosmetic.getName()) > 401 - 5) {
            size--;
            graphics.setFont(new Font("Burbank Big Cd Bd", Font.BOLD, size));
        }

        return size;
    }

}
