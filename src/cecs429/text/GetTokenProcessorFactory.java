/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.text;

/**
 *
 * @author atandel
 */
public class GetTokenProcessorFactory {
    
    public TokenProcessor GetTokenProcessor(String processorType) {
        if(processorType == null){  
             return null;  
            }  
          if(processorType.equalsIgnoreCase("advance")) {  
                 return new AdvanceTokenProcessor();  
               }   
           else if(processorType.equalsIgnoreCase("soundex")){  
                return new SoundexTokenProcessor();  
            }   
         return null;
    }
    
}
