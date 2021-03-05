package io.github.colemakmods.web;

/**
 * Created by steve on 09/01/18.
 */

public class ResourceStatic {

    public final static Resource[] ALL_LAYOUTS = {
        new Resource("Colemak-DH", "Ergonomic", "layout_full/colemak_dh.keyb"),
        new Resource("Colemak-DHk", "Ergonomic", "layout_full/colemak_dhk.keyb"),
        new Resource("Colemak", "Traditional", "layout_full/colemak.keyb"),
        new Resource("Dvorak", "Traditional", "layout_full/dvorak.keyb"),
        new Resource("Workman", "Traditional", "layout_full/workman.keyb"),
        new Resource("MTGAP", "Traditional", "layout_full/mtgap.keyb"),
        new Resource("qgmlwyfub", "Traditional", "layout_full/qgmlwyfub.keyb"),
        new Resource("Halmak", "Traditional", "layout_full/halmak.keyb"),
        new Resource("Soul", "Alternative", "layout_full/soul.keyb"),
        new Resource("Niro", "Alternative", "layout_full/niro.keyb"),
        new Resource("Asset", "Traditional", "layout_full/asset.keyb"),
        new Resource("Norman", "Traditional", "layout_full/norman.keyb"),
        new Resource("Qwpr", "Traditional", "layout_full/qwpr.keyb"),
        new Resource("Minimak-8", "Traditional", "layout_full/minimak8.keyb"),
        new Resource("Qwerty", "Traditional", "layout_full/qwerty.keyb"),
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
        new Resource("fi", "Finnish data", "freq/practical_cryptography/fi_pc.freq"),
        new Resource("fr", "French data", "freq/practical_cryptography/fr_pc.freq"),
        new Resource("pl", "Polish data", "freq/practical_cryptography/pl_pc.freq"),
        new Resource("sv", "Swedish data", "freq/practical_cryptography/sv_pc.freq")
    };


}
