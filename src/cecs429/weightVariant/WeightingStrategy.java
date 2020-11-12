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
public interface WeightingStrategy {
    
    public double calculateWqt(int N, double dft);
    public double calculateWdt(int totalDoc, int docId, double tftd, String dir);
    public double calculateLd(int docId, String dir);
    
}