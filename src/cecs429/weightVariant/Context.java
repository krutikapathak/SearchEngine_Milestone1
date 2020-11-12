/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.weightVariant;

/**
 *
 * @author atandel
 */
public class Context {
    private WeightingStrategy strategy;
    
    public Context(WeightingStrategy strategy){
      this.strategy = strategy;
   }

   public double calculateWqt(int N, double dft) {
        return strategy.calculateWqt(N, dft);
    }

    
    public double calculateWdt(int totalDoc, int docId, double tftd, String dir) {
        return strategy.calculateWdt(totalDoc, docId, tftd, dir);
    }

    
    public double calculateLd(int docId, String dir) {
       return strategy.calculateLd(docId, dir);
    }
    
}