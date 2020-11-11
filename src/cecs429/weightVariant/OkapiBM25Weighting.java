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
        double val1 = 0.1;
        double val2 = Math.log((N-dft+0.5)/(dft+0.5));
        double wqt = Math.max(val1, val2);
        return wqt;
    }

    @Override
    public double calculateWdt(int totalDoc, int docId, double tftd,String dir) {
       double doclength = index.getdocLength(docId, dir);
       double doclengthA = index.getdocLengthA(totalDoc, dir);
       double dividend = 2.2 * tftd;
       double divisor = (1.2*(0.25 + (0.75)*(doclength/doclengthA))) + tftd;
       double wdt = dividend/divisor;
      return wdt;
    }

    @Override
    public double calculateLd(int docId, String dir) {
         return 1;
    }
    
}
