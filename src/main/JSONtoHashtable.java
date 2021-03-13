package main;

import java.util.HashMap;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.Scanner;

public class JSONtoHashtable {
    public static HashMap<String, Object> toHashtable(String JSONString){
        ArrayList<String> keys = new ArrayList<String>();
        ArrayList<Object> values = new ArrayList<Object>();
    
        HashMap<String, Object> data = new HashMap<String, Object>();
        final String patternUsing = "\"((\\.|[^\"])+[^\\\\])\"(?=\\s*:([\"0-9]|(false|true|null)))"; // slightly altered version of https://regexr.com/56m28
        final String arrayPattern = "\"([a-z_-]+?)\":(?=[^\"])(\\[.+?(\\])(?=,|}))"; // https://regexr.com/5e4v3 (created by me)

        Pattern arrayKey = Pattern.compile(arrayPattern);
        Matcher array = arrayKey.matcher(JSONString);

        while (array.find()){
            int startInd = array.start();

            String key = array.group(1);
            String arr = array.group(2);
            arr = arr.substring(1,arr.length());

            HashMap<String, Object> parsed = toHashtable(arr);

            JSONString = JSONString.substring(0, startInd-1) + JSONString.substring(array.end() + 1, JSONString.length()); // delete array from string

            keys.add(key);
            values.add(parsed);
        } // handles the key-value pairs associated with arrays in the data set

        Pattern key = Pattern.compile(patternUsing);
        Matcher message = key.matcher(JSONString);

        while (message.find())
            keys.add(message.group(1)); // handles the rest of the non-array associated keys in the data set

        Scanner scanner = new Scanner(JSONString);
        scanner.useDelimiter(patternUsing);

        while (scanner.hasNext()){
            String val = scanner.next();

            if (!(val.equals("}") || val.equals("{"))){
                val = val
                    .substring(1,val.length()-1) //remove quotes
                    .replaceAll("\\\\\"", "\""); //replace escaped characters
                val = val.isEmpty() ? "not set" : val;  //check if empty
                values.add(val);
            }
        } // handles the rest of the non-array associated values in the data set

        for (int i = 0; i < keys.size(); i++)
            data.put(keys.get(i), values.get(i)); // merge into the single hashtable
        
        scanner.close();
        return data;
        
    }
}