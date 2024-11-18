package za.co.tyaphile;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.bundled.CorsPluginConfig;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import za.co.tyaphile.database.DatabaseManager;
import za.co.tyaphile.routing.APIRouting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ECommerceServer {
    private Javalin server;
    private DatabaseManager database;
    private static final String PAGES_DIR = "/html/";

    private void init() {
        createMockProducts();  // Comment out to disable creating mock items
        JavalinThymeleaf.init(templateEngine());
        server = Javalin.create(cfg -> {
            cfg.http.defaultContentType = "application/json";
            cfg.showJavalinBanner = false;
            cfg.plugins.enableCors(cors -> cors.add(CorsPluginConfig::anyHost));
            cfg.staticFiles.add(PAGES_DIR, Location.CLASSPATH);
        });
    }

    public void start() {
        start(5000);
    }

    public void start(int PORT) {
        server.start(PORT);
    }

    public void stop() {
        database = null;
        server.stop();
    }

    public ECommerceServer() {
        database = new DatabaseManager(":memory:");
//        new DatabaseManager("commerce.db");
        init();
        new APIRouting(server);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop()));
    }

    public static Map<String, Object> getErrorMessage(HttpStatus status, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", status);
        result.put("title", "Error");
        result.put("message", message);
        return result;
    }

    public static void main(String[] args) {
        ECommerceServer server = new ECommerceServer();
        server.start();
    }
//server//
    private TemplateEngine templateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix(PAGES_DIR);
        templateEngine.setTemplateResolver(resolver);
        return templateEngine;
    }

    private void createMockProducts() {
        Map<String, List<Object>> products = new HashMap<>();
        products.put("Designer Concepts Yves Plasma Stand - White 2.2m",
                new ArrayList<>(List.of("The Yves Plasma Stand Brings a Light " +
                "Concept To Your Living Room Through Its Straight Lines And Soft Colors", 6999)));
        products.put("OPPO Find N2 Flip 5G 256GB Dual Sim - Moonlit Purple",
                new ArrayList<>(List.of("OPPO Find N2 Flip 5G\n" +
                        "Discover the tech-savvy, style statement phone that lets you see more in a snap. " +
                        "The larger cover screen expands your view with innovative features, whilst the foldable design" +
                        " reimagines photography possibilities, all in a gorgeous and flawless design that " +
                        "you can pop in your pocket.", 23999)));
        products.put("Volkano SA Travel Plug to UK Plug Traveller Series",
                new ArrayList<>(List.of("There's nothing worse than arriving in a country with different sockets to " +
                        "the Type-M plug standard. Prepare in advance with this handy plug converter. Note, this " +
                        "adapter does not convert voltage, so make sure you use it with devices that have a " +
                        "universal input voltage range.", 149)));
        products.put("NIVEA Radiant & Beauty",
                new ArrayList<>(List.of("NIVEA Radiant & Beauty Even Glow Body Cream with 95% Pure Vitamin C, 400ml", 159)));
        products.put("GARDENA Garden Shower Solo", new ArrayList<>(List.of("Extends to maximum 207 cm in height\n" +
                "Height adjustable lever\n" +
                "Gentle soft spray pattern\n" +
                "Integrated ground spike for easy installation", 369)));
        products.put("GARDENA Rain Water Tank Pump 4700/2 inox", new ArrayList<>(List.of("40 yrs of innovation\n" +
                "Reliable engineering\n" +
                "Global market leader\n" +
                "Safe and reliable\n" +
                "High quality materials\n" +
                "Convenient\n" +
                "Versatile\n" +
                "Eco Friendly", 2199)));
        products.put("Everfurn Work Desk - Anthony Series",
                new ArrayList<>(List.of("A stunningly comprised work desk that boasts ample space with " +
                        "additional storage space. Sturdy and durable, the Anthony Series is also a looker with " +
                        "clean lines and design precision.", 1699)));
        products.put("Brother DCP-T720DW Ink Tank Printer 3in1 with WiFi and ADF",
                new ArrayList<>(List.of("Specifications:\n" +
                        "- Functions: Print, Copy, Scan with Auto two-sided print\n" +
                        "- Connectivity: USB, Wireless\n" +
                        "- Print speed: Up to 17/16.5 ipm (ISO)\n" +
                        "- Paper Input: 150 Sheets and 20 sheet Auto Document Feeder,\n" +
                        "- Single Sheet manual feed slot (max paper gsm - 300gsm)", 5499)));
        products.put("Brother BT5000 / 6000 Combo Ink Bottle Set", new ArrayList<>(List.of("Compatible Printers\n" +
                "\n" +
                "Brother DCP-T300\n" +
                "Brother DCP-T500W\n" +
                "Brother DCP-T700W\n" +
                "Brother MFC-T800W\n" +
                "Black - 115ml/Colours - 45ml\n" +
                "Page Yield - 4000/5 000 pgs @ 5% coverage\n" +
                "High quality printing and page yields equal to Original", 154)));
        products.put("Sugar", new ArrayList<>(List.of("1 kg of sweetener", 24.99)));

        products.forEach((key, info) -> DatabaseManager.addProduct(key, info.get(0).toString(), Double.parseDouble(info.get(1).toString())));
    }
}
