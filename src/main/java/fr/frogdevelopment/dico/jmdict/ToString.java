package fr.frogdevelopment.dico.jmdict;

public class ToString {

    public static void main(String[] args) {
        try {
            long start = System.currentTimeMillis();

            JMDictFetcher parser = new JMDictFetcher();
            parser.fetch();
            System.out.println("data fetched in " + (System.currentTimeMillis() - start) + "ms");

            parser.write("d:/Temp/");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
