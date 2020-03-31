package flashcards;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    static Map<String, String> cards = new HashMap<>();
    static Map<String, Integer> mistakes = new HashMap<>();
    static List<String> logs = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException {

        String exportFilePath = null;
        String importFilePath = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-export")) {
                exportFilePath = args[i + 1];
            } else if (args[i].equals("-import")) {
                importFilePath = args[i + 1];
            }
        }

        if (importFilePath != null) {
            importCards(importFilePath);
        }

        logs.add("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
        System.out.println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
        Scanner scan = new Scanner(System.in);
        String action = scan.nextLine();

        while (!action.equals("exit")) {
            switch (action) {
                case "add": {
                    logs.add("add");
                    addCard();
                    break;
                }
                case "remove": {
                    removeCard();
                    break;
                }
                case "ask": {
                    logs.add("ask");
                    ask();
                    break;
                }
                case "import": {
                    System.out.println("File name:");
                    importCards(scan.nextLine());
                    break;
                }
                case "export": {
                    System.out.println("File name:");
                    exportCards(scan.nextLine());
                    break;
                }
                case "log": {
                    saveLog();
                    break;
                }
                case "hardest card": {
                    showHardestCard();
                    break;
                }
                case "reset stats": {
                    resetStats();
                    break;
                }
                default: {
                    System.out.println("Can't find an option, try again.");
                    break;
                }
            }
            System.out.println("\nInput the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            action = scan.nextLine();
        }
        System.out.println("Bye bye!");
        if (exportFilePath != null) {
            exportCards(exportFilePath);
        }
    }

    public static void addCard() {

        System.out.println("The card:");

        Scanner scan = new Scanner(System.in);
        String term = scan.nextLine();

        if (cards.containsKey(term)) {
            System.out.println("The card \"" + term + "\" already exists.");
        } else {
            System.out.println("The definition of card:");
            String definition = scan.nextLine();
            if (cards.containsValue(definition)) {
                System.out.println("The definition \"" + definition + "\" already exists.");
            } else {
                cards.put(term, definition);
                mistakes.put(term, 0);
                System.out.println("The pair (\"" + term + "\":\"" + definition + "\") has been added.");
            }
        }
    }

    public static void removeCard() {

        System.out.println("The card:");

        Scanner scan = new Scanner(System.in);
        String term = scan.nextLine();

        if (cards.containsKey(term)) {
            cards.remove(term);
            mistakes.remove(term);
            System.out.println("The card has been removed.");
        } else {
            System.out.println("Can't remove \"" + term + "\": there is no such card.");
        }
    }

    public static void ask() {

        System.out.println("How many times to ask?");

        Scanner scan = new Scanner(System.in);
        int numberOfAsks = scan.nextInt();
        scan.nextLine();

        for (int i = 0; i < numberOfAsks; i++) {

            Map.Entry<String, String> entry = cards.entrySet()
                    .stream()
                    .skip(new Random().nextInt(cards.entrySet().size()))
                    .findFirst()
                    .orElse(null);
            assert entry != null;
            String term = entry.getKey();
            System.out.println("Print the definition of \"" + term + "\":");
            String definition = scan.nextLine();
            if (definition.equals(cards.get(term))) {
                System.out.println("Correct answer.");
            } else {
                if (cards.containsValue(definition)) {
                    Set<String> set = new HashSet<>(getKeysByValue(cards, definition));
                    String str = "";
                    for (String st : set) {
                        str = st;
                    }
                    System.out.println("Wrong answer. The correct one is \"" + cards.get(term) + "\", " +
                            "you've just written the definition of \"" +
                            str + "\".");
                } else {
                    System.out.println("Wrong answer. The correct one is \"" + cards.get(term) + "\".");
                }
                mistakes.replace(term, mistakes.get(term) + 1);
            }
        }
    }

    public static void importCards(String pathToFile) throws FileNotFoundException {

        File file = new File(pathToFile);

        if (!file.isFile()) {
            System.out.println("File not found.");
        } else {
            try (Scanner scanner = new Scanner(new File(pathToFile))) {
                while(scanner.hasNextLine()) {
                    String term = scanner.nextLine();
                    String description = scanner.nextLine();
                    int numberOfMistakes = Integer.parseInt(scanner.nextLine());
                    cards.put(term, description);
                    mistakes.putIfAbsent(term, numberOfMistakes);
                }
                System.out.println(cards.size() + " cards have been loaded.");
            }
        }
    }

    public static void exportCards(String pathToFile) throws FileNotFoundException {

        File file = new File(pathToFile);

        try (PrintWriter printer = new PrintWriter(file)) {
            cards.keySet().forEach(term -> {
                printer.println(term);
                printer.println(cards.get(term));
                printer.println(mistakes.get(term));
            });
            System.out.println(cards.size() + " cards have been saved.");
        }
    }

    public static void saveLog() throws FileNotFoundException {

        Scanner scan = new Scanner(System.in);

        System.out.println("File name:");
        String pathToFile = scan.nextLine();
        File file = new File(pathToFile);

        try (PrintWriter printer = new PrintWriter(file)) {
            logs.forEach(printer::println);
            System.out.println("The log has been saved.");
        }
    }

    public static void showHardestCard() {

        if (mistakes.values().stream().mapToInt(Integer::intValue).sum() == 0) {
            System.out.println("There are no cards with errors.");
        } else if (maxValueKeys(mistakes).size() > 1) {
            System.out.print("The hardest cards are ");
            for (String key : maxValueKeys(mistakes)) {
                if (key.equals(maxValueKeys(mistakes).get(maxValueKeys(mistakes).size() - 1))) {
                    System.out.print("\"" + key + "\". ");
                }
                else {
                    System.out.print("\"" + key + "\", ");
                }
            }
            System.out.println(" You have " + mistakes.get(keyOfMaxValue(mistakes)) + " errors answering them.");
        } else {
            System.out.println("The hardest card is \"" + keyOfMaxValue(mistakes) + "\". You have "
                    + mistakes.get(keyOfMaxValue(mistakes)) + " errors answering it.");
            System.out.println();
        }
    }

    public static String keyOfMaxValue(Map<String, Integer> map) {
        return Collections.max(map.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public static List<String> maxValueKeys(Map<String, Integer> map) {
        List<String> keysOfMax = new ArrayList<>();
        int maxValue = 0;
        for (String key : map.keySet()) {
            if (map.get(key) > maxValue) {
                maxValue = map.get(key);
            }
        }
        for (String key : map.keySet()) {
            if (map.get(key) == maxValue) {
                keysOfMax.add(key);
            }
        }
        return keysOfMax;
    }

    public static void resetStats() {
        for (String key : mistakes.keySet()) {
            mistakes.replace(key, 0);
        }
        System.out.println("Cards statistics has been reset.");
    }

    public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

}
