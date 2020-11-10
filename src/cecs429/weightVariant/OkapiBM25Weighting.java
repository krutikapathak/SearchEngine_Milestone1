/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.weightVariant;

import cecs429.index.DiskIndexWriter;

/**
 *
 * @author atandel
 */
public class OkapiBM25Weighting implements WeightingStrategy{

    DiskIndexWriter index = new DiskIndexWriter();
    @Override
    public double calculateWqt(int N, double dft) {
        double a = 0.1;
        double b = Math.log((N-dft+0.5)/(dft+0.5));
        double wqt = Math.max(a, b);
        return wqt;
    }

    @Override
    public double calculateWdt(int totalDoc, int docId, double tftd,String dir) {
        double doclength = 0,doclengthA = 0;
        doclength = index.getdocLength(docId, dir);
         System.out.println("doc len"+doclength);
         doclengthA = index.getdocLengthA(totalDoc, dir);
         System.out.println(doclengthA);
         double numerator = 2.2 * tftd;
      double denominator = (1.2*(0.25 + ((0.75)*(doclength/doclengthA)))) + tftd;
      double wdt = numerator/denominator;
      return wdt;
    }

    @Override
    public double calculateLd(int docId, String dir) {
         return 1;
    }
    
}
