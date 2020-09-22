package io.github.colemakmods.web;

/**
 * Created by steve on 09/01/18.
 */

public class ResourceStatic {

    public final static Resource[] ALL_LAYOUTS = {
        new Resource("> Main Keys", "", ""),
        new Resource("Mod-DH", "", "layout_main/colemak_dh.keyb"),
        new Resource("Colemak", "", "layout_main/colemak.keyb"),
        new Resource("Dvorak", "", "layout_main/dvorak.keyb"),
        new Resource("Workman", "", "layout_main/workman.keyb"),
        new Resource("MTGAP", "", "layout_main/mtgap.keyb"),
        new Resource("qgmlwyfub", "", "layout_main/qgmlwyfub.keyb"),
        new Resource("Asset", "", "layout_main/asset.keyb"),
        new Resource("Norman", "", "layout_main/norman.keyb"),
        new Resource("Qwpr", "", "layout_main/qwpr.keyb"),
        new Resource("Minimak-8", "", "layout_main/minimak8.keyb"),
        new Resource("Qwerty", "", "layout_main/qwerty.keyb"),
        new Resource("", "", ""),

        new Resource("> Full Keyboard", "", ""),
        new Resource("Mod-DH", "", "layout_full/colemak_dh.keyb"),
        new Resource("Colemak", "", "layout_full/colemak.keyb"),
        new Resource("Dvorak", "", "layout_full/dvorak.keyb"),
        new Resource("Workman", "", "layout_full/workman.keyb"),
        new Resource("MTGAP", "", "layout_full/mtgap.keyb"),
        new Resource("qgmlwyfub", "", "layout_full/qgmlwyfub.keyb"),
        new Resource("Asset", "", "layout_full/asset.keyb"),
        new Resource("Norman", "", "layout_full/norman.keyb"),
        new Resource("Qwpr", "", "layout_full/qwpr.keyb"),
        new Resource("Minimak-8", "", "layout_full/minimak8.keyb"),
        new Resource("Qwerty", "", "layout_full/qwerty.keyb"),
        new Resource("", "", ""),

    };

    public final static Resource[] ALL_LAYOUTS_FULL = {
    };

    public final static Resource[] ALL_CONFIGS = {
        new Resource("Traditional", "The traditional typing method", "config/effort_traditional_config.dat"),
        new Resource("Alternative", "An alternative typing method", "config/effort_alternative_config.dat"),
        new Resource("Ergonomic", "An ergonomic method using the Angle Mod", "config/effort_ergonomic_config.dat"),
        new Resource("Matrix", "A Matrix or ortholinear colemakmods", "config/effort_matrix_config.dat")
    };

    public final static Resource[] ALL_FREQS = {
        new Resource("en", "Default English data", "freq/en_books.freq"),
        new Resource("en_norvig", "English Data from\n  Peter Norvig, norvig.com/mayzner.html", "freq/en_norvig.freq"),
        new Resource("en_wikipedia", "Data from the 100 most popular\n  Wikipedia articles", "freq/en_wikipedia.freq"),
        new Resource("linux_src", "Data from the Linux source code", "freq/linux_src.freq"),
        new Resource("da", "Danish data", "freq/practical_cryptography/da_pc.freq"),
        new Resource("de", "German data", "freq/practical_cryptography/de_pc.freq"),
        new Resource("es", "Spanish data", "freq/practical_cryptography/es_pc.freq"),
        new Resource("fr", "French data", "freq/practical_cryptography/fr_pc.freq"),
        new Resource("pl", "Polish data", "freq/practical_cryptography/pl_pc.freq"),
        new Resource("sv", "Swedish data", "freq/practical_cryptography/sv_pc.freq")
    };


}
