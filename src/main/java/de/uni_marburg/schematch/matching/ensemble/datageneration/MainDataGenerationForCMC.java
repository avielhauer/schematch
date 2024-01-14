package de.uni_marburg.schematch.matching.ensemble.datageneration;

public class MainDataGenerationForCMC {

    public static void main(String[] args) {
        /*  TODO:
                hier muss die Main-Methode erstellt werden, in der die Datengenerierung umgesetzt wird.

            Ablauf (vorerst):
                Das Schematch-Framework muss, bevor die Datengenerierung angestoßen wird, mit den in Matchern erwähnten Matcher laufen.
                Ziel ist es, dass die Main-Methode die Sim und Ground-Truth Werte des letzten laufes nimmt (Gedanke: um irgendwie die Matcher in einem eilesbaren Dateiformat hinterlegen können. (villeicht YAML?))
                und diese als Vergleichsgrundlage für die (TODO hier Name für die Datentabelle einfügen)
                nimmt.
         */
        LoadData.getSimilaritiesFromResults();
    }
}
