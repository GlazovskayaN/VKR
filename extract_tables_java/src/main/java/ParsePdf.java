import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsePdf {
    private HashMap<String, String> oldWords = new HashMap<>();
    private HashMap<String, String> newWords = new HashMap<>();
    public List<String> preprocessingTextForTableMedicines(String text) throws UnsupportedEncodingException {
        oldWords = loadDictionary("wordDictionary.txt");
        String regex1 = "(?<=СКД)[\\s\\S]*?(?=\\r?\\n\\s*\\r?\\n*(4\\. Виды лечебного питания|________|\\*\\(\\d\\)|4\\. Кровь и ее компоненты|4\\. Перечень медицинских изделий|5\\. Виды лечебного питания|\\* Вероятность предоставления|─────────────────────────|Примечания))";
        Pattern pattern1 = Pattern.compile(regex1);
        Matcher matcher1 = pattern1.matcher(text);
        String text2 = "";
        if (matcher1.find()) {
            text2 = matcher1.group(0);
            text2 = text2.replaceAll("(?m)^\\s*$[\r\n]{1,}", "").trim();
        }
        Pattern pat = Pattern.compile("\\**(\\(\\d\\))*");
        Matcher mat = pat.matcher(text2);
        if(mat.find()){
            text2=text2.replaceAll(Pattern.quote(mat.group()), "").trim();
        }
        String regex3 = "(\\b\\p{L}+)[.,;:!?]*\\s+(1|(0,\\d*))\\s+[.,;:!?]*[а-я][^A-ZА-Я]*";
        Pattern pattern3 = Pattern.compile(regex3);
        Matcher matcher3 = pattern3.matcher(text2);
        while (matcher3.find()) {
            String partText = matcher3.group();
            Pattern pattern4 = Pattern.compile("(.|\\s)*?(?=(?:1|(0,\\d*)))");
            Matcher matcher4 = pattern4.matcher(partText);
            String partTextFirst = "";
            if (matcher4.find()) {
                partTextFirst = matcher4.group(0);
            }
            String partTextSecond = partText.replaceFirst(Pattern.quote(partTextFirst), "");
            pattern4 = Pattern.compile("\\b1|(0,\\d*)\\b");
            matcher4 = pattern4.matcher(partTextSecond);
            String number = "";
            if (matcher4.find()) {
                number = matcher4.group();
            }
            partTextSecond = partTextSecond.replaceFirst(Pattern.quote(number), "");
            partTextFirst = partTextFirst.replaceAll("\\s+", " ");
            partTextSecond = partTextSecond.replaceAll("\\s+", " ");
            partTextFirst = partTextFirst.trim();
            partTextSecond = partTextSecond.trim();
            String res;
            if (wordCheckInDictionary(partTextFirst,partTextSecond)) {
                res = partTextFirst + " " + partTextSecond + " " + number + " ";
            } else {
                res = partTextFirst + partTextSecond + " " + number + " ";
            }
            text2 = text2.replaceFirst(Pattern.quote(matcher3.group()), res);

        }
        text2 = deleteSpaces(text2);
        Pattern pattern4 = Pattern.compile("([A-ZА-Я]|№)\\d\\d\\d?[A-ZА-Я][A-ZА-Я][\\s\\S]*?(?=([A-ZА-Я]|№)\\d\\d\\d?[A-ZА-Я][A-ZА-Я])");
        List<String> res = new ArrayList<>();
        Matcher matcher4 =pattern4.matcher(text2);
        while (matcher4.find()){
            res.add(deleteLine(matcher4.group()));
            String patternToReplace = Pattern.quote(matcher4.group());
            text2 = text2.replaceAll(patternToReplace, "");
        }
        res.add(deleteLine(text2));
        saveDictionary(newWords, "wordDictionary.txt");
        return res;
    }
    public List<String> getNameOfDisease(String text) throws UnsupportedEncodingException {
        oldWords = loadDictionary("wordDictionary.txt");
        String name = "";
        Pattern pattern1 = Pattern.compile("(?<=Код по МКБ X|Код по МКБ Х)[\\s\\S]*?(?=1\\. Медицинские)");
        Matcher matcher1 = pattern1.matcher(text);
        if (matcher1.find()) {
            name = matcher1.group();
        }
        name = name.replaceAll("Нозологические единицы","").trim();
          name=name.replaceAll("Нозологическиеединицы","").trim();
        name=name.replaceAll("Нозологические","").trim();
        name=name.replaceAll("единицы","").trim();
        name = deleteSpaces(name);
        name = name.trim();
        Pattern pattern4 = Pattern.compile("\\**(\\(\\d\\))*");
        Matcher matcher4 = pattern4.matcher(name);
        if(matcher4.find()){
            name = name.replaceAll(Pattern.quote(matcher4.group()),"").trim();
        }
        List<String> names = new ArrayList<>();
        Pattern pattern2 = Pattern.compile("[^(\\-][A-ZА-Я]\\d+[\\s\\S]*?(?=[^(\\-][A-ZА-Я]\\d+)");
        Matcher matcher2 = pattern2.matcher(name);
        while(matcher2.find()){
            names.add(deleteLine(matcher2.group()));
            String patternToReplace = Pattern.quote(matcher2.group());
            name = name.replaceAll(patternToReplace, "");
        }
        pattern2 = Pattern.compile("[A-ZА-Я]\\d+[\\s\\S]*?(?=[A-ZА-Я]\\d+)");
        matcher2 = pattern2.matcher(name);
        while (matcher2.find()){
            if(!matcher2.group().contains("-")&&!matcher2.group().contains("(")){
            names.add(deleteLine(matcher2.group()));
            String patternToReplace = Pattern.quote(matcher2.group());
            name = name.replaceAll(patternToReplace, "");}
        }
        pattern2 = Pattern.compile("I?\\d+\\.\\d+[\\s\\S]*?(?=I?\\d+\\.\\d+)");
        matcher2 = pattern2.matcher(name);
        while (matcher2.find()){
            names.add(deleteLine(matcher2.group()));
            String patternToReplace = Pattern.quote(matcher2.group());
            name = name.replaceAll(patternToReplace, "");
        }

        names.add(deleteLine(name));
        saveDictionary(newWords, "wordDictionary.txt");
        return names;
    }
    public String deleteSpaces(String text) {
        String textWithoutSpaces = "";
        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            String trimmedLine = line.trim();
            result.append(trimmedLine).append("\n");
        }
        textWithoutSpaces = result.toString();
        return textWithoutSpaces;
    }

    public boolean checkWord(String words) {
        boolean res = false;
        String[] prepositionsAndConjunctions = {"в", "на", "за", "под", "из", "у", "о", "и", "с", "к", "от", "до","или","для"};
        String[] wordsToCheck = words.split("\\s+");
        String wordToCheck = wordsToCheck[0];
        if(Character.isUpperCase(wordToCheck.charAt(0))){
            res=true;
        }
        if((wordsToCheck.length>1)&&(isNumeric(words.split("\\s+")[1]))){
            res = true;
        }
        if (wordToCheck.length() > 3) {
            res = true;
        }
        for (String preposition : prepositionsAndConjunctions) {
            if (preposition.equalsIgnoreCase(wordToCheck)) {
                res = true;
            }
        }
        if(isNumeric(wordToCheck)){
            res = true;
        }

        return res;
    }
    public boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            str = str.replace(',', '.');
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public String deleteLine(String text) throws UnsupportedEncodingException {
        String res = "";
        if(text.startsWith("\n")){
            text = text.replaceFirst("\n","");
        }
        if(text.endsWith("\n")){
            text = text.substring(0, text.length() - 1);
        }
        String [] words  = text.split("\n");
        words[0] = words[0].trim();
        res = res+words[0];
        for(int i=1;i< words.length;i++){
            words[i]=words[i].trim();
            String[] wordsForCheck1 =words[i-1].split("\\s+");
            String[] wordsForCheck2 = words[i].split("\\s+");
            boolean check = false;
            if(wordsForCheck1.length>0&&wordsForCheck2.length>0&&(wordsForCheck1[wordsForCheck1.length-1].trim()!=""&& wordsForCheck2[0].trim()!="")){
            check = wordCheckInDictionary(wordsForCheck1[wordsForCheck1.length-1], wordsForCheck2[0]);}
            if(check == true){
                res = res +" "+words[i];
            }
            else{
                res = res+words[i];
            }

        }
        return res;
    }
    public boolean wordCheckInDictionary(String word1, String word2) throws UnsupportedEncodingException {
        word1=word1.trim();
        word2=word2.trim();
        String wordToCheck = word1+word2;
        boolean res = false;
        String encodedSearchTerm = URLEncoder.encode(wordToCheck, "UTF-8");
        String URL = "https://www.rlsnet.ru/search_result.htm?word="+encodedSearchTerm;
        if(isNumeric(word1)||isNumeric(word2)){
            res=true;
        }
        if(Character.isUpperCase(word2.charAt(0))){
            res=true;
        }
        boolean checkInHtml = false;
        if(res==false){
            if(!oldWords.containsKey(wordToCheck)){
            try {
                Connection connection = Jsoup.connect(URL);
                Document doc = connection.get();
                Elements searchResults = doc.select(".search-result");
                for (Element result : searchResults) {
                    Elements blocks = result.select(".block");
                    if (blocks.isEmpty()) {
                        res = true;
                        newWords.put(wordToCheck, word1+" "+word2);
                        oldWords.put(wordToCheck, word1+" "+word2);
                        break;
                    }
                    else{
                        cycle1:
                        for(Element block: blocks){
                          Elements li = block.getElementsByTag("li");
                          for(Element el: li){
                              String searchWords = String.valueOf(el.select(".link"));
                              if(searchWords.contains(wordToCheck.replaceAll("[,.]",""))){
                                  checkInHtml = true;
                                  break cycle1;
                              }
                          }

                        }
                        if(checkInHtml == true){
                            res = false;
                            oldWords.put(wordToCheck, wordToCheck);
                            newWords.put(wordToCheck, wordToCheck);
                        }
                        else{
                            res = true;
                            newWords.put(wordToCheck, word1+" "+word2);
                            oldWords.put(wordToCheck, word1+" "+word2);
                        }
                    }
                }

            } catch (IOException e) {
                    e.printStackTrace();
                    res = true;
            }
            try {
                    Thread.sleep(2001);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        else{
             String wordFromDictionary = oldWords.get(wordToCheck);
             if(wordFromDictionary.contains(" ")){
                 res = true;
             }
             else{
                 res= false;
             }
            }
        }

        return res;
    }
    private HashMap<String, String> loadDictionary(String fileName) {
        HashMap<String, String> loadedWords = new HashMap<>();
        File file = new File(fileName);
        try {
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" - ");
                    if (parts.length == 2) {
                        loadedWords.put(parts[0], parts[1]);
                    }
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return loadedWords;
    }
    private void saveDictionary(HashMap<String, String> words, String filename) {
        try {
            FileWriter writer = new FileWriter(filename, true);
            for (String word : words.keySet()) {
                writer.write(word + " - " + words.get(word) + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
