import java.util.HashMap;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.Scanner;

public class JSONtoHashtable {
    public static HashMap<String, Object> toHashtable(String JSONString){
        ArrayList<String> keys = new ArrayList<String>();
        ArrayList<Object> values = new ArrayList<Object>();
    
        HashMap<String, Object> data = new HashMap<String, Object>();
        final String patternUsing = "\"(\\.|[^\"])+[^\\\\]\"(?=\\s*:([\"0-9]|(false|true|null)))"; // slightly altered version of https://regexr.com/56m28
        final String arrayPattern = "(?=[^\"])\\[.+?(\\])(?=,|})"; // https://regexr.com/5e4v3 (created by me)

        Pattern arrayKey = Pattern.compile(arrayPattern);
        Matcher array = arrayKey.matcher(JSONString);

        while (array.find()){
            int startInd = array.start();
            if (! JSONString.substring(startInd-3,startInd).equals("\\\":")){
                String arrayVal = array.group();
                String foundKey = "";

                byte temp = 0;
                for (int c = startInd; c <= startInd; c--){
                    if (JSONString.charAt(c) == '\"' && temp != 2){
                        temp++;
                        if (temp == 2){
                            foundKey = JSONString.substring(c,startInd-1);
                            JSONString = JSONString.substring(0,c-1) + JSONString.substring(array.end()+1,JSONString.length());
                            break;
                        }
                    }
                }

                HashMap<String,Object> returned = toHashtable(arrayVal.substring(1,arrayVal.length()));
                keys.add(foundKey.substring(1,foundKey.length()-1));
                values.add(returned);
            }
        } // handles the key-value pairs associated with arrays in the data set

        Pattern key = Pattern.compile(patternUsing);
        Matcher message = key.matcher(JSONString);

        while (message.find()){
            String find = message.group();
            keys.add(find.substring(1,find.length()-1));
        } // handles the rest of the non-array associated keys in the data set

        Scanner scanner = new Scanner(JSONString);
        scanner.useDelimiter(patternUsing);

        while (scanner.hasNext()){
            String val = scanner.next();
            if ( !(val.equals("}")|val.equals("{")) ){

                val = val.substring(1,val.length()-1);

                val = (val.length() == 0) ? "not set" : val;

                val = val.replaceAll("\\\\\"", "\"");

                values.add(val);
            }
        } // handles the rest of the non-array associated values in the data set

        for (int i = 0; i < keys.size(); i++)
            data.put(keys.get(i), values.get(i)); // merge into the single hashtable
        
        scanner.close();
        return data;
        
    }
}
// TODO: make this more readable