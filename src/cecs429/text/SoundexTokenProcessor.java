/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.text;

import static cecs429.text.AdvanceTokenProcessor.removenonAlphanumeric;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author atandel
 */
public class SoundexTokenProcessor implements TokenProcessor{

    @Override
    public List<String> processToken(String token) {
        List<String> Stringtokens = new ArrayList<>();
        String[] termList = token.split(" "); 
         
        for(String term : termList){
            String normalizedTerm = term.replaceAll("\\W", "").toLowerCase();
            Stringtokens.add(normalizedTerm);
        }
        
        return Stringtokens;        
    }
    
}
