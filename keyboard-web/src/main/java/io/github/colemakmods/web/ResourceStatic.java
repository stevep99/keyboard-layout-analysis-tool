package io.github.colemakmods.web;

/**
 * Created by steve on 09/01/18.
 */

public class ResourceStatic {

    public final static Resource[] ALL_KEYBOARDS = {
        new Resource("Mod-DH", "", "colemak_dh.keyb"),
        new Resource("Colemak", "", "colemak.keyb"),
        new Resource("Dvorak", "", "dvorak.keyb"),
        new Resource("Workman", "", "workman.keyb"),
        new Resource("MTGAP", "", "mtgap.keyb"),
        new Resource("BEAKL", "", "beakl.keyb"),
        new Resource("Asset", "", "asset.keyb"),
        new Resource("Norman", "", "norman.keyb"),
        new Resource("Qwpr", "", "qwpr.keyb"),
        new Resource("Minimak-8", "", "minimak8.keyb"),
        new Resource("Qwerty", "", "qwerty.keyb"),
    };

    public final static Resource[] ALL_CONFIGS = {
        new Resource("Traditional", "The traditional typing method", "config/effort_traditional_config.dat"),
        new Resource("Alternative", "An alternative typing method", "config/effort_alternative_config.dat"),
        new Resource("Ergonomic", "An ergonomic method using the Angle Mod", "config/effort_ergonomic_config.dat"),
        new Resource("Matrix", "A Matrix or ortholinear colemakmods", "config/effort_matrix_config.dat")
    };

    public final static Resource[] ALL_FREQS = {
        new Resource("en", "Default English data", "freq/en_books.freq"),
        new Resource("en_norvig", "English Data from Peter Norvig, norvig.com/mayzner.html", "freq/en_norvig.freq"),
        new Resource("java", "Data from Java source files", "freq/java.freq"),
        new Resource("da", "Danish data", "freq/practical_cryptography/da_pc.freq"),
        new Resource("de", "German data", "freq/practical_cryptography/de_pc.freq"),
        new Resource("es", "Spanish data", "freq/practical_cryptography/es_pc.freq"),
        new Resource("fr", "French data", "freq/practical_cryptography/fr_pc.freq"),
        new Resource("pl", "Polish data", "freq/practical_cryptography/pl_pc.freq"),
        new Resource("sv", "Swedish data", "freq/practical_cryptography/sv_pc.freq")
    };


}
